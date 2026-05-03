package com.example.fleetsync

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── Auth state machine ───────────────────────────────────────────────────────
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // Auth state observed by the UI
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: androidx.compose.runtime.State<AuthState> = _authState

    // ─── Company list for Driver sign-up dropdown ─────────────────────────────
    var availableCompanies  by mutableStateOf<List<CompanyModel>>(emptyList())
        private set
    var isLoadingCompanies  by mutableStateOf(false)
        private set
    var companiesFetchError by mutableStateOf("")
        private set

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Fetch the list of registered Fleet companies for the Driver sign-up dropdown.
     *
     * The companies collection has "allow read: if true" in Firestore Rules so
     * this query requires NO authentication at all — it works on the sign-up
     * screen before the user has ever logged in.
     *
     * Only writes are restricted (Fleet Owners write their own document).
     */
    fun fetchCompanies() {
        isLoadingCompanies  = true
        companiesFetchError = ""
        Log.d("AuthVM", "Fetching companies (no auth required)…")

        // Simple direct query — no sign-in step needed
        db.collection("companies")
            .get()
            .addOnSuccessListener { snap ->
                val list = mutableListOf<CompanyModel>()
                for (doc in snap.documents) {
                    val name = doc.getString("companyName") ?: ""
                    if (name.isBlank()) continue
                    list.add(
                        CompanyModel(
                            companyId   = doc.id,
                            companyName = name,
                            ownerUid    = doc.getString("ownerUid") ?: "",
                            createdAt   = doc.getLong("createdAt") ?: 0L
                        )
                    )
                    Log.d("AuthVM", "  Company: $name")
                }
                availableCompanies  = list
                isLoadingCompanies  = false
                Log.d("AuthVM", "Total companies loaded: ${list.size}")
            }
            .addOnFailureListener { e ->
                isLoadingCompanies  = false
                companiesFetchError = when {
                    e.message?.contains("PERMISSION_DENIED") == true ->
                        // Tell the developer exactly which rule to add
                        "Firestore rule needed: in companies collection add \"allow read: if true;\""
                    e.message?.contains("NETWORK_ERROR") == true
                    || e.message?.contains("UNAVAILABLE")  == true ->
                        "No internet connection. Check your network and tap Retry."
                    else ->
                        "Could not load companies: ${e.message}"
                }
                Log.e("AuthVM", "fetchCompanies failed: ${e.message}")
            }
    }

    // ─── Registration ─────────────────────────────────────────────────────────
    /**
     * @param companyName  Only used when role == "Fleet Owner". The name of the company.
     * @param companyId    For Fleet Owners: will be their own uid. For Drivers: the selected company's id.
     */
    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String,
        companyName: String = "",
        companyId: String   = ""
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Create Firebase Auth account
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: throw Exception("Auth user was null after creation")
                val uid = user.uid

                if (role == "Fleet Owner") {
                    // ── Fleet Owner path ──────────────────────────────────────
                    // companyId for an owner = their own uid (simple, stable, unique)
                    val ownerCompanyId = uid

                    // a) Write the company document
                    val companyDoc = hashMapOf(
                        "companyId"   to ownerCompanyId,
                        "companyName" to companyName.ifBlank { "$name's Fleet" },
                        "ownerUid"    to uid,
                        "createdAt"   to System.currentTimeMillis()
                    )
                    db.collection("companies").document(ownerCompanyId).set(companyDoc).await()
                    Log.d("AuthVM", "Company doc written: $ownerCompanyId")

                    // b) Write the user document with companyId
                    val userModel = UserModel(
                        uid       = uid,
                        name      = name,
                        email     = email,
                        phone     = phone,
                        role      = role,
                        companyId = ownerCompanyId
                    )
                    db.collection("users").document(uid).set(userModel).await()

                } else if (role == "Driver") {
                    // ── Driver path ───────────────────────────────────────────
                    val userModel = UserModel(
                        uid          = uid,
                        name         = name,
                        email        = email,
                        phone        = phone,
                        role         = role,
                        companyId    = companyId,
                        fleetOwnerUid = companyId   // legacy compat
                    )
                    db.collection("users").document(uid).set(userModel).await()
                    Log.d("AuthVM", "Driver user doc written, companyId=$companyId")

                    // Also register driver into the company's drivers sub-collection
                    if (companyId.isNotBlank()) {
                        val driverRef = hashMapOf(
                            "driverUid"  to uid,
                            "driverName" to name,
                            "addedAt"    to System.currentTimeMillis()
                        )
                        db.collection("companies")
                            .document(companyId)
                            .collection("drivers")
                            .document(uid)
                            .set(driverRef)
                            .await()
                        Log.d("AuthVM", "Driver added to company/$companyId/drivers/$uid")
                    }

                } else {
                    // ── Other roles (Admin, Client) ───────────────────────────
                    val userModel = UserModel(
                        uid   = uid,
                        name  = name,
                        email = email,
                        phone = phone,
                        role  = role
                    )
                    db.collection("users").document(uid).set(userModel).await()
                }

                updateFcmToken()
                _authState.value = AuthState.Success(role)

            } catch (e: Exception) {
                Log.e("AuthVM", "Registration error: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: throw Exception("Login failed")
                val document = db.collection("users").document(user.uid).get().await()
                if (document.exists()) {
                    val role = document.getString("role") ?: "Fleet Owner"
                    updateFcmToken()
                    _authState.value = AuthState.Success(role)
                } else {
                    _authState.value = AuthState.Error("User record not found in database.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed. Check your credentials.")
            }
        }
    }

    fun updateFcmToken() {
        val uid = auth.currentUser?.uid ?: return
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("users").document(uid).update("fcmToken", token)
                .addOnSuccessListener { Log.d("AuthVM", "FCM Token updated: $token") }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}