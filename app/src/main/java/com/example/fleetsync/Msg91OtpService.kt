package com.example.fleetsync

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Msg91OtpService {
    private val client = OkHttpClient()

    fun sendOtp(mobile: String, onResult: (Boolean, String?) -> Unit) {
        val url = "https://api.msg91.com/api/v5/otp?authkey=${Constants.AUTH_KEY}&mobile=$mobile&template_id=${Constants.TEMPLATE_ID}&otp_length=6"
        
        // MSG91 allows parameters in URL for POST as well.
        // If body is required, we can send an empty JSON body.
        val body = "{}".toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Msg91OtpService", "Send OTP Failed: ${e.message}")
                onResult(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("Msg91OtpService", "Send OTP Response: $responseData")
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, responseData ?: "Unknown error")
                }
            }
        })
    }

    fun verifyOtp(mobile: String, otp: String, onResult: (Boolean, String?) -> Unit) {
        val url = "https://api.msg91.com/api/v5/otp/verify?authkey=${Constants.AUTH_KEY}&mobile=$mobile&otp=$otp"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Msg91OtpService", "Verify OTP Failed: ${e.message}")
                onResult(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("Msg91OtpService", "Verify OTP Response: $responseData")
                // MSG91 verification response usually contains "type":"success"
                if (response.isSuccessful && responseData?.contains("success") == true) {
                    onResult(true, null)
                } else {
                    onResult(false, responseData ?: "Invalid OTP")
                }
            }
        })
    }
}
