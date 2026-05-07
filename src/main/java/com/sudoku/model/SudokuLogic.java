package com.sudoku.model;
import com.sudoku.model.*;
/**
 * [UC-03] MODULE KIỂM TRA LOGIC VÀ GIẢI THUẬT
 * Phụ trách: Nguyễn Hoài Thương
 * Ánh xạ yêu cầu: UR-3.1, UR-3.2, UR-3.3, UR-3.4
 */
public class SudokuLogic {

    /**
     * [UR-3.1] Kiểm tra tính chính xác của nước đi (Strict Validation)
     * So sánh trực tiếp giá trị nhập với ma trận đáp án đã có sẵn trong Model.
     */
    public boolean checkUserInput(SudokuBoard board, int row, int col, int num) {
        int correctValue = board.getSolutionValue(row, col);
        return num == correctValue;
    }

    /**
     * [UR-3.2] Kiểm tra trạng thái hoàn thành của bảng
     * Quét toàn bộ bảng hiện tại để tìm ô trống (giá trị 0).
     */
    public boolean isBoardComplete(int[][] currentBoard) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (currentBoard[r][c] == 0) return false;
            }
        }
        return true;
    }

    /**
     * [UR-3.3] Thuật toán tự động giải bảng (Backtracking)
     * Sử dụng đệ quy để điền đầy các ô trống dựa trên quy tắc Sudoku.
     */
    public boolean solveSudoku(int[][] matrix) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (matrix[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValidRules(matrix, row, col, num)) {
                            matrix[row][col] = num;
                            if (solveSudoku(matrix)) return true;

                            matrix[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isValidRules(int[][] matrix, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            // Kiểm tra trùng hàng cột
            if (matrix[row][i] == num || matrix[i][col] == num) return false;
        }
        // Kiểm tra trùng trong khối
        int startR = row - row % 3;
        int startC = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matrix[startR + i][startC + j] == num) return false;
            }
        }
        return true;
    }
}