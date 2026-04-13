package com.example.coroutinesflows.domain.usecase.note

import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(title: String, content: String): Long {
        require(title.isNotBlank()) { "Title cannot be blank" }
        return repository.addNote(Note(title = title.trim(), content = content.trim()))
    }
}
