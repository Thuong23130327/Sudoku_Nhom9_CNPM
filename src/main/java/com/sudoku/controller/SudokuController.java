package com.sudoku.controller;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import com.sudoku.model.SudokuEngine;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.view.SudokuFrame;

public class SudokuController {
    private SudokuFrame view;
    private SudokuEngine engine;
    private SudokuGenerator generator;
    private int[][] currentMatrix;

    // Biến dùng để theo dõi trạng thái đang chạy hay đang dừng
    private boolean isRunning = false;

    private int hintCount = 0;
    private final int MAX_HINT = 3;

    public SudokuController(SudokuFrame view) {
        this.view = view;

        // Khởi tạo các thành phần Model
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();

        // Gắn sự kiện cho các nút bấm
        initController();
    }

    private void initController() {
 // ==========================================================
        // UR-1.1: Xử lý nút "Tạo Mới" (Generate Game)
        // ==========================================================
        view.getBtnGenerate().addActionListener(e -> {
            if (isRunning) return; // Nếu đang giải thì không cho bấm
            
            // Xóa ngẫu nhiên 40 ô để tạo currentMatrix
            int[][] newBoard = generator.generate(40); 
            
            // Khởi tạo và lưu lại bản sao của đề bài vào currentMatrix
            currentMatrix = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(newBoard[i], 0, currentMatrix[i], 0, 9);
            }

            // setBoardData sẽ tự động cấu hình isFixedCell = true (setEditable = false)
            view.setBoardData(currentMatrix);
            view.updateStatus("Đã tạo mới (ẩn 40 ô). Nhấn Giải để bắt đầu.");
        });

        // ==========================================================
        // UR-1.2: Xử lý nút "Làm Mới" (Reset Game)
        // ==========================================================
        view.getBtnReset().addActionListener(e -> {
            if (isRunning) return;
            
            if (currentMatrix != null) {
                // Đưa mảng gốc vào lại, hàm setBoardData sẽ đè lại giao diện,
                // tự động xóa các ô người chơi đã nhập và giữ nguyên ô đề bài
                view.setBoardData(currentMatrix);
                view.updateStatus("Đã làm mới ván chơi về trạng thái ban đầu!");
            } else {
                view.updateStatus("Chưa có ván đấu nào để làm mới!");
            }
        });

   

        // 2. Xử lý nút "Tự Nhập / Xóa"
        view.getBtnClear().addActionListener(e -> {

            if (isRunning) return;

            view.clearBoard();

            // Reset hint
            hintCount = 0;

            view.updateStatus("Mời bạn nhập đề sudoku...");
        });

        // 3. Xử lý nút "GIẢI / DỪNG"
        view.getBtnSolve().addActionListener(e -> {

            if (isRunning) {

                // Nếu đang chạy -> Bấm để DỪNG
                stop();

            } else {

                // Nếu đang dừng -> Bấm để CHẠY
                start();
            }
        });

        // 4. Xử lý nút Hint hỗ trợ điền
        view.getBtnHint().addActionListener(e -> {

            if (isRunning) return;

            giveHint();
        });
        // [UR-4.4]
        // Kiểm tra lỗi realtime khi nhập
        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                final int row = i;
                final int col = j;

                view.getCell(row, col)
                        .addKeyListener(new java.awt.event.KeyAdapter() {

                            @Override
                            public void keyReleased(
                                    java.awt.event.KeyEvent e) {
                                // [UR-4.3]
                                // Highlight lại các ô cùng số
                                view.highlightSameNumbers();
                                // [UR-4.4]
                                // Kiểm tra lỗi Sudoku
                                checkBoardErrors();


                            }
                        });
            }
        }
    }

    private void start() {

        // BƯỚC 1: Lấy dữ liệu từ giao diện
        // (bao gồm cả số người dùng tự nhập)
        int[][] inputBoard = view.getBoardData();

        // BƯỚC 2: Format lại giao diện
        // Dòng này sẽ biến các số người dùng vừa nhập
        // thành màu XANH (Blue/Gray)
        // và KHÓA lại (setEditable=false)
        // giống như đề bài ngẫu nhiên.
        view.setBoardData(inputBoard);

        // BƯỚC 3: Cấu hình Engine
        // để cập nhật giao diện khi chạy
        engine.setOnGenerationEvolved(ind -> {

            SwingUtilities.invokeLater(() -> {

                view.updateBoardFromIndividual(ind);

                view.updateStatus(
                        "Fitness: "
                                + ind.getFitness()
                                + "/162 | Gen: đang chạy...");
            });
        });

        // Cập nhật trạng thái nút bấm
        isRunning = true;

        view.getBtnSolve().setText("DỪNG");

        view.getBtnGenerate().setEnabled(false);
        view.getBtnClear().setEnabled(false);

        // Disable Hint khi đang solve
        view.getBtnHint().setEnabled(false);

        // BƯỚC 4: Chạy thuật toán trên luồng riêng
        // (SwingWorker)
        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                // Hàm này sẽ chạy tốn thời gian
                engine.solve(inputBoard);

                return null;
            }

            @Override
            protected void done() {

                // Kết thúc:tìm thành công hoặc bị dừng
                stop();

                view.updateStatus("Thành công hoặc đã dừng!");
            }
        };

        worker.execute();
    }

    private void stop() {

        engine.stop(); // Gửi lệnh dừng vào Model

        isRunning = false;

        // Reset lại giao diện nút bấm
        view.getBtnSolve().setText("GIẢI (Start)");

        view.getBtnGenerate().setEnabled(true);
        view.getBtnClear().setEnabled(true);

        // Enable lại Hint
        view.getBtnHint().setEnabled(true);
    }

    private void giveHint() {
        // [UR-4.2]
        // Kiểm tra số lần sử dụng Hint đã đạt giới hạn chưa
        if (hintCount >= MAX_HINT) {
            view.updateStatus("Đã hết lượt Hint!");
            return;
        }
        // [UR-4.1]
        // Lấy vị trí ô mà người chơi đang chọn
        int row = view.getSelectedRow();
        int col = view.getSelectedCol();

        // Kiểm tra người chơi đã chọn ô chưa
        if (row == -1 || col == -1) {
            view.updateStatus("Hãy chọn 1 ô trống!");
            return;
        }
        int[][] board = view.getBoardData();

        if (board[row][col] != 0) {
            view.updateStatus("Ô này đã có số!");
            return;
        }
        int[][] solution = engine.getSolution();

        if (solution == null) {

            view.updateStatus("Chưa có đáp án!");

            return;
        }
        int correctValue = solution[row][col];
        board[row][col] = correctValue;
        view.setCellValue(row, col, correctValue);

        // Tăng số lần đã sử dụng Hint
        hintCount++;

        // [UR-4.2]
        // Hiển thị số lượt Hint đã dùng
        view.updateStatus(
                "Đã dùng Hint: "
                        + hintCount + "/" + MAX_HINT);
    }
    // [UR-4.4]
    // Kiểm tra ô có vi phạm luật Sudoku không
    private boolean isInvalid(
            int[][] board,
            int row,
            int col,
            int value) {

        // Check trùng hàng
        for (int j = 0; j < 9; j++) {

            if (j != col &&
                    board[row][j] == value) {

                return true;
            }
        }

        // Check trùng cột
        for (int i = 0; i < 9; i++) {

            if (i != row &&
                    board[i][col] == value) {

                return true;
            }
        }

        // Check block 3x3
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int i = startRow; i < startRow + 3; i++) {

            for (int j = startCol; j < startCol + 3; j++) {

                if ((i != row || j != col)
                        &&
                        board[i][j] == value) {

                    return true;
                }
            }
        }

        return false;
    }
    //[UR-4.4]
//Kiểm tra toàn bộ board và highlight lỗi
    private void checkBoardErrors() {

        int[][] board = view.getBoardData();


        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                int value = board[i][j];

                // Bỏ qua ô trống
                if (value == 0) {
                    continue;
                }

                // Check số không hợp lệ
                if (value < 1 || value > 9) {

                    view.highlightErrorCell(i, j);

                    continue;
                }

                // Check trùng Sudoku
                if (isInvalid(board, i, j, value)) {

                    view.highlightErrorCell(i, j);
                }
            }
        }
    }
    //[UR-4.4]
//Refresh lại lỗi sau khi click ô
    public void refreshErrors() {

        checkBoardErrors();
    }

}