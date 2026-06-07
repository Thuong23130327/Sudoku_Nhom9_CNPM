package com.sudoku.model;

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

    public SudokuBoard() {
        this.currentMatrix = new int[9][9];
        this.isFixed = new boolean[9][9];
        this.solutionMatrix = new int[9][9];
    }

    // --- GETTERS & SETTERS ---
    
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


    // Các bạn phụ trách UC-01, UC-03 sẽ viết thêm logic vào đây
}