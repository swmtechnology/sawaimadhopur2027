package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

data class UserProfileData(
    val profileId: String = "SWM-GUEST",
    val name: String = "Name",
    val phone: String = "91000000",
    val email: String = "",
    val city: String = "City",
    val imagePath: String? = null,
    val isCustomized: Boolean = false
)

object UserProfileManager {
    private const val PREFS_NAME = "sawai_user_profile_prefs"
    private const val KEY_PROFILE_ID = "profile_id"
    private const val KEY_NAME = "profile_name"
    private const val KEY_PHONE = "profile_phone"
    private const val KEY_EMAIL = "profile_email"
    private const val KEY_CITY = "profile_city"
    private const val KEY_IMAGE_PATH = "profile_image_path"
    private const val KEY_CUSTOMIZED = "profile_customized"

    private val _profileState = MutableStateFlow(UserProfileData())
    val profileState: StateFlow<UserProfileData> = _profileState.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val profileId = prefs.getString(KEY_PROFILE_ID, "SWM-GUEST") ?: "SWM-GUEST"
        val name = prefs.getString(KEY_NAME, "सवाई माधोपुर गेस्ट") ?: "सवाई माधोपुर गेस्ट"
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val city = prefs.getString(KEY_CITY, "सवाई माधोपुर") ?: "सवाई माधोपुर"
        val imagePath = prefs.getString(KEY_IMAGE_PATH, null)
        val isCustomized = prefs.getBoolean(KEY_CUSTOMIZED, false)

        // Verify image exists if set
        val validImagePath = if (imagePath != null && File(imagePath).exists()) {
            imagePath
        } else {
            null
        }

        _profileState.value = UserProfileData(
            profileId = profileId,
            name = name,
            phone = phone,
            email = email,
            city = city,
            imagePath = validImagePath,
            isCustomized = isCustomized
        )
    }

    /**
     * Called when user logs in or signs up:
     * Details from signup/login immediately populate the single user profile.
     */
    fun syncWithSession(context: Context, session: SessionEntity) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingProfileId = prefs.getString(KEY_PROFILE_ID, null)
        val generatedProfileId = if (!existingProfileId.isNullOrBlank() && existingProfileId != "SWM-GUEST") {
            existingProfileId
        } else if (session.userId > 0) {
            "SWM-${10000 + session.userId}"
        } else {
            val hash = kotlin.math.abs((session.email.ifBlank { session.username }).hashCode() % 90000) + 10000
            "SWM-$hash"
        }

        val cleanName = session.fullName.ifBlank {
            session.username.replaceFirstChar { it.uppercase() }
        }
        val cleanPhone = session.mobile
        val cleanEmail = session.email
        val cleanCity = prefs.getString(KEY_CITY, "सवाई माधोपुर") ?: "सवाई माधोपुर"
        val currentImagePath = prefs.getString(KEY_IMAGE_PATH, null)

        prefs.edit()
            .putString(KEY_PROFILE_ID, generatedProfileId)
            .putString(KEY_NAME, cleanName)
            .putString(KEY_PHONE, cleanPhone)
            .putString(KEY_EMAIL, cleanEmail)
            .putString(KEY_CITY, cleanCity)
            .putBoolean(KEY_CUSTOMIZED, true)
            .apply()

        _profileState.value = UserProfileData(
            profileId = generatedProfileId,
            name = cleanName,
            phone = cleanPhone,
            email = cleanEmail,
            city = cleanCity,
            imagePath = currentImagePath,
            isCustomized = true
        )
    }

    fun saveProfile(
        context: Context,
        name: String,
        phone: String,
        email: String = "",
        city: String = "सवाई माधोपुर",
        newImageUri: Uri? = null
    ) {
        val cleanName = name.trim().ifEmpty { "सवाई माधोपुर यूज़र" }
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim()
        val cleanCity = city.trim().ifEmpty { "सवाई माधोपुर" }

        var finalImagePath = _profileState.value.imagePath

        // If user picked a new image from photo picker, copy to internal storage
        if (newImageUri != null) {
            try {
                val avatarFile = File(context.filesDir, "guest_profile_avatar.jpg")
                context.contentResolver.openInputStream(newImageUri)?.use { input ->
                    FileOutputStream(avatarFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (avatarFile.exists() && avatarFile.length() > 0) {
                    finalImagePath = avatarFile.absolutePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val currentProfileId = if (_profileState.value.profileId.isNotBlank() && _profileState.value.profileId != "SWM-GUEST") {
            _profileState.value.profileId
        } else {
            "SWM-${(10000..99999).random()}"
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PROFILE_ID, currentProfileId)
            .putString(KEY_NAME, cleanName)
            .putString(KEY_PHONE, cleanPhone)
            .putString(KEY_EMAIL, cleanEmail)
            .putString(KEY_CITY, cleanCity)
            .putString(KEY_IMAGE_PATH, finalImagePath)
            .putBoolean(KEY_CUSTOMIZED, true)
            .apply()

        _profileState.value = UserProfileData(
            profileId = currentProfileId,
            name = cleanName,
            phone = cleanPhone,
            email = cleanEmail,
            city = cleanCity,
            imagePath = finalImagePath,
            isCustomized = true
        )
    }

    fun removeProfileImage(context: Context) {
        val avatarFile = File(context.filesDir, "guest_profile_avatar.jpg")
        if (avatarFile.exists()) {
            avatarFile.delete()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IMAGE_PATH).apply()

        _profileState.value = _profileState.value.copy(imagePath = null)
    }

    fun clearOnLogout(context: Context) {
        val avatarFile = File(context.filesDir, "guest_profile_avatar.jpg")
        if (avatarFile.exists()) {
            avatarFile.delete()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        _profileState.value = UserProfileData()
    }
}
