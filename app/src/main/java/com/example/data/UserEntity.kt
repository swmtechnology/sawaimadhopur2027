package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val mobile: String = "",
    val username: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)
