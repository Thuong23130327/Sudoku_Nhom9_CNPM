package com.sudoku.controller;

import com.sudoku.model.SudokuEngine;
import com.sudoku.model.SudokuGenerator;
import com.sudoku.model.SudokuLogic;
import com.sudoku.utils.TimerUtils;
import com.sudoku.view.HistoryFrame;
import com.sudoku.view.SudokuFrame;
import com.sudoku.model.Move;

import javax.swing.*;
import java.awt.*;
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
    private GameController gameController;
    private int hintCount = 0;
    private final int MAX_HINT = 3;
    private boolean isGameRunning = false;

    public SudokuController(SudokuFrame view) {
        this.view = view;
        // Khởi tạo các thành phần Model hợp lệ
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
        // UR-1.1: Xử lý nút "Tạo Mới" (Tích hợp phân chia cấp độ chơi)
        // ==========================================================
        view.getBtnGenerate().addActionListener(e -> {
            /*
                Update cho nút "Tạo mới", kiểm tra trạng thái game để xác nhận và lưu lịch sử lượt chơi
                Thêm Điều kiện: Game thực sự đang chạy (isGameRunning == true) và trận đấu chưa kết thúc (chưa GameOver)
                Người thực hiện: Nguyễn Thanh Tú
            */
            if (isGameRunning && !gameController.isGameOver()) {
                int confirm = JOptionPane.showConfirmDialog(
                        view,
                        "Bạn có chắc chắn muốn tạo mới bàn cờ không?\nLượt chơi hiện tại sẽ tính là một trận THUA (Bỏ cuộc)!",
                        "Xác nhận tạo mới",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                // Nếu người chơi chọn NO hoặc tắt Pop-up thì dừng toàn bộ xử lý, giữ nguyên màn chơi cũ
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                // Nếu người chơi chọn YES -> Tiến hành ghi nhận trận này là Thua (Bỏ cuộc) vào lịch sử
                saveMatchToHistory("Thua (Bỏ cuộc)");
            }


            engine.stop();

            // 1. Lấy độ khó người chơi chọn từ ComboBox trên giao diện
            String selectedLevel = view.getSelectedLevel();

            // 2. Quy đổi cấp độ thành số lượng ô trống cần xóa tương ứng
            int cellsToRemove = 30;
            switch (selectedLevel) {
                case "dễ":
                    cellsToRemove = 30;
                    break;
                case "trung bình":
                    cellsToRemove = 35;
                    break;
                case "asian":
                    cellsToRemove = 40;
                    break;
            }

            // 3. Sinh ma trận đề bài dựa trên số ô cần xóa
            int[][] newBoard = generator.generate(cellsToRemove);
            int actualEmptyCells = 0;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (newBoard[i][j] == 0) actualEmptyCells++;
                }
            }
            System.out.println(">>> LEVEL CHỌN: " + selectedLevel + " | SỐ Ô TRỐNG THỰC TẾ TRÊN LƯỚI: " + actualEmptyCells);
            currentMatrix = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(newBoard[i], 0, currentMatrix[i], 0, 9);
            }
            // Reset lịch sử đi bài (Undo/Redo)
            undoStack.clear();
            redoStack.clear();

            // Nạp ngay solution chuẩn vào engine từ đầu để tránh xung đột dữ liệu
            engine.setSolution(generator.getSolution());

            // Đổ đề bài lên lưới giao diện
            view.setBoardData(currentMatrix);

            // Khởi tạo lại hệ thống gợi ý (Hint)
            hintCount = 0;
            view.updateHintUI(MAX_HINT, MAX_HINT);
            engine.setOnGenerationEvolved(null);

            // Khởi động trạng thái quản lý game và lỗi
            gameController.startGame();
            view.updateMistakeUI(0, gameController.getMaxMistakes());
            view.updateStatus("Đã tạo màn chơi mới cấp độ: " + selectedLevel);

            // Mở lại các tương tác phòng trường hợp ván trước đang bị khóa (Pause/GameOver)
            view.getBtnPause().setEnabled(true);
            view.getBtnPause().setText("Tạm dừng");
            view.setCellsVisible(true);

            // Chạy giải thuật ngầm để hỗ trợ kiểm soát logic trò chơi
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    engine.solve(currentMatrix);
                    return null;
                }

                @Override
                protected void done() {
                    view.updateStatus("Ván chơi [" + selectedLevel + "] đã sẵn sàng!");
                }
            }.execute();

            // Làm mới đồng hồ đếm giờ hành trình
            gameTimer.reset();
            gameTimer.start();

            //ĐÁNH DẤU CỜ HIỆU GAME CHÍNH THỨC BẮT ĐẦU CHẠY
            isGameRunning = true;
        });

        // ==========================================================
        // UR-1.2: Xử lý nút "Làm Mới" (Reset Game)
        // ==========================================================
        view.getBtnReset().addActionListener(e -> {
            if (currentMatrix != null) {
                view.setBoardData(currentMatrix);
                view.updateStatus("Đã làm mới ván chơi về trạng thái ban đầu!");
            } else {
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
                //setInputEnabled(true);
                view.setGameplayButtonsEnabled(true); // Update: Gọi hàm mới từ View để mở khóa các nút
                view.highlightSameNumbers();
                checkBoardErrors();
                view.getBtnPause().setText("Tạm dừng");
                view.updateStatus("Tiếp tục chơi!");
            } else {
                gameController.pauseGame();
                gameTimer.stop();
                view.setCellsVisible(false);
                //setInputEnabled(false);
                view.setGameplayButtonsEnabled(false); // Update: Gọi hàm mới từ View để ẩn các nút chức năng khác
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
                JTextField cell = view.getCell(row, col);

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
                            /*
                                Xử lý sự kiện cho chức năng Ghi chú (Note)
                                Người thực hiện: Nguyễn Thanh Tú
                             */
                            @Override
                            public void keyTyped(java.awt.event.KeyEvent e) {
                                // NẾU ĐANG BẬT NOTE: Xử lý bằng cơ chế ghi chú ẩn, chặn Swing can thiệp thô
                                if (view.getBtnNote().isSelected()) {
                                    handleNoteInputIndependent(cell, e.getKeyChar());
                                    view.highlightSameNumbers();
                                    e.consume(); // Chặn đứng không cho chữ tự điền vào Document để tránh kích hoạt InputHandler báo lỗi
                                }
                            }

                            @Override
                            public void keyReleased(java.awt.event.KeyEvent e) {
                                if (!gameController.isPlaying()) return;

                                if (view.getBtnNote().isSelected()) return; // Nếu đang bật Note thì bỏ qua hoàn toàn sự kiện này
                                JTextField cell = view.getCell(row, col);

                                // Nếu tắt note và người chơi nhập đè lên ô đang có note
                                if (cell.getClientProperty("isNoteMode") != null) {
                                    cell.putClientProperty("isNoteMode", null);
                                    cell.putClientProperty("noteText", null);
                                    cell.setFont(new Font("Arial", Font.BOLD, 20));
                                    cell.setForeground(Color.BLACK);
                                    cell.setText("");
                                }

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
        /*
            UC-5.6: Xem lịch sử các lần chơi
            Xử lý Sự kiện nút "Xem Lịch Sử" (btnHistory):
            Người thực hiện: Nguyễn Thanh Tú
        */
        view.getBtnHistory().addActionListener(e -> {
            // Mở cửa sổ lịch sử độc lập
            HistoryFrame historyWindow = new HistoryFrame();
            historyWindow.setVisible(true);
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

    /*
        Update hàm cho UC-5.6: Xem lịch sử các lần chơi
        Thêm biến isGameRunning = false, đánh dấu trận đấu kết thúc ngay lập tức
        Khi người dùng chiến thắng hoặc thua cuộc (do quá lỗi) thì gọi hàm saveMatchToHistory lưu lại trạng thái tương ứng
        Người thực hiện: Nguyễn Thanh Tú
     */
    private void triggerGameOver(boolean won) {
        gameTimer.stop();
        String time = gameTimer.getTimeString();

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                view.getCell(i, j).setEditable(false);

        view.getBtnPause().setEnabled(false);


        isGameRunning = false; // Đánh dấu trận đấu kết thúc ngay lập tức

        if (won) {
            saveMatchToHistory("Chiến thắng"); // Lưu lịch sử trận thắng

            JOptionPane.showMessageDialog(view,
                    "🎉 CHÚC MỪNG! Bạn đã hoàn thành xuất sắc bản Sudoku này!\n" + time,
                    "CHIẾN THẮNG", JOptionPane.INFORMATION_MESSAGE);
        } else {
            saveMatchToHistory("Thua (Quá lỗi)"); // Lưu lịch sử trận thua do phạm quy

            JOptionPane.showMessageDialog(view,
                    "💀 GAME OVER! Bạn đã phạm quá " + gameController.getMaxMistakes() + " lỗi quy định.\n" + time,
                    "THUA CUỘC", JOptionPane.ERROR_MESSAGE);
//            view.getBtnGenerate().doClick(); // Tự động Reset tạo màn chơi mới
        }
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
    /*
        UC-5.6: Xem lịch sử các lần chơi
        Thêm hàm phụ trách ghi trận đấu vào file JSON (Dùng chung cho cả Thắng, Thua, Bỏ cuộc)
        Khi người dùng chiến thắng hoặc thua cuộc thì gọi hàm saveMatchToHistory lưu lại trạng thái tương ứng
        Người thực hiện: Nguyễn Thanh Tú
    */
    private void saveMatchToHistory(String outcome) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDate = sdf.format(new java.util.Date());
        String currentLevel = view.getSelectedLevel();
        int duration = gameTimer.getSeconds(); // Giả định hàm lấy tổng số giây hiện tại của bộ đếm của bạn

        com.sudoku.model.GameMatch newMatch = new com.sudoku.model.GameMatch(currentDate, currentLevel, duration, outcome);

        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.util.List<com.sudoku.model.GameMatch> historyList = new java.util.ArrayList<>();

        // Đọc lịch sử cũ nếu có
        java.io.File file = new java.io.File("history.json");
        if (file.exists()) {
            try (java.io.FileReader reader = new java.io.FileReader(file)) {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.ArrayList<com.sudoku.model.GameMatch>>(){}.getType();
                java.util.List<com.sudoku.model.GameMatch> existing = gson.fromJson(reader, listType);
                if (existing != null) {
                    historyList.addAll(existing);
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        // Thêm trận mới vào danh sách
        historyList.add(newMatch);

        // Ghi ngược lại vào file
        try (java.io.FileWriter writer = new java.io.FileWriter("history.json")) {
            gson.toJson(historyList, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /*
       Hàm xử lý dữ liệu người dùng nhập giá trị vào ô khi đang bật chế độ Ghi chú (Note)
       Người thực hiện: Nguyễn Thanh Tú
    */
    private void handleNoteInputIndependent(JTextField cell, char keyChar) {
        if (keyChar < '1' || keyChar > '9') {
            return;
        }

        // Đánh dấu ô này đang ở chế độ Note ẩn
        cell.putClientProperty("isNoteMode", true);

        // Lấy chuỗi nháp cũ ra từ bộ nhớ ẩn của ô
        String currentText = (String) cell.getClientProperty("noteText");
        if (currentText == null) currentText = "";

        String inputNum = String.valueOf(keyChar);

        // Tách chuỗi xử lý mảng
        java.util.List<String> notes = new java.util.ArrayList<>();
        if (!currentText.isEmpty()) {
            String[] tokens = currentText.split("[\\s]+");
            for (String t : tokens) {
                if (!t.isEmpty()) notes.add(t);
            }
        }

        // Logic gõ lại số cũ thì xóa, số mới thì thêm
        if (notes.contains(inputNum)) {
            notes.remove(inputNum);
        } else {
            notes.add(inputNum);
        }

        java.util.Collections.sort(notes);

        // Nối chuỗi
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
            sb.append(notes.get(i));
            if (i < notes.size() - 1) sb.append(" ");
        }

        String resultText = sb.toString();

        // Lưu lại chuỗi nháp mới vào bộ nhớ ẩn
        cell.putClientProperty("noteText", resultText);

        // Ép giao diện hiển thị chuỗi "1 3 5" chữ xám nghiêng nhỏ nhưng KHÔNG bắn sự kiện thay đổi text thực tế
        SwingUtilities.invokeLater(() -> {
            cell.setFont(new Font("Arial", Font.ITALIC, 11));
            cell.setForeground(Color.GRAY);

            // Mẹo nhỏ: Dùng setText hiển thị nhưng vì keyTyped đã consume,
            // kết hợp với việc ta tạo luồng render riêng sẽ làm InputHandler đọc ra chuỗi rỗng, không gây lỗi logic.
            cell.setText(resultText);
        });
    }
}

