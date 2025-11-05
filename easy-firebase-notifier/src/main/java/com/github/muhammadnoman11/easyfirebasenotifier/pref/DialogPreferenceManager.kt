package com.github.muhammadnoman11.easyfirebasenotifier.pref

import android.content.Context
import android.content.SharedPreferences
import com.github.muhammadnoman11.easyfirebasenotifier.data.DialogData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */



/**
 * Manages dialog notification preferences
 */
class DialogPreferenceManager(context: Context) :
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _dialogDataFlow = MutableStateFlow<DialogData?>(getDialogData())
    val dialogDataFlow: StateFlow<DialogData?> = _dialogDataFlow.asStateFlow()

    companion object {
        private const val PREF_NAME = "fcm_dialog_prefs"
        private const val KEY_HAS_DIALOG = "has_dialog"
        private const val KEY_TITLE = "dialog_title"
        private const val KEY_BODY = "dialog_body"
        private const val KEY_IMAGE_URL = "dialog_image_url"
        private const val KEY_TIMESTAMP = "dialog_timestamp"
        private const val KEY_ACTION = "dialog_action"

        @Volatile
        private var instance: DialogPreferenceManager? = null

        fun getInstance(context: Context): DialogPreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: DialogPreferenceManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }



    /**
     * Save dialog notification data
     */
    fun saveDialogData(dialogData: DialogData) {
        prefs.edit().apply {
            putBoolean(KEY_HAS_DIALOG, true)
            putString(KEY_TITLE, dialogData.title)
            putString(KEY_BODY, dialogData.body)
            putString(KEY_IMAGE_URL, dialogData.imageUrl)
            putLong(KEY_TIMESTAMP, dialogData.timestamp)
            putString(KEY_ACTION, dialogData.action)
            apply()
        }
        _dialogDataFlow.value = dialogData
    }


    /**
     * Get dialog data if available
     */
    fun getDialogData(): DialogData? {
        if (!hasDialogData()) return null
        return DialogData(
            title = prefs.getString(KEY_TITLE, null),
            body = prefs.getString(KEY_BODY, null),
            imageUrl = prefs.getString(KEY_IMAGE_URL, null),
            timestamp = prefs.getLong(KEY_TIMESTAMP, 0L),
            action = prefs.getString(KEY_ACTION, null)
        )
    }

    /**
     * Check if there's pending dialog data
     */
    fun hasDialogData(): Boolean = prefs.getBoolean(KEY_HAS_DIALOG, false)


    /**
     * Clear dialog data after showing
     */
    fun clearDialogData() {
        prefs.edit().apply {
            putBoolean(KEY_HAS_DIALOG, false)
            remove(KEY_TITLE)
            remove(KEY_BODY)
            remove(KEY_IMAGE_URL)
            remove(KEY_TIMESTAMP)
            remove(KEY_ACTION)
            apply()
        }
        _dialogDataFlow.value = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_HAS_DIALOG || key == KEY_TITLE || key == KEY_BODY) {
            _dialogDataFlow.value = getDialogData()
        }
    }


    /**
     * Clear all preferences
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        _dialogDataFlow.value = null
    }
}
