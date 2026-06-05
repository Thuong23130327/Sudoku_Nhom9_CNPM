package com.sudoku.model;

/*
    UC-5.6: Xem lịch sử các lần chơi
    Class này dùng để tạo model đổ dữ liệu xuống file .json
    Người thực hiện: Nguyễn Thanh Tú
 */
public class GameMatch {
    private String date;
    private String level;
    private int duration; // Lưu bằng giây
    private String outcome; // "Thắng" hoặc "Thua (Bỏ cuộc)", "Thua (Quá lỗi)"

    public GameMatch(String date, String level, int duration, String outcome) {
        this.date = date;
        this.level = level;
        this.duration = duration;
        this.outcome = outcome;
    }

    // Getter phục vụ cho việc đọc dữ liệu đổ lên JTable
    public String getDate() { return date; }
    public String getLevel() { return level; }
    public int getDuration() { return duration; }
    public String getOutcome() { return outcome; }
}
