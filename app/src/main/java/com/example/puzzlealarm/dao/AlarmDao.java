package com.example.puzzlealarm.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.puzzlealarm.model.Alarm;

import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    long insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarms ORDER BY hour, min") //взять всё из таблицы alarms
    List<Alarm> getAll();

    //берём 1 будильник
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    Alarm getById(int id);
}
