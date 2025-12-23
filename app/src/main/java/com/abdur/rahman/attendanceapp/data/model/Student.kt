package com.abdur.rahman.attendanceapp.data.model

data class Student(
    val id: String = "",
    val name: String = "",
    val parentPhone: String = "",
    val isActive: Boolean = true, // false when removed (hidden from attendance list)
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", true, 0L)
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "parentPhone" to parentPhone,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>, id: String): Student {
            return Student(
                id = id,
                name = map["name"] as? String ?: "",
                parentPhone = map["parentPhone"] as? String ?: "",
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = map["createdAt"] as? Long ?: 0L
            )
        }
    }
}
