package com.example.puzzlealarm;

import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Query;

import com.example.puzzlealarm.data.AlarmDataBase;
import com.example.puzzlealarm.model.Alarm;
import com.example.puzzlealarm.model.PuzzleType;
import com.example.puzzlealarm.puzzle.MathPuzzleGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class WakeUpActivity extends AppCompatActivity {

    private MathPuzzleGenerator mpg;

    //переменные для задачки
    private TextView question;
    private EditText answer;
    private Button check;
    private TextView feedback;

    // переменные для от 1 до 16
    private GridLayout puzzlegrid;
    private TextView fifth_feedback;
    private Map<Integer, Integer> cellToNum;
    private static final int PUZZLE_SIZE = 16;
    private int nextNum;

    private Button btnDismiss;
    private MediaPlayer mediaPlayer;
    private int alarmId = -1;
    private Alarm alarmFromDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wake_up);

        alarmId = getIntent().getIntExtra("id", -1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnDismiss = findViewById(R.id.btnOtmena);
        btnDismiss.setVisibility(View.GONE);

        LinearLayout mathPuzzle = findViewById(R.id.math_puzzle_container);
        LinearLayout fifthPuzzle = findViewById(R.id.fifth_puzzle_container);

        if (alarmId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                alarmFromDb = AlarmDataBase.
                        getInstance(getApplicationContext())
                        .alarmDao()
                        .getById(alarmId);

                runOnUiThread(() -> {
                    PuzzleType type = (alarmFromDb != null
                            && alarmFromDb.getPuzzleType() != null)
                            ? alarmFromDb.getPuzzleType()
                            : PuzzleType.MATHEMATICS;
                    initPuzzleUI(type, mathPuzzle, fifthPuzzle);
                    loadAlarmAndPlayMusic();
                });
            });
        } else {
            initPuzzleUI(PuzzleType.MATHEMATICS, mathPuzzle, fifthPuzzle);
        }
        btnDismiss.setOnClickListener(v -> finish());
    }

    private void initPuzzleUI(PuzzleType type, LinearLayout mathContainer,
                              LinearLayout fifthContainer){
        if (type == PuzzleType.MATHEMATICS) {
            mathContainer.setVisibility(View.VISIBLE);
            fifthContainer.setVisibility(View.GONE);

            question = findViewById(R.id.task);
            answer = findViewById(R.id.puzzle_answer);
            check = findViewById(R.id.check);
            feedback = findViewById(R.id.puzzle_feedback);

            mpg = new MathPuzzleGenerator();
            mpg.generate();
            question.setText(mpg.getQuestion());
            feedback.setText("");

            check.setEnabled(true);
            answer.setEnabled(true);
            answer.setText("");

            check.setOnClickListener(v -> {
                int userAnswer;
                String userAnswerStr = answer.getText().toString().trim();
                if (userAnswerStr.isEmpty()) {
                    feedback.setText("Введите ответ");
                    return;
                }
                try{
                    userAnswer = Integer.parseInt(userAnswerStr);
                } catch (NumberFormatException e) {
                    feedback.setText("Некорректный формат, введите снова");
                    return;
                }

                if (userAnswer == mpg.getCorrectAnswer()) {
                    feedback.setText("Верно, можешь отключать будильник");
                    btnDismiss.setVisibility(View.VISIBLE);
                    check.setEnabled(false);
                    answer.setEnabled(false);
                } else {
                    feedback.setText("Неправильно.");
                }
            });
        } else {
            //прячем математику
            mathContainer.setVisibility(View.GONE);
            fifthContainer.setVisibility(View.VISIBLE);

            puzzlegrid = findViewById(R.id.puzzle_grid);
            fifth_feedback = findViewById(R.id.puzzle_feedback);
            nextNum = 1;

            setupPuzzleGrid();
        }
    }

    //настройки сетки
    private void setupPuzzleGrid() {
        List<Integer> shuffleNums = new ArrayList<>();
        for (int i = 1; i <= PUZZLE_SIZE; i++) {
            shuffleNums.add(i);
        }

        //пермешивание чисел
        Collections.shuffle(shuffleNums);

        //присваивание значения клетки по позиции
        cellToNum = new HashMap<>();
        for (int cell = 0; cell < PUZZLE_SIZE; cell++) {
            cellToNum.put(cell, shuffleNums.get(cell));
        }

        //размер сетки
        puzzlegrid.setColumnCount(4);
        puzzlegrid.setRowCount(4);

        //рисуем сетку
        drawGrid();
        fifth_feedback.setText("Нажимайте на числа по порядку от 1 до 16");
        btnDismiss.setVisibility(View.GONE);
    }

    private void drawGrid() {
        puzzlegrid.removeAllViews();
        puzzlegrid.setColumnCount(4);
        puzzlegrid.setRowCount(4);

        for (int cell = 0; cell < PUZZLE_SIZE; cell++) {
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(cell / 4, 1f);
            params.columnSpec = GridLayout.spec(cell % 4, 1f);
            params.setMargins(6, 6, 6, 6);

            Integer num = cellToNum.get(cell);
            if (num != null) {
                Button btn = new Button(this);
                btn.setText(String.valueOf(num));
                btn.setTextSize(20);
                btn.setAllCaps(false);
                btn.setTag(cell);
                btn.setBackgroundColor(0xFF444444);
                btn.setTextColor(0xFFFFFFFF);
                btn.setLayoutParams(params);

                btn.setOnClickListener(view -> {
                    int cellIdx = (int) view.getTag();
                    int value = cellToNum.get(cellIdx);
                    if (value == nextNum) {
                        cellToNum.remove(cellIdx);
                        nextNum++;

                        if (cellToNum.isEmpty()) {
                            fifth_feedback.setText("Победа! Теперь можно отключить будильник.");
                            btnDismiss.setVisibility(View.VISIBLE);
                        } else {
                            fifth_feedback.setText("Отлично! Теперь нажмите " + nextNum);
                        }
                        drawGrid();
                    } else {
                        fifth_feedback.setText("Сначала нажмите " + nextNum + "!");
                    }
                });

                puzzlegrid.addView(btn);
            } else {
                // Пустая ячейка при убранном числе — для сохранения внешнего вида
                View empty = new View(this);
                empty.setLayoutParams(params);
                puzzlegrid.addView(empty);
            }
        }
    }


    //для работы музыки
    private void loadAlarmAndPlayMusic() {
        if (alarmId == -1) {
            playDefaultAlarmSound();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            Alarm alarm = AlarmDataBase
                    .getInstance(getApplicationContext())
                    .alarmDao()
                    .getById(alarmId);

            new Handler(getMainLooper()).post(() -> {
                if (alarm != null && alarm.getRingtoneUri() != null
                        && !alarm.getRingtoneUri().isEmpty())  {
                    playAlarmSound(alarm.getRingtoneUri());
                } else {
                    playDefaultAlarmSound();
                }
            });
        });
    }

    private void playAlarmSound(String musicUri) {
        try {
            stopAlarmSound();
            mediaPlayer = MediaPlayer.create(this, Uri.parse(musicUri));

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } else {
                playDefaultAlarmSound();
            }
        } catch (Exception e) {
            playDefaultAlarmSound();
        }
    }

    private void playDefaultAlarmSound() {
        try {
            stopAlarmSound();
            Uri alarmUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            mediaPlayer = MediaPlayer.create(this, alarmUri);

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } else {
                playDefaultAlarmSound();
            }
        } catch (Exception e) {

        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy(){
        stopAlarmSound();
        super.onDestroy();
    }
}