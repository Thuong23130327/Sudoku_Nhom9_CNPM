package com.sudoku.model;
import java.util.Stack;

/**
 * [MODEL] Cấu trúc dữ liệu dùng chung cho toàn dự án.
 * Ánh xạ luồng sự kiện: 3.2.4, 4.1
 */
public class SudokuBoard {
    // Ma trận 9x9 chứa trạng thái hiện tại người chơi đang nhập
    private int[][] currentMatrix;
    
    // Ma trận 9x9 chứa đề bài gốc (không cho phép xóa/sửa)
    private boolean[][] isFixed;
    
    // Ma trận 9x9 chứa đáp án hoàn chỉnh (dùng cho Hint và Check Win) [3.2.4, 4.1]
    private int[][] solutionMatrix;
    private Stack<Move> undoStack = new Stack<>();
    private Stack<Move> redoStack = new Stack<>();

    public SudokuBoard() {
        this.currentMatrix = new int[9][9];
        this.isFixed = new boolean[9][9];
        this.solutionMatrix = new int[9][9];
    }

    public int getValueAt(int row, int col) {

        return currentMatrix[row][col];
    }

    // Cập nhật giá trị vào ma trận bàn cờ hiện tại
    public void setValueAt(int row, int col, int value) {
        // Kiểm tra trạng thái thấy ô hiện tại là ô gốc (Read-Only), từ chối thực hiện lệnh cập nhật
        if (!isFixed[row][col]) {
            currentMatrix[row][col] = value;
        }
    }

    // Kiểm tra xem ô được chọn có phải là ô đề bài gốc không
    public boolean isFixedCell(int row, int col) {
        return isFixed[row][col];
    }

    public void setFixed(int row, int col, boolean fixed) {
        this.isFixed[row][col] = fixed;
    }

    public void setSolutionBoard(int[][] dummySolution) {
    }

    public int getSolutionValue(int row, int col) {
        return solutionMatrix[row][col];
    }
// upadte uc3 hàm make move để tìm giá trị mới nhất mà người dùng nhập vào ô
    public boolean makeMove(int row, int col, int newValue) {

        if (isFixed[row][col]) {
            return false;
        }

        int oldValue = currentMatrix[row][col];
        if (oldValue == newValue) {
            return false;
        }
        undoStack.push(new Move(row, col, oldValue, newValue)
        );
        redoStack.clear();
        currentMatrix[row][col] = newValue;
        return true;
    }
    // Update UC3
    // tạo nút undo và kiểm tra redo lại board lúc đầu
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Move move = undoStack.pop();
        currentMatrix[move.getRow()][move.getCol()] = move.getOldValue();
        redoStack.push(move);
        return true;
    }
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Move move = redoStack.pop();
        currentMatrix[move.getRow()][move.getCol()] = move.getNewValue();
        undoStack.push(move);
        return true;
    }
    public int[][] getBoard(){
        return currentMatrix;
    }
    // Thêm hàm sinh đề bài dựa trên số lượng ô cần xóa
    public int[][] generatePuzzleByLevel(int[][] completeBoard, String level) {
        // Sao chép bảng ban đầu đã giải hoàn chỉnh sang bảng chơi
        int[][] puzzleBoard = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(completeBoard[i], 0, puzzleBoard[i], 0, 9);
        }

        // Xác định số lượng ô trống dựa trên Level nhận từ View
        int cellsToRemove = 30; // Mặc định cho "Dễ"
        switch (level) {
            case "Trung bình":
                cellsToRemove = 40;
                break;
            case "Khó":
                cellsToRemove = 50;
                break;
            case "Asian":
                cellsToRemove = 56;
                break;
        }

        // Tiến hành xóa ngẫu nhiên các ô trên ma trận
        java.util.Random rand = new java.util.Random();
        int removed = 0;
        while (removed < cellsToRemove) {
            int row = rand.nextInt(9);
            int col = rand.nextInt(9);

            // Nếu ô này chưa bị xóa (khác 0), tiến hành xóa nó
            if (puzzleBoard[row][col] != 0) {
                puzzleBoard[row][col] = 0;
                removed++;
            }
        }

        return puzzleBoard;
    }
    }
