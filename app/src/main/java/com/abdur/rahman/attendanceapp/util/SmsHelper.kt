package com.abdur.rahman.attendanceapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

// Sealed class to represent SMS result
sealed class SmsResult {
    data class Success(val studentName: String) : SmsResult()
    data class Error(val message: String, val studentName: String) : SmsResult()
    object PermissionDenied : SmsResult()
}

object SmsHelper {
    
    /**
     * Send SMS notification to parent about student absence
     * Returns SmsResult indicating success or failure
     */
    fun sendAbsenceSms(
        context: Context,
        phoneNumber: String,
        studentName: String
    ): SmsResult {
        return try {
            if (!hasSmSPermission(context)) {
                return SmsResult.PermissionDenied
            }
            
            val message = "Your child $studentName was marked absent today."
            
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(
                phoneNumber,
                null,
                message,
                null,
                null
            )
            
            SmsResult.Success(studentName)
        } catch (e: Exception) {
            SmsResult.Error(e.message ?: "Unknown error", studentName)
        }
    }
    
    /**
     * Check if SMS permission is granted
     */
    fun hasSmSPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
