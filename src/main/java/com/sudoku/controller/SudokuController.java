package com.sudoku.controller;

import com.sudoku.model.SudokuEngine;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.model.SudokuLogic;
import com.sudoku.utils.TimerUtils;
import com.sudoku.view.SudokuFrame;

import javax.swing.*;

public class SudokuController {
    private SudokuFrame view;
    private SudokuEngine engine;
    private SudokuGenerator generator;
    private SudokuLogic logic;
    private InputHandler inputHandler;
    private int[][] currentMatrix;

    private TimerUtils gameTimer;
    private boolean isPaused = false;

    private int mistakeCount = 0;
    private final int MAX_MISTAKES = 3;
    private GameController gameController; // UR-5: Quản lý trạng thái tập trung



    private int hintCount = 0;
    private final int MAX_HINT = 3;

    public SudokuController(SudokuFrame view) {
        this.view = view;

        // Khởi tạo các thành phần Model
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();
        this.logic = new SudokuLogic();

        this.gameTimer = new TimerUtils(view.getLblTimer());
        this.gameController = new GameController(3); // 3 = MAX_MISTAKES
        // [3.1.2] Khởi tạo InputHandler — validate realtime mỗi lần nhập số
        this.inputHandler = new InputHandler(view, logic);
        // Gắn sự kiện cho các nút bấm
        initController();
    }

    private void initController() {
        // ==========================================================
        // UR-1.1: Xử lý nút "Tạo Mới" (Generate Game)
        // ==========================================================
        view.getBtnGenerate().addActionListener(e -> {
            engine.stop();

            int[][] newBoard = generator.generate(40);
            currentMatrix = new int[9][9];
            for (int i = 0; i < 9; i++)
                System.arraycopy(newBoard[i], 0, currentMatrix[i], 0, 9);

            // Nạp ngay solution chuẩn vào engine từ đầu để tránh lỗi tô đỏ toàn bảng khi tiếp tục
            engine.setSolution(generator.getSolution());

            view.setBoardData(currentMatrix);
            hintCount = 0;
            view.updateHintUI(MAX_HINT, MAX_HINT);
            engine.setOnGenerationEvolved(null);

            // UR-5.1 + UR-5.4: Reset trạng thái game và số lỗi cho ván mới
            gameController.startGame();
            view.updateMistakeUI(0, gameController.getMaxMistakes());
            view.updateStatus("Ván mới bắt đầu!");

            // THÊM: Mở lại các nút phòng trường hợp ván trước đang pause/gameover
            view.getBtnPause().setEnabled(true);
            view.getBtnPause().setText("Tạm dừng");
            view.setCellsVisible(true);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    engine.solve(currentMatrix);
                    return null;
                }

                @Override
                protected void done() {
                    view.updateStatus("Ván chơi đã sẵn sàng!");
                }
            }.execute();

            // UR-5.1: Reset timer và bắt đầu đếm
            gameTimer.reset();
            gameTimer.start();
        });

        // ==========================================================
        // UR-1.2: Xử lý nút "Làm Mới" (Reset Game)
        // ==========================================================
        view.getBtnReset().addActionListener(e -> {

            if (currentMatrix != null) {
                // Đưa mảng gốc vào lại, hàm setBoardData sẽ đè lại giao diện,
                // tự động xóa các ô người chơi đã nhập và giữ nguyên ô đề bài
                view.setBoardData(currentMatrix);
                view.updateStatus("Đã làm mới ván chơi về trạng thái ban đầu!");
            } else {
                view.updateStatus("Chưa có ván đấu nào để làm mới!");
            }
        });




        // 3. Xử lý nút Hint hỗ trợ điền
        view.getBtnHint().addActionListener(e -> {

            giveHint();
        });

        // UR-5.2 & UR-5.3: Xử lý Tạm dừng/Tiếp tục
        view.getBtnPause().addActionListener(e -> {
            if (gameController.isGameOver()) return; // Không cho pause khi game kết thúc
            if (!gameController.isPlaying() && !gameController.isPaused()) return;

            if (gameController.isPaused()) {
                // UR-5.3 RESUME
                gameController.resumeGame();
                gameTimer.start();           // Tiếp tục đếm thời gian
                view.setCellsVisible(true);  // Hiện lại bàn cờ
                setInputEnabled(true);       // Mở khóa nhập liệu
                view.highlightSameNumbers(); // Làm mới lại màu highlight khi tiếp tục
                checkBoardErrors();          // Làm mới lại các ô lỗi
                view.getBtnPause().setText("Tạm dừng");
                view.updateStatus("Tiếp tục chơi!");
            } else {
                // UR-5.2 PAUSE
                gameController.pauseGame();
                gameTimer.stop();            // Dừng timer
                view.setCellsVisible(false); // Ẩn bàn cờ
                setInputEnabled(false);      // Khóa nhập liệu
                view.getBtnPause().setText("Tiếp tục");
                view.updateStatus("Đang tạm dừng...");
            }
        });

        // [UR-4.4]
        // Kiểm tra lỗi realtime khi nhập
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int row = i;
                final int col = j;

                view.getCell(row, col).addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        if (!gameController.isPlaying()) return; // UR-5.2: Block input khi pause/gameover
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

        // ==========================================================
        // [3.1.5] Xử lý nút Kiểm Tra (đã ẩn khỏi UI, logic chuyển sang realtime)
        // ==========================================================
        /*
        view.getBtnValidate().addActionListener(e -> {
            if (isRunning) return;

            int[][] board = view.getBoardData();
            boolean[][] errors = logic.validateWholeBoard(board);
            boolean hasError = false;

            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (errors[r][c]) {
                        hasError = true;
                        view.highlightErrorCell(r, c, true);
                    } else {
                        view.highlightErrorCell(r, c, false);
                    }
                }
            }

            if (hasError) {
                view.updateStatus("Có lỗi trên bảng! (Các ô màu đỏ vi phạm luật)");
            } else {
                if (logic.isBoardComplete(board)) {
                    view.updateStatus("Chúc mừng! Bạn đã giải xong Sudoku một cách hợp lệ!");
                } else {
                    view.updateStatus("Bảng hợp lệ. Hãy tiếp tục giải!");
                }
            }
        });
        */

        // ==========================================================
        // [3.2.1] ~ [3.2.6] Xử lý nút Xem Giải Pháp (Auto-Solver Backtracking)
        // ==========================================================
        view.getBtnShowSolution().addActionListener(e -> {

            int[][] board = view.getBoardData();

            // [3.2.3] Validate toàn bảng trước khi giải
            boolean[][] errors = logic.validateWholeBoard(board);
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (errors[r][c]) {
                        view.updateStatus("Lỗi! Bảng hiện tại sai luật, không thể giải.");
                        view.highlightErrorCell(r, c, true);
                        return;
                    }
                }
            }

            view.updateStatus("Đang giải bằng Backtracking...");

            // [3.2.4] Giải bằng Backtracking
            boolean solved = logic.solveSudoku(board);

            if (solved) {
                // [3.2.5] Hiển thị giải pháp lên giao diện
                view.setBoardData(board);
                view.updateStatus("Đã giải xong bằng thuật toán Backtracking!");
            } else {
                view.updateStatus("Không tìm thấy giải pháp cho bảng này.");
            }
        });
    }

    // UR-5.2 & UR-5.3: Khóa/Mở toàn bộ ô nhập liệu khi Pause/Resume
    private void setInputEnabled(boolean enabled) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Khi pause: disable tất cả
                // Khi resume: chỉ mở lại các ô người chơi được phép nhập (ô trống trong đề bài)
                if (!enabled) {
                    view.getCell(i, j).setEnabled(false);
                } else {
                    // Mở lại toàn bộ ô để khôi phục màu sắc ban đầu (không bị xám)
                    // (các ô đề bài đã được setEditable(false) từ trước nên người chơi vẫn không thể sửa)
                    view.getCell(i, j).setEnabled(true);
                }
            }
        }
        // Disable các nút trong lúc pause
        view.getBtnHint().setEnabled(enabled);
        // view.getBtnValidate().setEnabled(enabled);
        view.getBtnShowSolution().setEnabled(enabled);
        view.getBtnReset().setEnabled(enabled);
    }

    private void displaySolutionToView(int[][] sol) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Chỉ điền vào những ô người dùng được phép nhập
                if (view.getCell(i, j).isEditable()) {
                    view.setCellValue(i, j, sol[i][j]);
                }
            }
        }
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
                    view.updateStatus(
                            "Đã dùng Hint: "
                                    + hintCount + "/" + MAX_HINT);

                });
            }
        });

        // Hiển thị thông báo đang tìm gợi ý cho người dùng biết
        view.updateStatus("Hệ thống đang tìm gợi ý cho ô [" + (row + 1) + "," + (col + 1) + "]...");

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
        if (!gameController.isPlaying()) return; // UR-5.2: Block khi pause

        int[][] board = view.getBoardData();
        int value = board[row][col];
        int[][] solution = engine.getSolution();
        if (solution == null) return;

        if (value == 0) {
            // Người chơi xóa ô → reset tracking
            gameController.recordMistake(row, col, 0);
            return;
        }

        if (value != solution[row][col]) {
            // Bạn có thể thêm thông báo status để người chơi biết
            // Sai: chỉ tính lỗi nếu là giá trị sai MỚI (GameController chống trùng)
            boolean isNewMistake = gameController.recordMistake(row, col, value);
            if (isNewMistake) {
                mistakeCount = gameController.getMistakeCount();
                view.updateMistakeUI(mistakeCount, gameController.getMaxMistakes());
                view.updateStatus("Sai rồi! Ô [" + (row + 1) + "," + (col + 1) + "] cần kiểm tra lại.");

                // UR-5.5: Kiểm tra thua
                // UR-5.5: Thông báo Thua
                if (gameController.isGameLost()) {
                    gameController.setLost();
                    triggerGameOver(false);
                }
            }
        } else {
            // Đúng: reset tracking ô này
            gameController.recordMistake(row, col, 0);
            // Nếu đúng, kiểm tra xem đã điền hết bàn cờ chưa
            // UR-5.5: Kiểm tra thắng
            checkWinCondition(board, solution);
        }
    }

    private void checkWinCondition(int[][] currentBoard, int[][] solution) {
        if (solution == null) return;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // Nếu có ô trống hoặc ô sai so với đáp án -> Chưa thắng
                // Còn ô chưa đúng → chưa thắng
                if (currentBoard[i][j] == 0 || currentBoard[i][j] != solution[i][j]) {
                    return;
                }
            }
        }
        // Nếu vượt qua vòng lặp -> Tất cả các ô đều đúng
        // Tất cả đúng và đủ → Thắng
        gameController.setWon();
        triggerGameOver(true);
    }

    // UR-5.5: Xử lý kết thúc game (Win hoặc Lose)
    private void triggerGameOver(boolean won) {
        // Dừng timer
        gameTimer.stop();
        String time = gameTimer.getTimeString();

        // Khóa toàn bộ bàn cờ
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                view.getCell(i, j).setEditable(false);

        // Disable nút Pause vì game đã kết thúc
        view.getBtnPause().setEnabled(false);

        if (won) {
            // UR-5.5 WIN
            JOptionPane.showMessageDialog(view,
                    "🎉 CHÚC MỪNG! Bạn đã hoàn thành Sudoku!\n" + time,
                    "CHIẾN THẮNG", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // UR-5.5 LOSE
            JOptionPane.showMessageDialog(view,
                    "💀 GAME OVER! Bạn đã phạm " + gameController.getMaxMistakes() + " lỗi.\n" + time,
                    "THUA CUỘC", JOptionPane.ERROR_MESSAGE);
            // Tự động tạo ván mới
            view.getBtnGenerate().doClick();
        }
    }
}