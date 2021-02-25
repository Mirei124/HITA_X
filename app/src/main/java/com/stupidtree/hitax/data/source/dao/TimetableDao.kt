package com.stupidtree.hitax.data.source.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stupidtree.hitax.data.model.timetable.Timetable

@Dao
interface TimetableDao{


    /**
     * 根据教务代码查找课表
     */
    @Query("SELECT * FROM timetable WHERE code is :easCode")
    fun getTimetableByEASCode(easCode:String): Timetable?

    @Query("SELECT * FROM timetable order by -startTime")
    fun getTimetables(): LiveData<List<Timetable>>

    @Query("SELECT * FROM timetable WHERE id is :id")
    fun getTimetableById(id:String): LiveData<Timetable>

    /**
     *  获取所有使用默认名字的课表名字
     */
    @Query("SELECT name from timetable where name like :defaultName")
    fun getTimetableNamesWithDefaultSync(defaultName:String):List<String>

    /**
     * 保存课表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveTimetableSync(data:Timetable)

    @Delete
    fun deleteTimetablesSync(timetables:List<Timetable>)

}