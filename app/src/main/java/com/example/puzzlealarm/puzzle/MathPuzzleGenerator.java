package com.example.puzzlealarm.puzzle;

import java.util.Random;

public class MathPuzzleGenerator {
    private final Random random = new Random();

    private int correctAnswer = 0;
    private String question = "";

    public void generate(){
        int a = random.nextInt(50) + 2;
        int b = random.nextInt(50) + 2;
        int zn = random.nextInt(2);

        if (zn == 0){
            question = a + " + " + b + " = ?";
            correctAnswer = a + b;
        } else {
            if (a < b) {
                int ass = a; a = b; b = ass;
            }
            question = a + " - " + b + " = ?";
            correctAnswer = a - b;
        }
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public String getQuestion() {
        return question;
    }
}
