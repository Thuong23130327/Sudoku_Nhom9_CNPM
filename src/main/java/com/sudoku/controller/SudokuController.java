package com.sudoku.controller;

import com.sudoku.model.SudokuEngine;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.model.SudokuLogic;
import com.sudoku.utils.TimerUtils;
import com.sudoku.view.SudokuFrame;
import com.sudoku.view.WelcomeDialog;
import com.sudoku.model.Move;
import javax.swing.*;
import java.util.Stack;

public class SudokuController {
    private SudokuFrame view;
    private SudokuEngine engine;
    private SudokuGenerator generator;
    private SudokuLogic logic;
    private InputHandler inputHandler;
    private int[][] currentMatrix;
    private int[][] previousValues = new int[9][9];
    private TimerUtils gameTimer;
    private Stack<Move> undoStack = new Stack<>();
    private Stack<Move> redoStack = new Stack<>();
    private int mistakeCount = 0;
    private final int MAX_MISTAKES = 3;
    private GameController gameController; // UR-5: Quản lý trạng thái tập trung
    private int hintCount = 0;
    private final int MAX_HINT = 3;

    public SudokuController(SudokuFrame view) {
        this.view = view;
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();
        this.logic = new SudokuLogic();
        this.gameTimer = new TimerUtils(view.getLblTimer());
        this.gameController = new GameController(3); // 3 = MAX_MISTAKES

        // Khởi tạo InputHandler — validate realtime mỗi lần nhập số
        this.inputHandler = new InputHandler(view, logic);

        // Gắn sự kiện điều khiển cho toàn bộ UI
        initController();
    }

    private void initController() {
        // ==========================================================
        // UR-1.1: Xử lý nút "Tạo Mới" (Generate Game)
        // 1.1.1: Người chơi thực hiện hành động click() vào JButton btnGenerate trên giao diện.
        // ==========================================================
        view.getBtnGenerate().addActionListener(e -> {
            // 1.1.2: SudokuFrame (View) gọi hàm actionPerformed(ActionEvent e) gửi sự kiện đến SudokuController.
            WelcomeDialog dialog = new WelcomeDialog(view);
            // 1.1.3: SudokuController khởi tạo và gọi hàm setVisible(true) để hiển thị WelcomeDialog.
            dialog.setVisible(true);
            // 1.1.7: SudokuController gọi hàm getSelectedMissingDigits() từ WelcomeDialog để lấy tham số missingDigits.
            int missingDigits = dialog.getSelectedMissingDigits();
            if (missingDigits != -1) {
                // 1.1.8: SudokuController tự gọi hàm tổng hợp generateBoardWithDifficulty(missingDigits).
                generateBoardWithDifficulty(missingDigits);
            } else {
                // 1.3.2: SudokuController nhận kết quả missingDigits == -1 do người chơi đóng hộp thoại (Hủy tạo mới).
            }
        });

        // ==========================================================
        // UR-1.2: Xử lý nút "Làm Mới" (Reset Game)
        // 1.2.0: Tiền điều kiện (Current State): Bàn cờ đang hiển thị và biến currentMatrix khác null (Ván đấu đang diễn ra, người chơi đã điền một số ô).
        // 1.2.1: Người chơi thực hiện hành động click() vào JButton btnReset.
        // ==========================================================
        view.getBtnReset().addActionListener(e -> {
            // 1.2.2: SudokuFrame (View) gọi hàm actionPerformed(ActionEvent e) gửi sự kiện đến SudokuController.
            // 1.2.3: SudokuController xử lý lệnh kiểm tra điều kiện if (currentMatrix != null).
            if (currentMatrix != null) {
                // Đưa mảng gốc vào lại, hàm setBoardData sẽ đè lại giao diện,
                // tự động xóa các ô người chơi đã nhập và giữ nguyên ô đề bài
                // 1.2.4: Điều kiện đúng, SudokuController gọi hàm setBoardData(currentMatrix) của SudokuFrame.                view.setBoardData(currentMatrix);
                // 1.2.6: SudokuController gọi hàm updateStatus(msg) của SudokuFrame.
                view.updateStatus("Đã làm mới ván chơi về trạng thái ban đầu!");
            } else {
                // 1.4.1: SudokuController nhận kết quả currentMatrix == null (Chưa khởi tạo ván đấu).
                // 1.4.2: SudokuController gọi hàm updateStatus("Chưa có ván đấu nào để làm mới!") của SudokuFrame để báo lỗi.
                view.updateStatus("Chưa có ván đấu nào để làm mới!");
            }
        });

        // ==========================================================
        // UC-3: Quản lý Hoàn tác (Undo) và Làm lại (Redo) bao gồm Phím tắt
        // ==========================================================
        view.getBtnUndo().addActionListener(e -> undoMove());

        // Đăng ký phím tắt Ctrl + Z
        view.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control Z"), "undo");

        view.getRootPane()
                .getActionMap()
                .put("undo", new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        undoMove();
                    }
                });

        // Đăng ký phím tắt Ctrl + Y
        view.getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control Y"), "redo");

        view.getRootPane()
                .getActionMap()
                .put("redo", new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        redoMove();
                    }
                });

        // ==========================================================
        // UR-4.1: Xử lý nút Hint (Gợi ý)
        // ==========================================================
        view.getBtnHint().addActionListener(e -> giveHint());

        // ==========================================================
        // UR-5.2 & UR-5.3: Xử lý chức năng Tạm dừng / Tiếp tục game
        // ==========================================================
        view.getBtnPause().addActionListener(e -> {
            if (gameController.isGameOver()) return;

            if (gameController.isPaused()) {
                gameController.resumeGame();
                gameTimer.start();
                view.setCellsVisible(true);
                setInputEnabled(true);
                view.highlightSameNumbers();
                checkBoardErrors();
                view.getBtnPause().setText("Tạm dừng");
                view.updateStatus("Tiếp tục chơi!");
            } else {
                gameController.pauseGame();
                gameTimer.stop();
                view.setCellsVisible(false);
                setInputEnabled(false);
                view.getBtnPause().setText("Tiếp tục");
                view.updateStatus("Đang tạm dừng...");
            }
        });

        // ==========================================================
        // UR-4.4: Lắng nghe hành vi nhập số Real-time từ các ô lưới
        // ==========================================================
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int row = i;
                final int col = j;

                view.getCell(row, col).addFocusListener(
                        new java.awt.event.FocusAdapter() {
                            @Override
                            public void focusGained(java.awt.event.FocusEvent e) {
                                String text = view.getCell(row, col).getText();
                                previousValues[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
                            }
                        }
                );

                view.getCell(row, col).addKeyListener(
                        new java.awt.event.KeyAdapter() {
                            @Override
                            public void keyReleased(java.awt.event.KeyEvent e) {
                                if (!gameController.isPlaying()) return;

                                String text = view.getCell(row, col).getText();
                                int newValue = text.isEmpty() ? 0 : Integer.parseInt(text);
                                int oldValue = previousValues[row][col];

                                if (oldValue != newValue) {
                                    undoStack.push(new Move(row, col, oldValue, newValue));
                                    redoStack.clear(); // Hủy Redo khi có bước đi mới
                                    previousValues[row][col] = newValue;
                                }

                                view.highlightSameNumbers();
                                checkBoardErrors();
                                handleUserInput(row, col);
                            }
                        }
                );
            }
        }

        // ==========================================================
        // UR-3.2: Xử lý tính năng Tự động giải (Auto-Solver Backtracking)
        // ==========================================================
        view.getBtnShowSolution().addActionListener(e -> {
            int[][] boardData = view.getBoardData();

            // Validate toàn bảng trước khi chạy giải thuật
            boolean[][] errors = logic.validateWholeBoard(boardData);
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (errors[r][c]) {
                        view.updateStatus("Lỗi! Bảng hiện tại sai luật, không thể tự động giải.");
                        view.highlightErrorCell(r, c, true);
                        return;
                    }
                }
            }

            view.updateStatus("Đang tìm giải pháp tự động bằng Backtracking...");
            boolean solved = logic.solveSudoku(boardData);

            if (solved) {
                view.setBoardData(boardData);
                view.updateStatus("Đã hoàn tất giải bảng Sudoku tự động!");
            } else {
                view.updateStatus("Không tìm thấy giải pháp khả thi cho trạng thái bảng này.");
            }
        });
    }

    private void setInputEnabled(boolean enabled) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!enabled) {
                    view.getCell(i, j).setEnabled(false);
                } else {
                    view.getCell(i, j).setEnabled(true);
                }
            }
        }
        view.getBtnHint().setEnabled(enabled);
        view.getBtnShowSolution().setEnabled(enabled);
        view.getBtnReset().setEnabled(enabled);
    }

    private void giveHint() {
        if (hintCount >= MAX_HINT) {
            view.updateStatus("Đã hết số lượt gợi ý (Hint) cho ván đấu này!");
            return;
        }

        int row = view.getSelectedRow();
        int col = view.getSelectedCol();

        if (row == -1 || col == -1) {
            view.updateStatus("Hãy nhấp chọn một ô trống trên bàn cờ trước!");
            return;
        }

        int[][] boardData = view.getBoardData();
        if (boardData[row][col] != 0) {
            view.updateStatus("Ô được chọn đã có dữ liệu số!");
            return;
        }

        final boolean[] hintProcessed = {false};

        engine.setOnGenerationEvolved(ind -> {
            if (ind.getFitness() >= 160 && !hintProcessed[0]) {
                hintProcessed[0] = true;
                int suggestedValue = ind.getGenes().get(row).getNumber().get(col);

                SwingUtilities.invokeLater(() -> {
                    view.setCellValue(row, col, suggestedValue);
                    hintCount++;
                    int remaining = MAX_HINT - hintCount;
                    view.updateHintUI(remaining, MAX_HINT);
                    view.updateStatus("Đã kích hoạt gợi ý tại ô [" + (row + 1) + "," + (col + 1) + "]");
                });
            }
        });

        view.updateStatus("Hệ thống đang chạy ngầm để tính toán gợi ý...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                engine.solve(boardData);
                return null;
            }
        }.execute();
    }

    private boolean isInvalid(int[][] boardData, int row, int col, int value) {
        for (int j = 0; j < 9; j++) {
            if (j != col && boardData[row][j] == value) return true;
        }
        for (int i = 0; i < 9; i++) {
            if (i != row && boardData[i][col] == value) return true;
        }
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if ((i != row || j != col) && boardData[i][j] == value) return true;
            }
        }
        return false;
    }

    private void checkBoardErrors() {
        int[][] boardData = view.getBoardData();
        int[][] solution = engine.getSolution();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int value = boardData[i][j];
                if (value == 0) continue;

                if (value < 1 || value > 9) {
                    view.highlightErrorCell(i, j);
                    continue;
                }
                if (isInvalid(boardData, i, j, value)) {
                    view.highlightErrorCell(i, j);
                }
                if (solution != null && value != solution[i][j]) {
                    view.highlightErrorCell(i, j);
                }
            }
        }
    }

    private void handleUserInput(int row, int col) {
        if (!gameController.isPlaying()) return;

        int[][] boardData = view.getBoardData();
        int value = boardData[row][col];
        int[][] solution = engine.getSolution();
        if (solution == null) return;

        if (value == 0) {
            gameController.recordMistake(row, col, 0);
            return;
        }

        if (value != solution[row][col]) {
            boolean isNewMistake = gameController.recordMistake(row, col, value);
            if (isNewMistake) {
                mistakeCount = gameController.getMistakeCount();
                view.updateMistakeUI(mistakeCount, gameController.getMaxMistakes());
                view.updateStatus("Sai rồi! Ô [" + (row + 1) + "," + (col + 1) + "] không chính xác.");

                if (gameController.isGameLost()) {
                    gameController.setLost();
                    triggerGameOver(false);
                }
            }
        } else {
            gameController.recordMistake(row, col, 0);
            checkWinCondition(boardData, solution);
        }
    }

    private void checkWinCondition(int[][] currentBoard, int[][] solution) {
        if (solution == null) return;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (currentBoard[i][j] == 0 || currentBoard[i][j] != solution[i][j]) {
                    return;
                }
            }
        }
        gameController.setWon();
        triggerGameOver(true);
    }

    private void triggerGameOver(boolean won) {
        gameTimer.stop();
        String time = gameTimer.getTimeString();

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                view.getCell(i, j).setEditable(false);

        view.getBtnPause().setEnabled(false);

        if (won) {
            JOptionPane.showMessageDialog(view,
                    "🎉 CHÚC MỪNG! Bạn đã hoàn thành xuất sắc bản Sudoku này!\n" + time,
                    "CHIẾN THẮNG", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view,
                    "💀 GAME OVER! Bạn đã phạm quá " + gameController.getMaxMistakes() + " lỗi quy định.\n" + time,
                    "THUA CUỘC", JOptionPane.ERROR_MESSAGE);
            view.getBtnGenerate().doClick(); // Tự động Reset tạo màn chơi mới
        }
    }

    public void generateBoardWithDifficulty(int missingDigits) {
        // 1.1.9: SudokuController gọi hàm stop() của SudokuEngine.
        engine.stop();

        // 1.1.10: SudokuController gọi hàm generate(missingDigits) của SudokuGenerator (Model).
        int[][] newBoard = generator.generate(missingDigits);
        currentMatrix = new int[9][9];
        for (int i = 0; i < 9; i++)
            System.arraycopy(newBoard[i], 0, currentMatrix[i], 0, 9);

        // Nạp ngay solution chuẩn vào engine từ đầu để tránh lỗi tô đỏ toàn bảng khi tiếp tục
        // 1.1.14: SudokuController gọi hàm getSolution() từ SudokuGenerator để lấy đáp án chuẩn.
        // 1.1.15: SudokuController gọi hàm setSolution(solution) truyền đáp án vào SudokuEngine.
        engine.setSolution(generator.getSolution());

        // 1.1.16: SudokuController gọi hàm setBoardData(currentMatrix) của SudokuFrame.
        view.setBoardData(currentMatrix);
        hintCount = 0;
        view.updateHintUI(MAX_HINT, MAX_HINT);
        engine.setOnGenerationEvolved(null);

        // UR-5.1 + UR-5.4: Reset trạng thái game và số lỗi cho ván mới
        gameController.startGame();
        // 1.1.18: SudokuController gọi hàm updateMistakeUI(0, maxMistakes) và updateStatus(msg) của SudokuFrame.
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
        // 1.1.19: SudokuController gọi hàm reset() và start() của đối tượng TimerUtils.
        gameTimer.reset();
        gameTimer.start();
    }

    private void undoMove() {
        if (undoStack.isEmpty()) {
            view.updateStatus("Không có thao tác nào để hoàn tác!");
            return;
        }

        Move move = undoStack.pop();
        redoStack.push(move);

        view.setCellValue(move.getRow(), move.getCol(), move.getOldValue());
        previousValues[move.getRow()][move.getCol()] = move.getOldValue();

        int[][] solution = engine.getSolution();
        if (move.getOldValue() == 0 || (solution != null && move.getOldValue() == solution[move.getRow()][move.getCol()])) {
            gameController.recordMistake(move.getRow(), move.getCol(), 0);
        } else if (solution != null && move.getOldValue() != solution[move.getRow()][move.getCol()]) {
            gameController.recordMistake(move.getRow(), move.getCol(), move.getOldValue());
        }

        mistakeCount = gameController.getMistakeCount();
        view.updateMistakeUI(mistakeCount, gameController.getMaxMistakes());
        view.highlightSameNumbers();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                view.highlightErrorCell(r, c, false);
            }
        }
        checkBoardErrors();
        view.updateStatus("Đã hoàn tác bước đi vừa thực hiện.");
    }

    private void redoMove() {
        if (redoStack.isEmpty()) {
            view.updateStatus("Không có thao tác nào để làm lại!");
            return;
        }

        Move move = redoStack.pop();
        undoStack.push(move);

        view.setCellValue(move.getRow(), move.getCol(), move.getNewValue());
        previousValues[move.getRow()][move.getCol()] = move.getNewValue();

        view.highlightSameNumbers();
        checkBoardErrors();
        handleUserInput(move.getRow(), move.getCol());

        view.updateStatus("Đã thực hiện lại (Redo) hành động.");
    }
}