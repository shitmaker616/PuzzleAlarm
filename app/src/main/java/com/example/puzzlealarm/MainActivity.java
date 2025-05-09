package com.example.puzzlealarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.example.puzzlealarm.adapter.AlarmAdapter;
import com.example.puzzlealarm.data.AlarmDataBase;
import com.example.puzzlealarm.model.Alarm;
import com.example.puzzlealarm.model.PuzzleType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnTapListener {

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

        checkExactAlarmPermission(); // Проверка разрешения на точные сигналы
        requestNotificationPermissionIfNeeded(); // Разрешение на отправку уведомлений
        checkIgnoreBatteryOptimization(); // Предложение снять оптимизацию батареи

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

        loadAlarms();
    }

    // Проверка разрешения на постановку точных сигналов (API 31+)
    private void checkExactAlarmPermission() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (!manager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Для работы будильников необходимо разрешение!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Запрашивает разрешение на уведомления для Android 13+
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE_NOTIFICATIONS
                );
            }
        }
    }

    // Предлагает пользователю исключить приложение из оптимизации батареи
    private void checkIgnoreBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        String packageName = getPackageName();
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            // Диалог с предложением внести в исключения
            new AlertDialog.Builder(this)
                    .setTitle("Снять ограничение по батарее")
                    .setMessage("Для корректной работы будильников добавьте приложение в исключения энергосбережения. Иначе сигналы могут не сработать после закрытия приложения.")
                    .setPositiveButton("Снять ограничение", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace(); // Может не поддерживаться
                            }
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }

    @Override
    public void onClick(Alarm alarm) {
        Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("id", alarm.getId());
        intent.putExtra("hour", alarm.getHour());
        intent.putExtra("min", alarm.getMin());
        intent.putExtra("puzzle_type", alarm.getPuzzleType().name());
        intent.putExtra("enabled", alarm.isOn());
        intent.putExtra("ringtoneUri", alarm.getRingtoneUri());

        startActivityForResult(intent, REQUEST_CODE_EDIT_ALARM);

    }

    @Override
    public void onHold(Alarm alarm) {
        new AlertDialog.Builder(this).setTitle("Удалить будильник?")
                .setMessage("Вы уверены, что хотите удалить будильник?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            cancelAlarm(MainActivity.this, alarm);
                            AlarmDataBase.getInstance(getApplicationContext()).alarmDao()
                                    .delete(alarm);
                            loadAlarms();
                        });
                    }
                })
                .setNegativeButton("Отмена", null).show();
    }

    private void updateAlarms(List<Alarm> alarms) {
        for (Alarm alarm : alarms) {
            if (alarm.isOn()) {
                scheduleAlarm(this, alarm); //установление сигнала
            } else {
                cancelAlarm(this, alarm); //убираем сигнал
            }
        }
    }

    //метод для расписания будильников
    private void scheduleAlarm(Context context, Alarm alarm) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createAlarmPendingIntent(context, alarm);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMin());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();
        if (triggerTime < System.currentTimeMillis()) {
            //переносим будильник на завтра
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            triggerTime = calendar.getTimeInMillis();
        }

        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    //отмена будильника
    private void cancelAlarm(Context context, Alarm alarm) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createAlarmPendingIntent(context, alarm);
        manager.cancel(pendingIntent);
    }

    private PendingIntent createAlarmPendingIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, WakeUpReceiver.class);
        int requestCode = alarm.getId();
        intent.putExtra("id", requestCode);

        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void loadAlarms() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Alarm> alarms = AlarmDataBase.getInstance(getApplicationContext())
                    .alarmDao().getAll();
            realAlarmsList = alarms;

            // обновление интерфейса
            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.setAlarms(alarms);
                updateAlarms(alarms);

                if (alarms.isEmpty()) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_ADD_ALARM || requestCode == REQUEST_CODE_EDIT_ALARM)
                && resultCode == RESULT_OK && data != null) {
            int hour = data.getIntExtra("hour", 0);
            int min = data.getIntExtra("min", 0);
            boolean enabled = data.getBooleanExtra("enabled", true);
            String puzzleTypeStr = data.getStringExtra("puzzle_type");
            PuzzleType puzzleType = PuzzleType.valueOf(puzzleTypeStr);
            String ringtoneUri = data.getStringExtra("ringtoneUri");

            if (requestCode == REQUEST_CODE_ADD_ALARM) {
                Alarm newAlarm = new Alarm(hour, min, puzzleType, enabled);
                Executors.newSingleThreadExecutor().execute(() -> {
                    AlarmDataBase.getInstance(getApplicationContext()).alarmDao().insert(newAlarm);
                    loadAlarms();
                });
            } else if (requestCode == REQUEST_CODE_EDIT_ALARM) {
                int id = data.getIntExtra("id", -1);
                if (id != -1) {
                    Alarm updateAlarm = new Alarm(id, hour, min, puzzleType, enabled, ringtoneUri);
                    System.out.println(updateAlarm);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AlarmDataBase.getInstance(getApplicationContext()).alarmDao().update(updateAlarm);
                        loadAlarms();
                    });
                }
            }
        }
    }
}