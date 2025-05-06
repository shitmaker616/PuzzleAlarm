package com.example.puzzlealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.example.puzzlealarm.adapter.AlarmAdapter;
import com.example.puzzlealarm.data.AlarmDataBase;
import com.example.puzzlealarm.model.Alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnTapListener{

    private TextView empty;
    private AlarmAdapter adapter;
    private static final int REQUEST_CODE_ADD_ALARM = 1;
    private static final int REQUEST_CODE_EDIT_ALARM = 2;
    private static final int PERMISSION_REQUEST_CODE_NOTIFICATIONS = 10;
    private List<Alarm> realAlarmsList = new ArrayList<>();

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

    private void updateAlarms(List<Alarm> alarms){
        for (Alarm alarm : alarms){
            if (alarm.isOn()){
                scheduleAlarm(this, alarm); //установление сигнала
            } else {
                cancelAlarm(this, alarm); //убираем сигнал
            }
        }
    }

    //метод для расписания будильников


    //отмена будильника
    private void cancelAlarm(Context context, Alarm alarm){
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createAlarmPendingIntent(context, alarm);
        manager.cancel(pendingIntent);
    }

    private PendingIntent createAlarmPendingIntent(Context context, Alarm alarm){
        Intent intent = new Intent(context, WakeUpReceiver.class);
        int requestCode = alarm.getId();
        intent.putExtra("id", requestCode);

        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void loadAlarms(){
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Alarm> alarms = AlarmDataBase.getInstance(getApplicationContext())
                    .alarmDao().getAll();
            realAlarmsList = alarms;

            // обновление интерфейса
            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.setAlarms(alarms);
                //TODO: дописать обновление UI
            });
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