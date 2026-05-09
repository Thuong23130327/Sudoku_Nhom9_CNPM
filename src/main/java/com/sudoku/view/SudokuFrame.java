package com.sudoku.view;

import java.util.List;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.sudoku.model.Individual;

public class SudokuFrame extends JFrame {

    private JTextField[][] cells = new JTextField[9][9];

    private JButton btnSolve, btnGenerate, btnClear;
    private JButton btnHint;

    private JLabel lblStatus;

    // Lưu ô đang được chọn
    private int selectedRow = -1;
    private int selectedCol = -1;

    public SudokuFrame() {

        setTitle("Sudoku");

        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // BÀN CỜ
        JPanel pnlBoard = new JPanel(new GridLayout(9, 9));

        pnlBoard.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10));

        Font font = new Font("Arial", Font.BOLD, 20);

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                cells[i][j] = new JTextField();

                cells[i][j]
                        .setHorizontalAlignment(JTextField.CENTER);

                cells[i][j].setFont(font);

                // Lưu vị trí ô được click
                final int r = i;
                final int c = j;

                // ===========================================================================
                // UR-2.1: Hệ thống phải cho phép người dùng chọn một ô trống trên lưới bằng chuột
                // ===========================================================================
                cells[i][j].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        selectedRow = r;
                        selectedCol = c;
                        // Ép ô này nhận Focus để kích hoạt FocusListener
                        cells[r][c].requestFocusInWindow();
                        highlightSameNumbers();
                    }
                });
                //Sự kiện khi người dùng chọn vào 1 ô thì ô đó sẽ hiện lên màu xanh lá cây để nhận biết:
                cells[i][j].addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        JTextField source = (JTextField)evt.getSource();
                        source.setBackground(new Color(0, 255, 0)); // Xanh lá cây khi chọn
                    }
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        JTextField source = (JTextField)evt.getSource();
                        // Trả lại màu cũ tùy theo ô đó là đề bài hay ô trống
                        if (!source.isEditable()) {
                            source.setBackground(new Color(230, 230, 230));
                        } else {
                            source.setBackground(Color.WHITE);
                        }
                    }
                });
                // =============================================================
                // UR-2.2: Hệ thống tiếp nhận giá trị nhập từ bàn phím. Chuyển đổi văn bản nhập vào thành số nguyên từ 1-9.
                // =============================================================
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        char charKey = e.getKeyChar();

                        // Chỉ cho phép nhập số 1-9
                        if (!((charKey >= '1') && (charKey <= '9'))) {
                            e.consume(); // Chặn ký tự lạ
                            return;
                        }

                        // Nếu ô đã có nội dung, ghi đè nội dung mới thay vì nối thêm. Giúp ô Sudoku luôn chỉ có 1 chữ số
                        if (cells[r][c].getText().length() >= 1) {
                            cells[r][c].setText("");
                        }
                    }
                });
                cells[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectedRow = r; // Dùng r thay vì i
                        selectedCol = c; // Dùng c thay vì j
                        cells[r][c].requestFocusInWindow();
                        highlightSameNumbers();
                    }
                });
                // ===========================================================================
                // UR-2.5: Hệ thống phải hỗ trợ người dùng chuyển đổi giữa các ô bằng các phím mũi tên trên bàn phím.
                // ===========================================================================
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        int code = e.getKeyCode();
                        if (code == java.awt.event.KeyEvent.VK_UP ||
                                code == java.awt.event.KeyEvent.VK_DOWN ||
                                code == java.awt.event.KeyEvent.VK_LEFT ||
                                code == java.awt.event.KeyEvent.VK_RIGHT) {

                            moveFocus(r, c, code);
                        }
                    }
                });
                // Tạo viền đậm cho khối 3x3
                int top = (i % 3 == 0) ? 2 : 1;
                int left = (j % 3 == 0) ? 2 : 1;
                int bottom = (i == 8) ? 2 : 1;
                int right = (j == 8) ? 2 : 1;

                cells[i][j].setBorder(
                        new MatteBorder(
                                top,
                                left,
                                bottom,
                                right,
                                Color.BLACK));

                pnlBoard.add(cells[i][j]);
            }
        }

        add(pnlBoard, BorderLayout.CENTER);

        // PANEL ĐIỀU KHIỂN
        JPanel pnlControl = new JPanel();

        btnGenerate = new JButton("Tạo Mới");

        btnClear = new JButton("Tự Nhập / Xóa");

        btnHint = new JButton("HINT");

        btnSolve = new JButton("GIẢI");

        lblStatus = new JLabel("Sẵn sàng!");

        pnlControl.add(btnGenerate);
        pnlControl.add(btnClear);
        pnlControl.add(btnHint);
        pnlControl.add(btnSolve);
        pnlControl.add(lblStatus);

        add(pnlControl, BorderLayout.SOUTH);
    }

    //Hàm xử lý cho phép người chơi dùng phím mũi tên di chuyển giữa các ô
    public void moveFocus(int currentRow, int currentCol, int keyCode) {
        int nextR = currentRow;
        int nextC = currentCol;

        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_UP:
                nextR = (currentRow - 1 + 9) % 9; // Lên trên (vòng lại nếu ở biên)
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                nextR = (currentRow + 1) % 9;     // Xuống dưới
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                nextC = (currentCol - 1 + 9) % 9; // Sang trái
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                nextC = (currentCol + 1) % 9;     // Sang phải
                break;
            default:
                return; // Nếu là phím khác thì không làm gì
        }

        cells[nextR][nextC].requestFocus(); // Chuyển con trỏ sang ô mới
    }

    // Lấy dữ liệu từ giao diện ra mảng int[][]
    public int[][] getBoardData() {

        int[][] board = new int[9][9];

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                String text = cells[i][j].getText();

                try {
                    // =============================================================
                    // UR-2.2: Hệ thống tiếp nhận giá trị nhập từ bàn phím. Chuyển đổi văn bản nhập vào thành số nguyên từ 1-9.
                    // =============================================================
                    int value = Integer.parseInt(text);
                    if (value >= 1 && value <= 9) {
                        board[i][j] = value;
                    } else {
                        board[i][j] = 0; // Không chấp nhận số ngoài khoảng 1-9
                    }
                } catch (NumberFormatException e) {

                    // Nếu nhập chữ thì coi như là 0
                    board[i][j] = 0;
                }
            }
        }

        return board;
    }

    // Hiển thị đề bài lên giao diện
    public void setBoardData(int[][] board) {

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                if (board[i][j] != 0) {

                    cells[i][j]
                            .setText(String.valueOf(board[i][j]));

                    // =================================================================
                    // UR-2.4: Hệ thống ngăn chặn việc chỉnh sửa/xóa ô thuộc đề bài gốc
                    // =================================================================

                    // Con số nằm trong ô thuộc đề bài gốc thì không được phép thay đổi:
                    cells[i][j].setEditable(false);

                    cells[i][j].setForeground(Color.BLUE);

                    cells[i][j]
                            .setBackground(new Color(230, 230, 230));

                } else {
                    // =================================================================
                    // UR-2.3: Cho phép người dùng nhập và xóa giá trị (Backspace/Delete) thông qua việc thiết lập quyền chỉnh sửa cho ô trống.
                    // =================================================================
                    cells[i][j].setText("");

                    cells[i][j].setEditable(true);

                    cells[i][j].setForeground(Color.BLACK);

                    cells[i][j].setBackground(Color.WHITE);

                }
            }
        }
    }

    // Cập nhật kết quả từ thuật toán
    public void updateBoardFromIndividual(Individual ind) {

        // Individual chứa List<Gene>,
        // mỗi Gene là một hàng

        for (int i = 0; i < 9; i++) {

            List<Integer> rowData =
                    ind.getGenes().get(i).getNumber();

            for (int j = 0; j < 9; j++) {

                // Chỉ cập nhật các ô editable
                if (cells[i][j].isEditable()) {

                    cells[i][j]
                            .setText(String.valueOf(rowData.get(j)));
                }
            }
        }
    }

    // Xóa board
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

    // Update status
    public void updateStatus(String msg) {

        lblStatus.setText(msg);
    }
    // [UR-4.3]
    // Highlight các ô cùng số
    public void highlightSameNumbers() {

        // Reset màu trước
        resetCellColors();

        // Kiểm tra đã chọn ô chưa
        if (selectedRow == -1 || selectedCol == -1) {
            return;
        }

        String value =
                cells[selectedRow][selectedCol].getText();

        // Nếu ô trống thì không highlight
        if (value.isEmpty()) {
            return;
        }

        // Highlight các ô cùng số
        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                // Nếu là ô đang focus thì không đè màu vàng lên
                if (cells[i][j].isFocusOwner()) continue;

                if (cells[i][j].getText().equals(value)) {
                    cells[i][j].setBackground(new Color(255, 255, 150)); // Màu vàng highlight
                }
            }
        }

        // Highlight ô đang chọn đậm hơn
        cells[selectedRow][selectedCol]
                .setBackground(
                        new Color(255, 220, 100));
    }

    // Reset màu ô
    public void resetCellColors() {

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {
                // Nếu ô này đang có focus thì KHÔNG reset màu (để giữ màu xanh)
                if (cells[i][j].isFocusOwner()) {
                    continue;
                }

                if (cells[i][j].isEditable()) {
                    cells[i][j].setBackground(Color.WHITE);
                } else {
                    cells[i][j].setBackground(new Color(230, 230, 230));
                }            }
        }
    }

    // Set giá trị cho 1 ô
    public void setCellValue(
            int row,
            int col,
            int value) {

        cells[row][col]
                .setText(String.valueOf(value));

        cells[row][col]
                .setForeground(Color.RED);

        cells[row][col]
                .setEditable(false);
        // [UR-4.4]
        // Highlight trực quan ô được hệ thống tác động
        cells[row][col]
                .setBackground(
                        new Color(255, 200, 200));
    }
    // [UR-4.4]
    // Highlight ô lỗi màu đỏ
    public void highlightErrorCell(
            int row,
            int col) {

        cells[row][col]
                .setBackground(
                        new Color(255, 120, 120));
    }
    // Getter ô đang chọn
    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    // Getter cho các nút
    public JButton getBtnSolve() {
        return btnSolve;
    }

    public JButton getBtnGenerate() {
        return btnGenerate;
    }

    public JButton getBtnClear() {
        return btnClear;
    }

    public JButton getBtnHint() {
        return btnHint;
    }
    public JTextField getCell(
            int row,
            int col) {

        return cells[row][col];
    }
}