package view;

import java.util.List;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import model.Individual;

public class SudokuFrame extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private JButton btnSolve, btnGenerate, btnReset; // Thay btnClear thành btnReset
    private JLabel lblStatus;

    public SudokuFrame() {
        setTitle("Sudoku");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Bàn cờ
        JPanel pnlBoard = new JPanel(new GridLayout(9, 9));
        pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font font = new Font("Arial", Font.BOLD, 20);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(font);

                // Tạo viền đậm cho khối 3x3
                int top = (i % 3 == 0) ? 2 : 1;
                int left = (j % 3 == 0) ? 2 : 1;
                int bottom = (i == 8) ? 2 : 1;
                int right = (j == 8) ? 2 : 1;
                
                cells[i][j].setBorder(new MatteBorder(top, left, bottom, right, Color.BLACK));
                pnlBoard.add(cells[i][j]);
            }
        }
        add(pnlBoard, BorderLayout.CENTER);

        // Panel điều khiển
        JPanel pnlControl = new JPanel();
        btnGenerate = new JButton("Tạo Mới");
        btnReset = new JButton("Làm Mới"); // Cập nhật Text nút
        btnSolve = new JButton("GIẢI");
        lblStatus = new JLabel("Sẵn sàng!");

        pnlControl.add(btnGenerate);
        pnlControl.add(btnReset); // Add nút Làm Mới
        pnlControl.add(btnSolve);
        pnlControl.add(lblStatus);

        add(pnlControl, BorderLayout.SOUTH);
    }

    // Lấy dữ liệu từ giao diện ra mảng int[][]
    public int[][] getBoardData() {
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                try {
                    board[i][j] = text.isEmpty() ? 0 : Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    board[i][j] = 0; // Nếu nhập chữ thì coi như là 0
                }
            }
        }
        return board;
    }

    // Hiển thị một đề bài lên giao diện (đáp ứng isFixedCell)
    public void setBoardData(int[][] board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                    cells[i][j].setEditable(false); // Số đề bài thì không được sửa (isFixedCell = true)
                    cells[i][j].setForeground(Color.BLUE);
                    cells[i][j].setBackground(new Color(230, 230, 230));
                } else {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true); // Ô trống cho phép nhập (isFixedCell = false)
                    cells[i][j].setForeground(Color.BLACK);
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    // Cập nhật kết quả từ thuật toán
    public void updateBoardFromIndividual(Individual ind) {
        // Individual chứa List<Gene>, mỗi Gene là một hàng
        for (int i = 0; i < 9; i++) {
            List<Integer> rowData = ind.getGenes().get(i).getNumber();
            for (int j = 0; j < 9; j++) {
                // Chỉ cập nhật các ô màu đen (ô giải), giữ nguyên ô màu xanh (ô đề)
                if (cells[i][j].isEditable()) {
                    cells[i][j].setText(String.valueOf(rowData.get(j)));
                }
            }
        }
    }

    // (Tuỳ chọn: Nếu không cần chức năng Xóa Trắng bảng hoàn toàn nữa thì có thể bỏ hàm clearBoard() này)
    public void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText("");
                cells[i][j].setEditable(true);
                cells[i][j].setForeground(Color.BLACK);
                cells[i][j].setBackground(Color.WHITE);
            }
        }
    }

    public void updateStatus(String msg) {
        lblStatus.setText(msg);
    }
    
    // Getter cho các nút để Controller bắt sự kiện
    public JButton getBtnSolve() { return btnSolve; }
    public JButton getBtnGenerate() { return btnGenerate; }
    public JButton getBtnReset() { return btnReset; } // Cập nhật Getter
}