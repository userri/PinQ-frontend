package com.example.pinq_frontend.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class WrongNoteStore(context: Context) {

    private val prefs = context.getSharedPreferences("wrong_notes_v1", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, SavedWrongNote::class.java)
    private val adapter = moshi.adapter<List<SavedWrongNote>>(listType)

    fun getAll(): List<SavedWrongNote> {
        val json = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }
            .getOrDefault(emptyList())
    }

    /** 같은 quizId가 이미 있으면 savedDateMillis·myAnswerText를 갱신, 없으면 추가. */
    fun upsert(notes: List<SavedWrongNote>) {
        val existing = getAll().associateBy { it.quizId }.toMutableMap()
        for (note in notes) {
            existing[note.quizId] = note
        }
        val json = adapter.toJson(existing.values.toList())
        prefs.edit().putString(KEY_NOTES, json).apply()
    }

    companion object {
        private const val KEY_NOTES = "notes_json"
    }
}
