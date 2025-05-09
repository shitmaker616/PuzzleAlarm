package com.example.puzzlealarm;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.puzzlealarm.model.PuzzleType;

import java.sql.Time;
import java.util.logging.StreamHandler;

public class AddAlarmActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_RINGTONE = 101;
    private TimePicker timePicker;
    private Spinner spinner;
    private Switch switchEnabled;
    private Button saveAlarm;
    private TextView musicName;
    private Button pickMusic;

    private String mode = "add";
    private int alarmId;
    private String ringtoneUri = null; // null - стандартная мелодия

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
        musicName = findViewById(R.id.music);
        pickMusic = findViewById(R.id.btnPickRingtone);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Математика", "Цифры от 1 до 16"});

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Intent editIntent = getIntent();

        //проверка, что мы в режиме редактирования
        if (editIntent != null && "edit".equals(editIntent.getStringExtra("mode"))) {
            System.out.println("Режим редактирования");
            mode = "edit";
            alarmId = editIntent.getIntExtra("id", -1);
            int hour = editIntent.getIntExtra("hour", 0);
            int min = editIntent.getIntExtra("min", 0);
            String puzzleTypeStr = editIntent.getStringExtra("puzzle_type");
            boolean enabled = editIntent.getBooleanExtra("enabled", true);
            ringtoneUri = editIntent.getStringExtra("ringtoneUri");


            //установка значения в AddAlarmActivity на основе будильника
            timePicker.setHour(hour);
            timePicker.setMinute(min);

            //головоломка
            if ("MATHEMATICS".equals(puzzleTypeStr)){
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }

            //включен ли будильник?
            switchEnabled.setChecked(enabled);

            updateMusicName(ringtoneUri);

            saveAlarm.setText("Сохранить изменения");
        }

        pickMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pickIntent.setType("audio/*");
                pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(pickIntent, REQUEST_CODE_PICK_RINGTONE);
            }
        });

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
                data.putExtra("ringtoneUri", ringtoneUri);

                if ("edit".equals(mode)) {
                    System.out.println("mode = " + mode);
                    data.putExtra("id", alarmId);
                }

                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_RINGTONE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ringtoneUri = uri.toString();
                updateMusicName(ringtoneUri);
            }
        }
    }

    private void updateMusicName(String uriStr){
        if (uriStr == null || uriStr.isEmpty()){
            musicName.setText("Мелодия: по умолчанию");
            return;
        }
        try {
            Uri uri = Uri.parse(uriStr);
            String name = queryDisplayName(uri);
            musicName.setText("Мелодия: " + (name != null ? name : uri.getLastPathSegment()));
        } catch (Exception e) {
            musicName.setText("Ошибка");
        }
    }

    /**
     * Получение названия файла по Uri (красиво выводить в UI).
     */
    private String queryDisplayName(Uri uri) {
        String displayName = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst() && nameIndex != -1) {
                    displayName = cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return displayName;
    }

}