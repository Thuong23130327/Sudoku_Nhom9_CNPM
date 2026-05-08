package com.sudoku.model;

import java.util.ArrayList;
import java.util.List;
import com.sudoku.model.CellPosition;

/**
 * [UC-04] HỆ THỐNG TRỢ GIÚP VÀ PHẢN HỒI THỊ GIÁC
 * Phụ trách: Minh
 *
 * UR-4.1: Hint
 * UR-4.2: Giới hạn Hint
 * UR-4.3: Highlight ô cùng giá trị
 * UR-4.4: Highlight ô vi phạm luật Sudoku
 */
public class SudokuHight {
    /**
     * [UR-4.1]
     * Lấy giá trị Hint đúng từ bảng đáp án
     */
    public int getHintValue(
            int[][] solution,
            int row,
            int col) {

        return solution[row][col];
    }

    /**
     * [UR-4.2]
     * Kiểm tra còn lượt Hint hay không
     */
    public boolean canUseHint(
            int hintCount,
            int maxHint) {

        return hintCount < maxHint;
    }

    /**
     * [UR-4.4]
     * Kiểm tra ô hiện tại có vi phạm luật Sudoku không
     */
    public boolean isInvalid(
            int[][] board,
            int row,
            int col,
            int value) {

        // Kiểm tra trùng hàng
        for (int j = 0; j < 9; j++) {

            if (j != col &&
                    board[row][j] == value) {

                return true;
            }
        }

        // Kiểm tra trùng cột
        for (int i = 0; i < 9; i++) {

            if (i != row &&
                    board[i][col] == value) {

                return true;
            }
        }

        // Kiểm tra block 3x3
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;

        for (int i = startRow;
             i < startRow + 3;
             i++) {

            for (int j = startCol;
                 j < startCol + 3;
                 j++) {

                if ((i != row || j != col)
                        &&
                        board[i][j] == value) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * [UR-4.4]
     * Lấy danh sách các ô vi phạm luật Sudoku
     */
    public List<CellPosition> getInvalidCells(
            int[][] board) {

        List<CellPosition> invalidCells =
                new ArrayList<>();

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                int value = board[i][j];

                // Bỏ qua ô trống
                if (value == 0) {
                    continue;
                }

                if (isInvalid(
                        board,
                        i,
                        j,
                        value)) {

                    invalidCells.add(
                            new CellPosition(i, j)
                    );
                }
            }
        }

        return invalidCells;
    }

    /**
     * [UR-4.3]
     * Lấy danh sách các ô có cùng giá trị
     */
    public List<CellPosition> getSameValueCells(
            int[][] board,
            int value) {

        List<CellPosition> cells =
                new ArrayList<>();

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                if (board[i][j] == value) {

                    cells.add(
                            new CellPosition(i, j)
                    );
                }
            }
        }

        return cells;
    }
}
