package com.sudoku.model;

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

    public SudokuBoard() {
        this.currentMatrix = new int[9][9];
        this.isFixed = new boolean[9][9];
        this.solutionMatrix = new int[9][9];
    }

    // --- GETTERS & SETTERS ---
    
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


    // Các bạn phụ trách UC-01, UC-03 sẽ viết thêm logic vào đây
}