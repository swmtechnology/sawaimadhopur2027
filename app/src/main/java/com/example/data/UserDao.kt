package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM sessions WHERE id = 1 AND isLoggedIn = 1 LIMIT 1")
    fun getActiveSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE id = 1 LIMIT 1")
    suspend fun getActiveSessionOnce(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun clearSession()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR LOWER(username) = LOWER(:identifier) OR mobile = :identifier LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun findUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun findUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun findUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun findUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC LIMIT 50")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE LOWER(fullName) LIKE '%' || LOWER(:query) || '%' OR mobile LIKE '%' || :query || '%' OR LOWER(email) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("UPDATE users SET fullName = :fullName, mobile = :mobile, email = :email WHERE id = :userId")
    suspend fun updateUser(userId: Long, fullName: String, mobile: String, email: String)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPasswordHash: String)
}
