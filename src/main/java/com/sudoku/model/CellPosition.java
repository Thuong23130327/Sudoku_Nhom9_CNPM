package com.sudoku.model;

/**
 * Class lưu vị trí của một ô Sudoku
 * Dùng cho:
 * - Highlight ô cùng giá trị
 * - Highlight ô lỗi
 * - Hint
 */
public class CellPosition {

    private int row;
    private int col;

    public CellPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // Getter row
    public int getRow() {
        return row;
    }

    // Setter row
    public void setRow(int row) {
        this.row = row;
    }

    // Getter col
    public int getCol() {
        return col;
    }

    // Setter col
    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}