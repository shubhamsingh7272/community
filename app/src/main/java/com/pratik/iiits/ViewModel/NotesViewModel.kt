package com.pratik.iiits.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.pratik.iiits.Database.NotesDatabase
import com.pratik.iiits.Models.Notes

import com.pratik.iiits.Repo.NotesRepo

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    val notesRepo : NotesRepo

    init {
        val dao = NotesDatabase.getDatabaseInstance(application).myNotesDao()
        notesRepo = NotesRepo(dao)
    }

    fun addNotes(notes: Notes){
        notesRepo.insertNotes(notes)
    }
    fun getallnotes():LiveData<List<Notes>> = notesRepo.getallnotes()

    fun getHighNotes():LiveData<List<Notes>> = notesRepo.getHighNotes()


    fun getMediumNotes():LiveData<List<Notes>> = notesRepo.getMediumNotes()


    fun getLowNotes():LiveData<List<Notes>> = notesRepo.getLowNotes()


    fun deleteNotes(id:Int){
        notesRepo.deleteNotes(id)
    }
    fun updateNotes(notes: Notes){
        notesRepo.updatenotes(notes)
    }
}
