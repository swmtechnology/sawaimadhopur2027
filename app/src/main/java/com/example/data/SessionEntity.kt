package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Single active session row
    val userId: Long,
    val fullName: String,
    val email: String,
    val mobile: String = "",
    val username: String,
    val isLoggedIn: Boolean,
    val loginTimestamp: Long = System.currentTimeMillis()
)
