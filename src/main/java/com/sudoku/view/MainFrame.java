package com.sudoku.view;

import javax.swing.*;
import java.awt.*;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.controller.SudokuController;

public class MainFrame extends JFrame {

    private JButton btnSinglePlayer;
    private JButton btnTwoPlayers;

    public MainFrame() {
        setTitle("Sudoku Game - Menu Chính");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Tiêu đề Game
        JLabel lblTitle = new JLabel("SUDOKU TRÍ TUỆ", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(new Color(50, 50, 150));

        // Nút 1 người chơi
        btnSinglePlayer = new JButton("Chế độ 1 Người chơi");
        styleMenuButton(btnSinglePlayer);

        // Nút Đấu đối kháng 2 người
        btnTwoPlayers = new JButton("Chế độ 2 Người (Đối Kháng)");
        styleMenuButton(btnTwoPlayers);

        // Đặt các thành phần vào Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;

        gbc.gridy = 0;
        add(lblTitle, gbc);

        gbc.gridy = 1;
        add(btnSinglePlayer, gbc);

        gbc.gridy = 2;
        add(btnTwoPlayers, gbc);

        initEvents();
    }

    private void styleMenuButton(JButton btn) {
        btn.setPreferredSize(new Dimension(280, 45));
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void initEvents() {
        // chọn chế độ 1 người chơi
        btnSinglePlayer.addActionListener(e -> {
            // Ẩn Menu chính đi NGAY LẬP TỨC khi vừa click chọn chế độ
            this.setVisible(false);

            // Sau đó mới hiển thị hộp thoại chọn độ khó
            WelcomeDialog welcome = new WelcomeDialog(this);
            welcome.setVisible(true);

            int missingDigits = welcome.getSelectedMissingDigits();

            if (missingDigits != -1) {
                // Nếu chọn level hợp lệ, khởi tạo và vào game luôn
                SudokuFrame singleFrame = new SudokuFrame("Người chơi 1");
                SudokuController controller = new SudokuController(singleFrame);
                singleFrame.setLocationRelativeTo(null);
                singleFrame.setVisible(true);

                // Tạo bảng theo số lượng ô trống tương ứng với level đã chọn
                controller.generateBoardWithDifficulty(missingDigits);
            } else {
                // Nếu người chơi bấm nút Thoát/Hủy ở bảng chọn độ khó, hiển thị lại Menu chính
                this.setVisible(true);
            }
        });
        // chế dộ 2 người chơi
        btnTwoPlayers.addActionListener(e -> {
            // Ẩn Menu chính đi NGAY LẬP TỨC khi vừa click chọn chế độ
            this.setVisible(false);

            // Hiển thị hộp thoại chọn độ khó chung cho cả 2 bên
            WelcomeDialog welcome = new WelcomeDialog(this);
            welcome.setVisible(true);

            int missingDigits = welcome.getSelectedMissingDigits();

            if (missingDigits != -1) {
                // Khởi tạo Generator sinh duy nhất 1 bộ đề bài chung theo level được chọn
                SudokuGenerator sharedGenerator = new SudokuGenerator();
                int[][] sharedBoard = sharedGenerator.generate(missingDigits);
                int[][] sharedSolution = sharedGenerator.getSolution();

                // Khởi tạo 2 cửa sổ độc lập cho Player 1 và Player 2
                SudokuFrame p1Frame = new SudokuFrame("Người chơi 1");
                SudokuFrame p2Frame = new SudokuFrame("Người chơi 2");

                // Xếp 2 màn hình nằm song song Trái - Phải cạnh nhau để chơi chung một máy
                p1Frame.setLocation(40, 100);
                p2Frame.setLocation(1160, 100);

                // Gắn liên kết chéo quản lý thắng thua
                p1Frame.setOpponentFrame(p2Frame);
                p2Frame.setOpponentFrame(p1Frame);

                // Nạp chung dữ liệu đề bài đã sinh vào 2 bộ điều khiển
                SudokuController p1Controller = new SudokuController(p1Frame, sharedBoard, sharedSolution);
                SudokuController p2Controller = new SudokuController(p2Frame, sharedBoard, sharedSolution);

                // Bật hiển thị cả 2 cửa sổ game lên cùng lúc
                p1Frame.setVisible(true);
                p2Frame.setVisible(true);
            } else {
                // Nếu người chơi bấm nút Thoát/Hủy ở bảng chọn độ khó, hiển thị lại Menu chính
                this.setVisible(true);
            }
        });
    }
}