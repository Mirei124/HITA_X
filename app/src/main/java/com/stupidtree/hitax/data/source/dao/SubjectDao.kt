package com.stupidtree.hitax.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stupidtree.hitax.data.model.timetable.SubjectColor
import com.stupidtree.hitax.data.model.timetable.TermSubject

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subject WHERE id is :id")
    fun getSubjectById(id: String): LiveData<TermSubject>

    @Query("SELECT * FROM subject WHERE id in (:ids)")
    fun getSubjectsInIdsSync(ids: List<String>): List<TermSubject>


    @Query("SELECT * FROM subject WHERE timetableId is :timetableId AND name is :name")
    fun getSubjectByName(timetableId: String, name: String?): TermSubject?

    @Query("SELECT * FROM subject WHERE timetableId is :timetableId")
    fun getSubjectsSync(timetableId: String): List<TermSubject>

    @Query("SELECT * FROM subject WHERE timetableId is :timetableId")
    fun getSubjects(timetableId: String): LiveData<List<TermSubject>>


    @Query("select id,color from subject where id in (:ids)")
    fun getSubjectColorsWithId(ids: Set<String>): LiveData<List<SubjectColor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSubjectSync(data: TermSubject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSubjectsSync(data: List<TermSubject>)

    @Query("DELETE from subject where timetableId in (:ids)")
    fun deleteSubjectsFromTimetablesSync(ids: List<String>)

    @Query("delete from subject where id in (:ids)")
    fun deleteSubjectsInIdsSync(ids: List<String>)

    @Query("update subject set color = :color where id =:id")
    fun changeSubjectColorSync(id: String, color: Int)

    @Query("select id from subject where timetableId in (:ids)")
    fun getSubjectIdsOfTimetablesSync(ids: List<String>): List<String>

    @Delete
    fun deleteSubjectsSync(subjects: List<TermSubject>)

    @Query("delete from subject")
    fun clear()
}