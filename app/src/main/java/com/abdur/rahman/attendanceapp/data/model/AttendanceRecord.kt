package com.abdur.rahman.attendanceapp.data.model

data class AttendanceRecord(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val status: AttendanceStatus = AttendanceStatus.NOT_MARKED,
    val smsSent: Boolean = false, // Track if SMS was sent for this absence
    val timestamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", AttendanceStatus.NOT_MARKED, false, 0L)
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "studentId" to studentId,
            "studentName" to studentName,
            "date" to date,
            "status" to status.name,
            "smsSent" to smsSent,
            "timestamp" to timestamp
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>, id: String): AttendanceRecord {
            return AttendanceRecord(
                id = id,
                studentId = map["studentId"] as? String ?: "",
                studentName = map["studentName"] as? String ?: "",
                date = map["date"] as? String ?: "",
                status = try {
                    AttendanceStatus.valueOf(map["status"] as? String ?: "NOT_MARKED")
                } catch (e: Exception) {
                    AttendanceStatus.NOT_MARKED
                },
                smsSent = map["smsSent"] as? Boolean ?: false,
                timestamp = map["timestamp"] as? Long ?: 0L
            )
        }
    }
}
