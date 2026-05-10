package com.sudoku.controller;

import javax.swing.*;

import com.sudoku.model.SudokuEngine;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.utils.TimerUtils;
import com.sudoku.view.SudokuFrame;

public class SudokuController {
    private SudokuFrame view;
    private SudokuEngine engine;
    private SudokuGenerator generator;
    private int[][] currentMatrix;

    private TimerUtils gameTimer;
    private boolean isPaused = false;

    private int mistakeCount = 0;
    private final int MAX_MISTAKES = 3;

    // Biến dùng để theo dõi trạng thái đang chạy hay đang dừng
    private boolean isRunning = false;

    private int hintCount = 0;
    private final int MAX_HINT = 3;

    public SudokuController(SudokuFrame view) {
        this.view = view;

        // Khởi tạo các thành phần Model
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();

        this.gameTimer = new TimerUtils(view.getLblTimer());
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
            hintCount = 0; // Reset biến đếm về 0 (đã dùng 0 lượt)
            view.updateHintUI(MAX_HINT, MAX_HINT); // Hiển thị lại 3/3

            hintCount = 0;
            mistakeCount = 0; // Quan trọng: Reset số lỗi
            view.updateHintUI(MAX_HINT, MAX_HINT);
            view.updateMistakeUI(0, MAX_MISTAKES);
            view.updateStatus("Ván mới bắt đầu!");

            // BẮT BUỘC: Chạy Engine ngầm để có Solution ngay từ đầu
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    engine.solve(currentMatrix); // Giải dựa trên đề bài mới tạo
                    return null;
                }
            }.execute();
            // UR-5.1: Bắt đầu bộ đếm thời gian
            gameTimer.reset();
            gameTimer.start();


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
        // UR-5.2 & UR-5.3: Xử lý Tạm dừng/Tiếp tục
        view.getBtnPause().addActionListener(e -> {
            if (isPaused) {
                gameTimer.start();
                view.setCellsVisible(true);
                view.getBtnPause().setText("Tạm dừng");
            } else {
                gameTimer.stop();
                view.setCellsVisible(false);
                view.getBtnPause().setText("Tiếp tục");
            }
            isPaused = !isPaused;
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

                                // (UR-5.4) - Để tính số lỗi Mistakes
                                handleUserInput(row, col);
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
            view.updateStatus("Bạn đã hết lượt gợi ý!");
            view.updateHintUI(0, MAX_HINT);
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

//        int[][] solution = engine.getSolution();
//
//        if (solution == null) {
//
//            view.updateStatus("Chưa có đáp án!");
//
//            return;
//        }
//        // Điền giá trị và cập nhật
//        int correctValue = solution[row][col];
//        board[row][col] = correctValue;
//        view.setCellValue(row, col, correctValue);
//
//        // Tăng số lần đã sử dụng Hint
//        hintCount++;
//        int remaining = MAX_HINT - hintCount;
//        view.updateHintUI(remaining, MAX_HINT);
//        // [UR-4.2]
//        // Hiển thị số lượt Hint đã dùng
//        view.updateStatus("Đã dùng gợi ý cho ô [" + (row+1) + "," + (col+1) + "]");

        final boolean[] hintProcessed = {false};

        engine.setOnGenerationEvolved(ind -> {
            // Chỉ cần tìm được một kết quả tương đối chính xác (fitness >= 160)
            if (ind.getFitness() >= 160 && !hintProcessed[0]) {
                hintProcessed[0] = true;

                // Lấy giá trị tại ô đó từ kết quả của Engine
                int suggestedValue = ind.getGenes().get(row).getNumber().get(col);

                SwingUtilities.invokeLater(() -> {
                    // Điền giá trị gợi ý lên màn hình
                    view.setCellValue(row, col, suggestedValue);

                    // Cập nhật số lượt
                    hintCount++;
                    int remaining = MAX_HINT - hintCount;
                    view.updateHintUI(remaining, MAX_HINT);
                    // [UR-4.2]
                    // Hiển thị số lượt Hint đã dùng
                    view.updateStatus("Đã dùng gợi ý! Còn " + remaining + " lượt.");

                    // Dừng engine ngầm sau khi đã lấy được hint
                    engine.stop();
                });
            }
        });

        // Chạy Engine ngầm để tìm đáp án
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                engine.solve(board);
                return null;
            }
        }.execute();
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
        int[][] solution = engine.getSolution();

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
                if (solution != null && value != solution[i][j]) {
                    view.highlightErrorCell(i, j);
                    // Lưu ý: Logic cộng dồn mistakeCount nên để ở sự kiện gõ phím
                    // để tránh việc loop này cộng lỗi liên tục.
                }
            }
        }
    }
    //[UR-4.4]
//Refresh lại lỗi sau khi click ô
    public void refreshErrors() {

        checkBoardErrors();
    }

    private void handleUserInput(int row, int col) {
        int[][] board = view.getBoardData();
        int value = board[row][col];

        if (value == 0) return; // Ô trống thì không tính lỗi

        int[][] solution = engine.getSolution();
        if (solution == null) {
            // Nếu chưa có đáp án ngầm, có thể Engine đang giải, hãy nhắc người dùng
            return;
        }

        if (value != solution[row][col]) {
            mistakeCount++;
            view.updateMistakeUI(mistakeCount, MAX_MISTAKES);

            // Bạn có thể thêm thông báo status để người chơi biết
            view.updateStatus("Sai rồi! Ô [" + (row+1) + "," + (col+1) + "] phải là số khác.");

            if (mistakeCount >= MAX_MISTAKES) {
                gameOver(false); // UR-5.5: Thông báo Thua
            }
        } else {
            // Nếu đúng, kiểm tra xem đã điền hết bàn cờ chưa
            checkWinCondition(board, solution);
        }
    }
    private void checkWinCondition(int[][] currentBoard, int[][] solution) {
        if (solution == null) return;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Nếu có ô trống hoặc ô sai so với đáp án -> Chưa thắng
                if (currentBoard[i][j] == 0 || currentBoard[i][j] != solution[i][j]) {
                    return;
                }
            }
        }
        // Nếu vượt qua vòng lặp -> Tất cả các ô đều đúng
        gameOver(true);
    }
    private void gameOver(boolean won) {
        gameTimer.stop();
        String time = gameTimer.getTimeString();
        if (won) {
            JOptionPane.showMessageDialog(view, "CHÚC MỪNG! Bạn đã thắng!\n" + time);
        } else {
            JOptionPane.showMessageDialog(view, "GAME OVER! Bạn đã phạm 3 lỗi.\n" + time);
            view.getBtnGenerate().doClick(); // Tự động tạo ván mới
        }
    }
}