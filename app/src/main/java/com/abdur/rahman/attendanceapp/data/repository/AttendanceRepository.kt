package com.abdur.rahman.attendanceapp.data.repository

import com.abdur.rahman.attendanceapp.data.model.AttendanceRecord
import com.abdur.rahman.attendanceapp.data.model.AttendanceStatus
import com.abdur.rahman.attendanceapp.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AttendanceRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val studentsCollection = firestore.collection("students")
    private val attendanceCollection = firestore.collection("attendance")
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    fun getTodayDate(): String = dateFormat.format(Date())
    
    // ==================== STUDENT OPERATIONS ====================
    
    /**
     * Get all active students as a Flow (real-time updates)
     */
    fun getActiveStudents(): Flow<List<Student>> = callbackFlow {
        val listener = studentsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Student.fromMap(it, doc.id) }
                }?.sortedBy { it.name } ?: emptyList()
                
                trySend(students)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Add a new student
     */
    suspend fun addStudent(name: String, parentPhone: String): Result<Student> {
        return try {
            val id = UUID.randomUUID().toString()
            val student = Student(
                id = id,
                name = name,
                parentPhone = parentPhone,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            
            studentsCollection.document(id).set(student.toMap()).await()
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a student (soft delete - sets isActive to false)
     * Does NOT delete past attendance history
     */
    suspend fun removeStudent(studentId: String): Result<Unit> {
        return try {
            studentsCollection.document(studentId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update student information (name and/or phone)
     */
    suspend fun updateStudent(studentId: String, name: String, parentPhone: String): Result<Unit> {
        return try {
            studentsCollection.document(studentId)
                .update(
                    mapOf(
                        "name" to name,
                        "parentPhone" to parentPhone
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a single student by ID
     */
    suspend fun getStudentById(studentId: String): Student? {
        return try {
            val doc = studentsCollection.document(studentId).get().await()
            doc.data?.let { Student.fromMap(it, doc.id) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get all attendance records for a specific student (historical data)
     */
    fun getStudentAttendanceHistory(studentId: String): Flow<List<AttendanceRecord>> = callbackFlow {
        val listener = attendanceCollection
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AttendanceRecord.fromMap(it, doc.id) }
                } ?: emptyList()
                
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }
    
    // ==================== ATTENDANCE OPERATIONS ====================
    
    /**
     * Get attendance records for today as a Flow (real-time updates)
     */
    fun getTodayAttendance(): Flow<List<AttendanceRecord>> = callbackFlow {
        val today = getTodayDate()
        
        val listener = attendanceCollection
            .whereEqualTo("date", today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AttendanceRecord.fromMap(it, doc.id) }
                } ?: emptyList()
                
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get attendance records for a specific date
     */
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = callbackFlow {
        val listener = attendanceCollection
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val records = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AttendanceRecord.fromMap(it, doc.id) }
                } ?: emptyList()
                
                trySend(records)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get all unique dates that have attendance records (for history)
     */
    fun getAttendanceDates(): Flow<List<String>> = callbackFlow {
        val listener = attendanceCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val dates = snapshot?.documents
                    ?.mapNotNull { it.getString("date") }
                    ?.distinct()
                    ?: emptyList()
                
                trySend(dates)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Create or update attendance record for a student
     * Returns true if SMS should be sent (new absence)
     */
    suspend fun updateAttendance(
        studentId: String,
        studentName: String,
        status: AttendanceStatus
    ): Result<Boolean> {
        return try {
            val today = getTodayDate()
            val recordId = "${studentId}_$today"
            
            // Check if record exists
            val existingDoc = attendanceCollection.document(recordId).get().await()
            val existingRecord = existingDoc.data?.let { 
                AttendanceRecord.fromMap(it, recordId) 
            }
            
            // Determine if SMS should be sent
            val shouldSendSms = status == AttendanceStatus.ABSENT && 
                                existingRecord?.smsSent != true
            
            val record = AttendanceRecord(
                id = recordId,
                studentId = studentId,
                studentName = studentName,
                date = today,
                status = status,
                smsSent = existingRecord?.smsSent ?: false || 
                         (status == AttendanceStatus.ABSENT && shouldSendSms),
                timestamp = System.currentTimeMillis()
            )
            
            attendanceCollection.document(recordId).set(record.toMap()).await()
            Result.success(shouldSendSms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark that SMS was sent for a student's absence
     */
    suspend fun markSmsSent(studentId: String): Result<Unit> {
        return try {
            val today = getTodayDate()
            val recordId = "${studentId}_$today"
            
            attendanceCollection.document(recordId)
                .update("smsSent", true)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if SMS should be sent for a student (hasn't been sent today)
     */
    suspend fun shouldSendSms(studentId: String): Boolean {
        return try {
            val today = getTodayDate()
            val recordId = "${studentId}_$today"
            
            val doc = attendanceCollection.document(recordId).get().await()
            val smsSent = doc.getBoolean("smsSent") ?: false
            
            !smsSent // Return true if SMS hasn't been sent
        } catch (e: Exception) {
            true // Default to sending SMS if we can't check
        }
    }
    
    /**
     * Initialize attendance records for all active students at the start of a new day
     * Sets all students to NOT_MARKED
     */
    suspend fun initializeDailyAttendance(students: List<Student>): Result<Unit> {
        return try {
            val today = getTodayDate()
            val batch = firestore.batch()
            
            students.forEach { student ->
                val recordId = "${student.id}_$today"
                val docRef = attendanceCollection.document(recordId)
                
                // Only create if doesn't exist
                val existing = docRef.get().await()
                if (!existing.exists()) {
                    val record = AttendanceRecord(
                        id = recordId,
                        studentId = student.id,
                        studentName = student.name,
                        date = today,
                        status = AttendanceStatus.NOT_MARKED,
                        smsSent = false,
                        timestamp = System.currentTimeMillis()
                    )
                    batch.set(docRef, record.toMap())
                }
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if attendance was already initialized for today
     */
    suspend fun isTodayInitialized(): Boolean {
        return try {
            val today = getTodayDate()
            val snapshot = attendanceCollection
                .whereEqualTo("date", today)
                .limit(1)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
