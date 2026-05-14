package com.example.pinq_frontend.ui.wrongnote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.local.SavedWrongNote
import com.example.pinq_frontend.data.local.WrongNoteStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WrongNoteViewModel(private val store: WrongNoteStore) : ViewModel() {

    private val _allNotes = MutableStateFlow<List<SavedWrongNote>>(emptyList())
    val allNotes: StateFlow<List<SavedWrongNote>> = _allNotes.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _allNotes.value = withContext(Dispatchers.IO) { store.getAll() }
        }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer { WrongNoteViewModel(WrongNoteStore(context.applicationContext)) }
        }
    }
}
