package com.sudoku.view;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sudoku.model.GameMatch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryFrame() {
        setTitle("Lịch Sử Các Lượt Chơi Sudoku");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chỉ đóng cửa sổ này, không tắt app chính
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Định nghĩa các cột theo yêu cầu của bạn
        String[] columns = {"STT", "Ngày Chơi", "Cấp Độ", "Thời Gian Chơi", "Kết Quả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho người chơi sửa trực tiếp trên bảng lịch sử
            }
        };

        table = new JTable(tableModel);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        loadHistoryFromJson();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadHistoryFromJson() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("history.json")) {
            java.lang.reflect.Type listType = new TypeToken<ArrayList<GameMatch>>(){}.getType();
            List<GameMatch> historyList = gson.fromJson(reader, listType);

            if (historyList != null) {
                int stt = 1;
                for (GameMatch match : historyList) {
                    // Định dạng lại giây thành mm:ss
                    String timeFormatted = String.format("%02d:%02d", match.getDuration() / 60, match.getDuration() % 60);

                    Object[] rowData = {
                            stt++,
                            match.getDate(),
                            match.getLevel(),
                            timeFormatted,
                            match.getOutcome()
                    };
                    tableModel.addRow(rowData);
                }
            }
        } catch (IOException e) {
            // Nếu chưa có file history.json (chưa có trận nào), bảng sẽ trống trống trải.
        }
    }
}
