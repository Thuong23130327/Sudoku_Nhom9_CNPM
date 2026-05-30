package com.sudoku.utils;

import javax.swing.*;

public class TimerUtils {
    private Timer timer;
    private int secondsElapsed;
    private JLabel labelToUpdate;

    public TimerUtils(JLabel labelToUpdate) {
        this.labelToUpdate = labelToUpdate;
        this.secondsElapsed = 0;

        // Khởi tạo Swing Timer chạy mỗi 1000ms (1 giây)
        this.timer = new Timer(1000, e -> {
            secondsElapsed++;
            updateLabel();
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void reset() {
        timer.stop();
        secondsElapsed = 0;
        updateLabel();
    }

    private void updateLabel() {
        int mins = secondsElapsed / 60;
        int secs = secondsElapsed % 60;
        labelToUpdate.setText(String.format("Thời gian: %02d:%02d", mins, secs));
    }

    public String getTimeString() {
        return labelToUpdate.getText();
    }
}

