package com.pratik.iiits.Repo

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.pratik.iiits.Dao.NotesDao
import com.pratik.iiits.Models.Notes


class NotesRepo(val dao: NotesDao) {
    fun getallnotes(): LiveData<List<Notes>>{
        return dao.getNotes()
    }

    fun getHighNotes():LiveData<List<Notes>> = dao.getHighNotes()


    fun getMediumNotes():LiveData<List<Notes>> = dao.getMediumNotes()


    fun getLowNotes():LiveData<List<Notes>> = dao.getLowNotes()


    fun insertNotes(notes: Notes){
        dao.insertNotes(notes)
    }

    fun deleteNotes(id: Int){
        dao.deleteNote(id)
    }
    fun updatenotes(notes: Notes){
        dao.updateNote(notes)
    }
}