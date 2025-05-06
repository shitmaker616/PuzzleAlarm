package com.example.puzzlealarm;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Time;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker picker;
    private Spinner spinner;
    private Switch switchEnabled;
    private Button saveAlarm;

    private String mode = "add";
    private int alarmId;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_alarm);
        picker.findViewById(R.id.time_picker);
        spinner.findViewById(R.id.puzzle_choose);
        switchEnabled.findViewById(R.id.switch_enabled);
        saveAlarm.findViewById(R.id.add_alarm_button);
    }
}