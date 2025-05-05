package com.example.puzzlealarm.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "alarms") //превращает класс в таблицу
@TypeConverters({PuzzleTypeConverter.class})
public class Alarm {
    @PrimaryKey(autoGenerate = true) //делает поле ниже главным ключом таблицы
    private int id;
    private int hour;
    private int min;
    private PuzzleType puzzleType;
    private boolean isOn;

    public Alarm(int id, int hour, int min, PuzzleType puzzleType, boolean isOn) {
        this.id = id;
        this.hour = hour;
        this.min = min;
        this.puzzleType = puzzleType;
        this.isOn = isOn;
    }

    public int getId() {
        return id;
    }


    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public PuzzleType getPuzzleType() {
        return puzzleType;
    }

    public void setPuzzleType(PuzzleType puzzleType) {
        this.puzzleType = puzzleType;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }
}
