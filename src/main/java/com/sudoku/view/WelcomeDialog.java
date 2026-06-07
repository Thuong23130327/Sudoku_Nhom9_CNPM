package com.sudoku.view;

import javax.swing.*;
import java.awt.*;

public class WelcomeDialog extends JDialog {
    private int selectedMissingDigits = -1;

    public WelcomeDialog(JFrame parent) {
        super(parent, "Sudoku Nhóm 9 - Chọn Độ Khó", true);
        // 1.3.1: Người chơi click nút [X] đóng hộp thoại (Sự kiện đóng cửa sổ gốc của JDialog sẽ kích hoạt mặc định, không chọn độ khó).
        setSize(480, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnlTitle = new JPanel();
        pnlTitle.setBackground(new Color(60, 140, 210));
        pnlTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel lblTitle = new JLabel("CHÀO MỪNG ĐẾN VỚI SUDOKU");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        pnlTitle.add(lblTitle);
        add(pnlTitle, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblDesc = new JLabel("Vui lòng chọn độ khó cho ván đấu mới:");
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setFont(new Font("Arial", Font.PLAIN, 16));
        pnlCenter.add(lblDesc);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton btnEasy = createRoundedButton("Dễ", new Color(76, 175, 80));
        JButton btnMedium = createRoundedButton("Trung Bình", new Color(255, 152, 0));
        JButton btnHard = createRoundedButton("Khó", new Color(244, 67, 54));

        // 1.1.4: Người chơi thực hiện hành động click() vào 1 trong 3 nút chọn độ khó trên hộp thoại.
        btnEasy.addActionListener(e -> {
            // 1.1.5: WelcomeDialog kích hoạt sự kiện nội bộ actionPerformed(ActionEvent e) để lưu tham số.
            selectedMissingDigits = 20; // 20 lỗ trống
            // 1.1.6: WelcomeDialog tự gọi hàm dispose() để đóng hộp thoại.
            dispose();
        });

        btnMedium.addActionListener(e -> {
            selectedMissingDigits = 35; // 35 lỗ trống
            dispose();
        });

        btnHard.addActionListener(e -> {
            selectedMissingDigits = 50; // 50 lỗ trống (Đảm bảo logic giải Backtracking vẫn mượt)
            dispose();
        });

        pnlButtons.add(btnEasy);
        pnlButtons.add(btnMedium);
        pnlButtons.add(btnHard);

        pnlCenter.add(pnlButtons);
        add(pnlCenter, BorderLayout.CENTER);
    }

    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Khử răng cưa cho viền mượt
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }
                // Vẽ HCN bo tròn góc (20px)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Ghi đè rỗng để ẩn viền hình chữ nhật mặc định
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); // Bắt buộc false để vẽ đè nền bo tròn
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 40));
        return btn;
    }

    public int getSelectedMissingDigits() {
        return selectedMissingDigits;
    }
}
