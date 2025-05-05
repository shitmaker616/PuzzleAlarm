package com.example.puzzlealarm.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.puzzlealarm.dao.AlarmDao;
import com.example.puzzlealarm.model.Alarm;
import com.example.puzzlealarm.model.PuzzleTypeConverter;

@Database(entities = {Alarm.class}, version = 1)
@TypeConverters({PuzzleTypeConverter.class})
public abstract class AlarmDataBase extends RoomDatabase {
    private static volatile AlarmDataBase INSTANCE;
    public abstract AlarmDao alarmDao();

    public static AlarmDataBase getInstance(Context context) {
        if (INSTANCE == null){
            synchronized (AlarmDataBase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AlarmDataBase.class, "alarm_database")
                            .fallbackToDestructiveMigration().build();
                }
            }

        }
        return  INSTANCE;
    }
}
