package com.sudoku.model;
import java.util.Stack;

/**
 * [MODEL] Cấu trúc dữ liệu dùng chung cho toàn dự án.
 * Thống nhất đánh số theo dải UR-1.x, 2.x, 3.x
 */
public class SudokuBoard {
    // Ma trận 9x9 chứa trạng thái hiện tại người chơi đang nhập [UR-2.2]
    private int[][] currentMatrix;
    
    // Ma trận 9x9 chứa đề bài gốc (để không cho phép xóa/sửa) [UR-2.4]
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

    public void setValueAt(int row, int col, int value) {
        // Chỉ cho phép sửa nếu ô đó không phải là số mặc định của đề bài
        if (!isFixed[row][col]) {
            currentMatrix[row][col] = value;
        }
    }

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

    public boolean makeMove(int row, int col, int newValue) {

        if (isFixed[row][col]) {
            return false;
        }

        int oldValue = currentMatrix[row][col];

        if (oldValue == newValue) {
            return false;
        }

        undoStack.push(
                new Move(row, col, oldValue, newValue)
        );

        redoStack.clear();

        currentMatrix[row][col] = newValue;

        return true;
    }
    public boolean undo() {

        if (undoStack.isEmpty()) {
            return false;
        }

        Move move = undoStack.pop();

        currentMatrix[move.getRow()][move.getCol()]
                = move.getOldValue();

        redoStack.push(move);

        return true;
    }
    public boolean redo() {

        if (redoStack.isEmpty()) {
            return false;
        }

        Move move = redoStack.pop();

        currentMatrix[move.getRow()][move.getCol()]
                = move.getNewValue();

        undoStack.push(move);

        return true;
    }
    public int[][] getBoard(){
        return currentMatrix;

    }

    }
