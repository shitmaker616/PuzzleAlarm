package com.example.puzzlealarm.model;

import androidx.room.TypeConverter;

public class PuzzleTypeConverter {
    @TypeConverter
    public static String puzzleTypeToString(PuzzleType pt) {
        return pt == null ? null : pt.name();
    }

    @TypeConverter
    public static PuzzleType stringToPuzzleType(String value) {
        return value == null ? null : PuzzleType.valueOf(value);
    }
}
