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

                // [2.1.1] Người chơi dùng chuột click chọn 1 ô trên giao diện bàn cờ. (View — SudokuFrame.mouseClicked())
                cells[i][j].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // [2.3.1] View kiểm tra JTextField.isEditable() — phát hiện ô thuộc đề bài gốc.
                        if (!cells[r][c].isEditable()) {
                            // [2.3.2] View phát hiện ô là Read-Only, không ghi nhận selectedRow/selectedCol. (SudokuFrame.mouseClicked → return)
                            return;
                        }

                        // [2.1.2] View tính toán tọa độ lưới (row, col), ghi nhận vào selectedRow, selectedCol.
                        selectedRow = r;
                        selectedCol = c;
                        // [2.1.3] View gọi JTextField.requestFocusInWindow() để ô nhận focus, kích hoạt FocusListener.
                        cells[r][c].requestFocusInWindow();
                        // [2.1.4] View gọi SudokuFrame.highlightSameNumbers() highlight tất cả ô cùng giá trị.
                        highlightSameNumbers();
                    }
                });
                // [2.2.4] FocusListener — khi moveFocus() gọi requestFocus() chuyển sang ô mới,
                //          FocusListener tự động cập nhật selectedRow/selectedCol và gọi highlightSameNumbers().
                cells[i][j].addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        // [2.3.1] Kiểm tra ô gốc — nếu Read-Only thì không ghi nhận.
                        if (!cells[r][c].isEditable()) {
                            return;
                        }
                        // [2.2.4] Cập nhật selectedRow/selectedCol và highlight ô mới.
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
                // [2.1.5] Người chơi gõ một số (1-9) hoặc nhấn phím Backspace/Delete. (View — SudokuFrame.keyTyped() / keyPressed())
                // [2.3.3] Người chơi thực hiện nhập phím số hoặc xóa vào ô này.
                // =============================================================
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {

                    @Override
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        // Block input nếu game đang Pause (isEnabled = false) hoặc Game Over/Ô đề bài (isEditable = false)
                        // [2.3.4] View nhận sự kiện nhưng kiểm tra trạng thái thấy ô hiện tại là ô gốc (Read-Only), từ chối thực hiện lệnh cập nhật.
                        // [2.3.5] Giá trị ô đề bài được giữ nguyên trên giao diện.
                        if (!cells[r][c].isEditable() || !cells[r][c].isEnabled()) {
                            e.consume();
                            return;
                        }

                        char charKey = e.getKeyChar();

                        // [2.4.1] Người chơi nhập phím không thuộc phạm vi 1-9 (ví dụ: chữ cái 'a', ký tự đặc biệt '@', phím số '0').
                        if (!((charKey >= '1') && (charKey <= '9'))) {
                            // [2.4.2] Ngay tại View, phương thức SudokuFrame.keyTyped() bắt sự kiện, nhận diện ký tự không hợp lệ và chặn lại bằng e.consume().
                            e.consume(); // Chặn ký tự lạ
                            return;
                        }

                        // [2.1.6] Nếu ô đã có nội dung, ghi đè nội dung mới thay vì nối thêm.
                        // (View — SudokuFrame.setText(""))
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
                // (Block MouseListener trùng lặp đã được gỡ bỏ — logic chọn ô nằm ở [2.1.1] phía trên)
                // ===========================================================================
                // [2.2.1] Người chơi nhấn một trong các phím mũi tên (Up, Down, Left, Right). (View — SudokuFrame.keyPressed())
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
                            // [2.2.2] View gửi KeyEvent để xử lý di chuyển bằng phím mũi tên.
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
        add(pnlControl, BorderLayout.SOUTH);
        JPanel pnlKeyboard = createVirtualKeyboard();
        add(pnlKeyboard, BorderLayout.EAST);

    }

    // [2.2.3] Tính toán tọa độ (nextR, nextC) của ô đích dựa trên phím mũi tên.
    // [2.5.1] Phép % 9 xử lý wrap around khi vượt biên (ví dụ: row 0 + UP → row 8).
    // HÀM TẠO GIAO DIỆN BÀN PHÍM ẢO
    private JPanel createVirtualKeyboard() {
        // Sử dụng BorderLayout để chia khu vực: phần Lưới số ở trên, nút Xóa ở dưới
        JPanel pnlKeyboard = new JPanel(new BorderLayout(10, 10)); // Khoảng cách 10px giữa các phần

        // Trang trí viền bao quanh bàn phím
        pnlKeyboard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true),
                        " Bàn Phím Ảo ",
                        0, 0,
                        new Font("Arial", Font.BOLD, 14),
                        new Color(80, 80, 80)
                ),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Fix kích thước để bàn phím không bị méo khi kéo giãn cửa sổ
        pnlKeyboard.setPreferredSize(new Dimension(280, 350));

        Font btnFont = new Font("Segoe UI", Font.BOLD, 26); // Font chữ hiện đại

        // 1. TẠO LƯỚI 3x3 CHỨA CÁC SỐ TỪ 1 ĐẾN 9
        JPanel pnlNumbers = new JPanel(new GridLayout(3, 3, 10, 10));
        for (int i = 1; i <= 9; i++) {
            btnNumbers[i] = new JButton(String.valueOf(i));
            styleVirtualButton(btnNumbers[i], btnFont, new Color(245, 245, 245), new Color(50, 50, 50));
            pnlNumbers.add(btnNumbers[i]);
        }

        // 2. TẠO NÚT XÓA CHIẾM FULL CHIỀU NGANG
        btnNumbers[0] = new JButton("Xóa"); // Thêm chữ Xóa cho nút dài nhìn cân đối hơn
        styleVirtualButton(btnNumbers[0], btnFont, new Color(255, 220, 220), new Color(200, 0, 0));

        // Cố định chiều cao cho nút Xóa để nó vuông vắn với lưới số ở trên (ví dụ: 65px)
        btnNumbers[0].setPreferredSize(new Dimension(0, 65));

        // 3. RÁP CÁC THÀNH PHẦN VÀO PANEL CHÍNH
        pnlKeyboard.add(pnlNumbers, BorderLayout.CENTER);
        pnlKeyboard.add(btnNumbers[0], BorderLayout.SOUTH);

        // Bọc thêm 1 panel trống bên ngoài để bàn phím luôn bám lên phía trên (chống bị giãn dài theo chiều dọc màn hình)
        JPanel pnlOuter = new JPanel(new BorderLayout());
        pnlOuter.add(pnlKeyboard, BorderLayout.NORTH);

        return pnlOuter;
    }
    // -------------------------------------------------------------------------
    // HÀM HỖ TRỢ TRANG TRÍ NÚT BẤM (CSS-like cho Java Swing)
    // -------------------------------------------------------------------------
    private void styleVirtualButton(JButton btn, Font font, Color bgColor, Color fgColor) {
        btn.setFont(font);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Làm tối màu nền đi một chút khi hover
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Trả lại màu cũ khi chuột dời đi
                btn.setBackground(bgColor);
            }
        });
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
                nextR = (currentRow - 1 + 9) % 9; // [2.5.1] Wrap around: hàng 0 → hàng 8
                break;
            case java.awt.event.KeyEvent.VK_DOWN:
                nextR = (currentRow + 1) % 9;     // [2.5.1] Wrap around: hàng 8 → hàng 0
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                nextC = (currentCol - 1 + 9) % 9; // [2.5.1] Wrap around: cột 0 → cột 8
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                nextC = (currentCol + 1) % 9;     // [2.5.1] Wrap around: cột 8 → cột 0
                break;
            default:
                return; // Phím khác — không xử lý
        }

        // [2.2.4] View gọi JTextField.requestFocus() chuyển focus sang ô mới.
        // [2.5.2] Focus tự động chuyển sang ô đầu/cuối hàng/cột nếu vượt biên (kết quả của 2.5.1).
        cells[nextR][nextC].requestFocus();
    }

    // Lấy dữ liệu từ giao diện ra mảng int[][]
    public int[][] getBoardData() {

        int[][] board = new int[9][9];

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                String text = cells[i][j].getText();

                try {
                    // View lấy giá trị tại ô (chuyển đổi văn bản nhập vào thành số nguyên từ 1-9).
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
        // 1.1.17: SudokuFrame lặp qua 81 ô, tự gọi hàm JTextField.setText() và
        // JTextField.setEditable(false/true) để render lại các ô trên màn hình.
        // 1.2.5: SudokuFrame lặp qua 81 ô và tự gọi hàm JTextField.setText(value) ghi đè lại dữ liệu mảng gốc.
        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                if (board[i][j] != 0) {

                    cells[i][j]
                            .setText(String.valueOf(board[i][j]));

                    // =================================================================
                    // Phát hiện tọa độ thuộc về ô đề bài gốc và thiết lập Read-Only.
                    // =================================================================

                    // Con số nằm trong ô thuộc đề bài gốc thì không được phép thay đổi:
                    cells[i][j].setEditable(false);

                    cells[i][j].setForeground(Color.BLUE);

                    cells[i][j]
                            .setBackground(getBaseColor(i, j, false));

                } else {
                    // =================================================================
                    // UC-2.3: Cho phép người dùng nhập và xóa giá trị (Backspace/Delete) thông qua việc thiết lập quyền chỉnh sửa cho ô trống.
                    // =================================================================
                    cells[i][j].setText("");

                    cells[i][j].setEditable(true);

                    cells[i][j].setForeground(Color.BLACK);

                    cells[i][j].setBackground(getBaseColor(i, j, true));

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
                cells[i][j].setBackground(getBaseColor(i, j, true));
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

    private Color getBaseColor(int r, int c, boolean isEditable) {
        boolean isDarkBlock = ((r / 3) + (c / 3)) % 2 != 0;
        if (isEditable) {
            // Ô người chơi nhập: Block sáng là Trắng, Block tối là Xanh dương nhạt rõ rệt
            return isDarkBlock ? new Color(220, 235, 255) : Color.WHITE;
        } else {
            // Ô đề bài (Read-only): Block sáng là Xám, Block tối là Xám xanh đậm hơn
            return isDarkBlock ? new Color(200, 215, 235) : new Color(230, 230, 230);
        }
    }

    // Reset màu ô
    public void resetCellColors() {

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {
                cells[i][j].setBackground(getBaseColor(i, j, cells[i][j].isEditable()));
            }
        }
    }

    // Set giá trị cho 1 ô (Tối ưu cho cả Hint và Undo)

    public void setCellValue(int row, int col, int value) {
        if (value == 0) {
            cells[row][col].setText("");
            cells[row][col].setEditable(true);
            cells[row][col].setForeground(Color.BLACK);
            cells[row][col].setBackground(getBaseColor(row, col, true));
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
            cells[row][col].setBackground(getBaseColor(row, col, cells[row][col].isEditable()));
            if (cells[row][col].isEditable()) {
                cells[row][col].setForeground(Color.BLACK);
            } else {
                cells[row][col].setForeground(Color.BLUE);
            }
        }
    }

    // ==========================================================
    // Yêu cầu hiển thị giá trị mới (dùng cho Auto-Solve/Hint, không dùng trong nhập tay)
    // ==========================================================
    public void updateCellDisplay(int row, int col, int value) {
        if (value == 0) {
            cells[row][col].setText("");
        } else {
            cells[row][col].setText(String.valueOf(value));
        }
    }

    // ==========================================================
    // Yêu cầu bỏ highlight ô cũ và chuyển sang ô mới.
    // ==========================================================
    public void changeSelectedCell(int oldRow, int oldCol, int newRow, int newCol) {
        cells[newRow][newCol].requestFocus();
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

    public JTextField getCell(
            int row,
            int col) {

        return cells[row][col];
    }

    public JButton getBtnHistory() { return btnHistory; }


}