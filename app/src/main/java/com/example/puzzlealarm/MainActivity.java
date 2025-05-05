package com.example.puzzlealarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.puzzlealarm.adapter.AlarmAdapter;
import com.example.puzzlealarm.model.Alarm;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnTapListener{

    private TextView empty;
    private AlarmAdapter adapter;
    private static final int REQUEST_CODE_ADD_ALARM = 1;
    private static final int REQUEST_CODE_EDIT_ALARM = 2;
    private static final int PERMISSION_REQUEST_CODE_NOTIFICATIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewAlarms);
        empty = findViewById(R.id.empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlarmAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.add_alarm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        AddAlarmActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_ALARM);
            }
        });
    }

    @Override
    public void onClick(Alarm alarm) {
    }

    @Override
    public void onHold(Alarm alarm) {
    }
}

// TODO: написать метод для загрузки будильников из БД
// TODO: написать проверки для андроида
// TODO: переопределить нажатия по карточкам с будильниками
// TODO: обработать результаты из NewAlarmActivity
// TODO: PendingIntent