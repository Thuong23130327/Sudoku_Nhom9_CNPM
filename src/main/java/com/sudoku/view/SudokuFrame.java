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

    private JButton btnGenerate, btnReset;
    private JButton btnHint;
    private JButton btnUndo;
    private JButton btnValidate; // [3.1.5] Nút Kiểm tra toàn bảng (đã ẩn khỏi UI)
    private JButton btnShowSolution; // [3.2.1] Nút Xem giải pháp (Auto-Solver)
    private JLabel lblStatus, lblHintCount;
    private JLabel LblMistakes;
    private JComboBox<String> cblevel;
    private JLabel lblTimer , lblLevels;
    private JButton btnPause;

    private JToggleButton btnNote;

    private JLabel lblMistakes;

    private JButton btnHistory;

    //Mảng chứa 10 nút bấm của Bàn phím ảo (0-9)
    private JButton[] btnNumbers = new JButton[10];

    // Lưu ô đang được chọn
    private int selectedRow = -1;
    private int selectedCol = -1;

    public SudokuFrame() {
        setTitle("Sudoku");
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // BÀN CỜ
        JPanel pnlBoard = new JPanel(new GridLayout(9, 9));

        pnlBoard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
                        // Chỉ cho phép chọn ô trống (ô có thể chỉnh sửa)
                        if (!cells[r][c].isEditable()) {
                            return;
                        }

                        selectedRow = r;
                        selectedCol = c;
                        // Ép ô này nhận Focus để kích hoạt FocusListener
                        cells[r][c].requestFocusInWindow();
                        highlightSameNumbers();
                    }
                });
                //Sự kiện khi người dùng chọn vào 1 ô thì ô đó sẽ được highlight đồng bộ:
                cells[i][j].addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        if (!cells[r][c].isEditable()) {
                            return;
                        }
                        selectedRow = r;
                        selectedCol = c;
                        highlightSameNumbers();
                    }
                    public void focusLost(java.awt.event.FocusEvent evt) {
                        selectedRow = -1;
                        selectedCol = -1;
                        resetCellColors();
                    }
                });
                // =============================================================
                // UR-2.2: Hệ thống tiếp nhận giá trị nhập từ bàn phím. Chuyển đổi văn bản nhập vào thành số nguyên từ 1-9.
                // =============================================================
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        // Block input nếu game đang Pause (isEnabled = false) hoặc Game Over/Ô đề bài (isEditable = false)
                        if (!cells[r][c].isEditable() || !cells[r][c].isEnabled()) {
                            e.consume();
                            return;
                        }

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

                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        if (!cells[r][c].isEditable() || !cells[r][c].isEnabled()) {
                            return;
                        }
                        highlightSameNumbers();
                    }
                });
                // (Block MouseListener trùng lặp đã được gỡ bỏ — logic chọn ô nằm ở UR-2.1 phía trên)
                // ===========================================================================
                // UR-2.5: Hệ thống phải hỗ trợ người dùng chuyển đổi giữa các ô bằng các phím mũi tên trên bàn phím.
                // ===========================================================================
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        // Khóa điều hướng nếu game đang Pause
                        if (!cells[r][c].isEnabled()) {
                            return;
                        }
                        
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
        String[]levels = {"dễ", "trung bình","asian"};
        cblevel = new JComboBox<>(levels);
        btnGenerate = new JButton("Tạo Mới");
        btnUndo = new JButton("Hoàn Tác");
        btnReset = new JButton("Làm Mới");
        btnHint = new JButton("Gợi ý (Chọn 1 ô)");
        lblHintCount = new JLabel("Gợi ý: 3/3");
        lblHintCount.setForeground(new Color(0, 128, 0));
        btnValidate = new JButton("Kiểm Tra");
        btnShowSolution = new JButton("Xem Giải Pháp");
        lblStatus = new JLabel("Sẵn sàng!");
        lblLevels = new JLabel("Levels");
        btnHistory = new JButton("Xem Lịch Sử");

        //Time:

        lblTimer = new JLabel("Thời gian: 00:00");
        btnPause = new JButton("Tạm dừng");
        //Tính số lần sai:
        lblMistakes = new JLabel("Lỗi: 0/3");
        lblMistakes.setForeground(Color.RED);

        /*
            Thêm nút chức năng Ghi chú (Note) lên giao diện, thêm sự kiện khi người dùng nhấn vào nút Ghi chú,
            hiển thị trạng thái Bật/Tắt của chế độ Ghi chú
         */
        //Note ghi chú:
        btnNote = new JToggleButton("Ghi Chú: TẮT");
        btnNote.setFont(new Font("Arial", Font.BOLD, 12));
        btnNote.addActionListener(e -> {
            if (btnNote.isSelected()) {
                btnNote.setText("Ghi Chú: BẬT");
                btnNote.setBackground(Color.ORANGE); // Đổi màu để nhận biết đang bật mode note
            } else {
                btnNote.setText("Ghi Chú: TẮT");
                btnNote.setBackground(null);
            }
        });

        pnlControl.add(btnGenerate);
        pnlControl.add(btnReset);
        pnlControl.add(btnHint);
        pnlControl.add(lblHintCount);
        // pnlControl.add(btnValidate); // Bỏ nút Kiểm tra theo yêu cầu
        pnlControl.add(btnShowSolution);
        pnlControl.add(lblStatus);
        pnlControl.add(btnPause);
        pnlControl.add(lblTimer);
        pnlControl.add(lblMistakes);
        pnlControl.add(btnUndo);
        pnlControl.add(lblLevels);
        pnlControl.add(cblevel);
        pnlControl.add(btnHistory);
        pnlControl.add(btnNote);
        add(pnlControl, BorderLayout.SOUTH);
        JPanel pnlKeyboard = createVirtualKeyboard();
        add(pnlKeyboard, BorderLayout.EAST);

    }
    // Hàm tạo giao diện Bàn phím ảo giống Numpad
    private JPanel createVirtualKeyboard() {
        // Tạo panel với lưới 4 hàng x 3 cột (giống bàn phím điện thoại)
        JPanel pnlKeyboard = new JPanel(new GridLayout(4, 3, 5, 5));
        pnlKeyboard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Bàn phím"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Khởi tạo các nút từ 1 đến 9
        for (int i = 1; i <= 9; i++) {
            btnNumbers[i] = new JButton(String.valueOf(i));
            btnNumbers[i].setFont(new Font("Arial", Font.BOLD, 24));
            btnNumbers[i].setFocusable(false); // Không cướp focus của ô lưới
            pnlKeyboard.add(btnNumbers[i]);
        }

        // Hàng cuối cùng: Trống - Nút Xóa (0) - Trống
        pnlKeyboard.add(new JLabel("")); // Ô trống góc dưới trái

        btnNumbers[0] = new JButton("X");
        btnNumbers[0].setFont(new Font("Arial", Font.BOLD, 24));
        btnNumbers[0].setForeground(Color.RED);
        btnNumbers[0].setFocusable(false);
        pnlKeyboard.add(btnNumbers[0]);

        pnlKeyboard.add(new JLabel("")); // Ô trống góc dưới phải

        return pnlKeyboard;
    }

    // Hàm lấy nút bấm để Controller gọi sự kiện
    public JButton getVirtualButton(int number) {
        return btnNumbers[number];
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
                    // UC-2.3: Cho phép người dùng nhập và xóa giá trị (Backspace/Delete) thông qua việc thiết lập quyền chỉnh sửa cho ô trống.
                    // =================================================================
                    cells[i][j].setText("");

                    cells[i][j].setEditable(true);

                    cells[i][j].setForeground(Color.BLACK);

                    cells[i][j].setBackground(Color.WHITE);

                }
            }
        }
    }
       // Update uc3 cập nhập lại gái trị các ô sau khi xóa
    // Cập nhật kết quả từ thuật toán
    public void updateBoardFromIndividual(Individual ind) {
        // Individual chứa List<Gene>,
        // mỗi Gene là một hàng
        for (int i = 0; i < 9; i++) {
            List<Integer> rowData = ind.getGenes().get(i).getNumber();
            for (int j = 0; j < 9; j++) {
                // Chỉ cập nhật các ô editable
                if (cells[i][j].isEditable()) {
                    cells[i][j].setText(String.valueOf(rowData.get(j)));
                }
            }
        }
    }
     // Update UC3 hàm xóa board
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
    //Hàm cập nhật label hiển thị số lượt gợi ý
    public void updateHintUI(int remaining, int max) {
        lblHintCount.setText("Gợi ý: " + remaining + "/" + max);

        if (remaining <= 0) {
            btnHint.setEnabled(false);
            btnHint.setBackground(Color.RED);
            lblHintCount.setForeground(Color.RED); // Chữ label cũng chuyển đỏ khi hết lượt
        } else {
            btnHint.setEnabled(true);
            btnHint.setBackground(Color.GREEN);
            lblHintCount.setForeground(new Color(0, 128, 0));
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

        // Nếu ô trống thì chỉ highlight focus
        if (value.isEmpty()) {
            cells[selectedRow][selectedCol].setBackground(new Color(173, 216, 230)); // Xanh nhạt focus
            return;
        }

        // Highlight các ô cùng số
        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {
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
                if (cells[i][j].isEditable()) {
                    cells[i][j].setBackground(Color.WHITE);
                } else {
                    cells[i][j].setBackground(new Color(230, 230, 230));
                }
            }
        }
    }

    // Set giá trị cho 1 ô (Tối ưu cho cả Hint và Undo)

    public void setCellValue(int row, int col, int value) {
        if (value == 0) {
            cells[row][col].setText("");
            cells[row][col].setEditable(true);
            cells[row][col].setForeground(Color.BLACK);
            cells[row][col].setBackground(Color.WHITE);
        } else {
            cells[row][col].setText(String.valueOf(value));
            cells[row][col].setForeground(Color.RED);
            cells[row][col].setEditable(true); // Vẫn cho phép sửa nếu người chơi đổi ý gõ đè số khác
            cells[row][col].setBackground(new Color(255, 200, 200)); // Highlight ô hệ thống tác động
        }
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

    // [3.1.4] Highlight ô bị lỗi (sai luật Sudoku) — phản hồi trực quan cho người chơi
    public void highlightErrorCell(int row, int col, boolean isError) {
        if (isError) {
            cells[row][col].setForeground(Color.RED);
            cells[row][col].setBackground(new Color(255, 200, 200));
        } else {
            // Trả lại màu bình thường nếu không phải là đề bài
            if (cells[row][col].isEditable()) {
                cells[row][col].setForeground(Color.BLACK);
                cells[row][col].setBackground(Color.WHITE);
            } else {
                cells[row][col].setForeground(Color.BLUE);
                cells[row][col].setBackground(new Color(230, 230, 230));
            }
        }
    }

    // Hàm ẩn/hiện bàn cờ khi Pause (UR-5.2)
    public void setCellsVisible(boolean visible) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setVisible(visible);
            }
        }
    }


    //Hàm cập nhật số lần điền sai
    public void updateMistakeUI(int current, int max) {
        lblMistakes.setText("Lỗi: " + current + "/" + max);
    }


    /*
    UC-5.6: Xem lịch sử các lần chơi
    Hàm dùng để ẩn các nút chức năng trên giao diện khi đang "Tạm dừng"
    Nâng cấp cho phiên bản trước đó: vẫn hiển thị các nút chức năng khác như Tạo mới, Làm mới dẫn đến lỗi
    Người thực hiện: Nguyễn Thanh Tú
     */
    public void setGameplayButtonsEnabled(boolean enabled) {
        btnGenerate.setEnabled(enabled);
        btnReset.setEnabled(enabled);
        btnHint.setEnabled(enabled);
        btnUndo.setEnabled(enabled);
        btnShowSolution.setEnabled(enabled);
        cblevel.setEnabled(enabled);

        // Vô hiệu hóa hoặc kích hoạt tương tác trên các ô lưới
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setEnabled(enabled);
            }
        }
    }

    // Getter ô đang chọn
    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public String getSelectedLevel() {
        return (String) cblevel.getSelectedItem();
    }
    public JButton getBtnGenerate() {
        return btnGenerate;
    }

    public JButton getBtnReset() {
        return btnReset;
    }
    public JButton getBtnUndo(){
        return btnUndo;
    }

    public JButton getBtnHint() {
        return btnHint;
    }

    public JButton getBtnValidate() {
        return btnValidate;
    }

    public JButton getBtnShowSolution() {
        return btnShowSolution;
    }

    public JLabel getLblTimer() { return lblTimer; }
    public JButton getBtnPause() { return btnPause; }

    public JToggleButton getBtnNote() { return btnNote; }

    public JTextField getCell(
            int row,
            int col) {

        return cells[row][col];
    }

    public JButton getBtnHistory() { return btnHistory; }


}