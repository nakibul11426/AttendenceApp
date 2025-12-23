package com.abdur.rahman.attendanceapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat

object SmsHelper {
    
    /**
     * Send SMS notification to parent about student absence
     * Returns true if SMS was sent successfully
     */
    fun sendAbsenceSms(
        context: Context,
        phoneNumber: String,
        studentName: String
    ): Boolean {
        return try {
            if (!hasSmSPermission(context)) {
                Toast.makeText(
                    context,
                    "SMS permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
                return false
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
            
            Toast.makeText(
                context,
                "SMS sent to parent of $studentName",
                Toast.LENGTH_SHORT
            ).show()
            
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to send SMS: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            false
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
