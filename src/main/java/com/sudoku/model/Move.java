package com.sudoku.model;

public class Move {
    private int row ;
    private int col;
    private int oldValue;
    private int newValue;
    public Move(int row, int col, int oldValue, int newValue) {
        this.row = row;
        this.col = col;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getOldValue() {
        return oldValue;
    }

    public int getNewValue() {
        return newValue;
    }
}

