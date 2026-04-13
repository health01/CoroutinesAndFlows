package com.example.coroutinesflows.domain.usecase.note

import com.example.coroutinesflows.domain.model.Note
import com.example.coroutinesflows.domain.repository.NoteRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.deleteNote(note)
}
