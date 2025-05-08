package com.example.puzzlealarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puzzlealarm.model.PuzzleType;

import java.sql.Time;
import java.util.logging.StreamHandler;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Spinner spinner;
    private Switch switchEnabled;
    private Button saveAlarm;

    private String mode = "add";
    private int alarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_alarm);
        timePicker = findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        spinner = findViewById(R.id.puzzle_choose);
        switchEnabled = findViewById(R.id.switch_enabled);
        saveAlarm = findViewById(R.id.btnSave);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Математика", "По порядку"});

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Intent intent = getIntent();
        saveAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, min;
                hour = timePicker.getHour();
                min = timePicker.getMinute();

                PuzzleType puzzleType = spinner.getSelectedItemPosition() == 0 ? PuzzleType.MATHEMATICS : PuzzleType.ORDER_PUZZLE;
                boolean enabled = switchEnabled.isChecked();

                Intent data = new Intent();
                data.putExtra("hour", hour);
                data.putExtra("min", min);
                data.putExtra("puzzle_type", puzzleType.name());
                data.putExtra("enabled", enabled);

                if ("edit".equals(mode)) {
                    data.putExtra("id", alarmId);
                }

                Log.d("AddAlarmActivity", data.toString());
                Log.d("AddAlarmActivity", "hour = " + data.getIntExtra("hour", -1));
                Log.d("AddAlarmActivity", "min = " + data.getIntExtra("min", -1));
                Log.d("AddAlarmActivity", "puzzle_type = " + data.getStringExtra("puzzle_type"));
                Log.d("AddAlarmActivity", "enabled = " + data.getBooleanExtra("enabled", false));

                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}