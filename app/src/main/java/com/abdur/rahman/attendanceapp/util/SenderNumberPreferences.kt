package com.abdur.rahman.attendanceapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.senderDataStore: DataStore<Preferences> by preferencesDataStore(name = "sender_settings")

class SenderNumberPreferences(private val context: Context) {
    
    companion object {
        private val SENDER_NUMBER_KEY = stringPreferencesKey("sender_number")
    }
    
    val senderNumber: Flow<String> = context.senderDataStore.data.map { preferences ->
        preferences[SENDER_NUMBER_KEY] ?: ""
    }
    
    suspend fun saveSenderNumber(number: String) {
        context.senderDataStore.edit { preferences ->
            preferences[SENDER_NUMBER_KEY] = number
        }
    }
    
    /**
     * Try to detect the SIM card phone number
     * Note: This often returns null/empty as many carriers don't store the number on the SIM
     */
    fun detectSimNumber(): String? {
        return try {
            // Check for READ_PHONE_STATE permission
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            
            // Try using SubscriptionManager (API 22+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_NUMBERS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val subscriptionInfoList = subscriptionManager?.activeSubscriptionInfoList
                    if (!subscriptionInfoList.isNullOrEmpty()) {
                        val number = subscriptionInfoList[0].number
                        if (!number.isNullOrBlank()) {
                            return number
                        }
                    }
                }
            }
            
            // Fallback to TelephonyManager
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            
            @Suppress("DEPRECATION")
            val lineNumber = telephonyManager?.line1Number
            
            if (!lineNumber.isNullOrBlank()) {
                return lineNumber
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get list of available SIM cards (for dual-SIM devices)
     */
    fun getAvailableSims(): List<SimInfo> {
        val sims = mutableListOf<SimInfo>()
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                    val subscriptionInfoList = subscriptionManager?.activeSubscriptionInfoList
                    
                    subscriptionInfoList?.forEach { info ->
                        val number = if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_PHONE_NUMBERS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            info.number ?: ""
                        } else {
                            ""
                        }
                        
                        sims.add(
                            SimInfo(
                                subscriptionId = info.subscriptionId,
                                carrierName = info.carrierName?.toString() ?: "Unknown",
                                displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                                phoneNumber = number,
                                slotIndex = info.simSlotIndex
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return sims
    }
}

data class SimInfo(
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String,
    val phoneNumber: String,
    val slotIndex: Int
)
