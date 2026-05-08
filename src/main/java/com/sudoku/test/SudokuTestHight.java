package com.sudoku.test;

import com.sudoku.model.CellPosition;
import com.sudoku.model.SudokuHight;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SudokuTestHight {

    private SudokuHight hight = new SudokuHight();
    // BOARD HỢP LỆ
    public static final int[][] VALID_BOARD = {

            {5,3,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},

            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},

            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };
    // UR-4.3
    // BOARD TEST HIGHLIGHT CÙNG GIÁ TRỊ
    public static final int[][] SAME_NUMBER_BOARD = {

            {5,3,5,0,7,0,0,0,0},
            {6,0,0,1,9,5,0,0,0},
            {0,9,8,0,0,0,0,6,0},

            {8,0,0,0,6,0,0,0,3},
            {4,0,0,8,0,3,0,0,1},
            {7,0,0,0,2,0,0,0,6},

            {0,6,0,0,0,0,2,8,0},
            {0,0,0,4,1,9,0,0,5},
            {0,0,0,0,8,0,0,7,9}
    };
    // UR-4.4
    // BOARD TEST TRÙNG HÀNG
    public static final int[][] INVALID_ROW_BOARD = {

            {5,5,4,6,7,8,9,1,2},
            {6,7,2,1,9,5,3,4,8},
            {1,9,8,3,4,2,5,6,7},

            {8,5,9,7,6,1,4,2,3},
            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},

            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };
    // UR-4.4
    // BOARD TEST TRÙNG CỘT
    public static final int[][] INVALID_COL_BOARD = {

            {5,3,4,6,7,8,9,1,2},
            {5,7,2,1,9,5,3,4,8},

            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},

            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},

            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };
    // UR-4.4
    // BOARD TEST TRÙNG BLOCK
    public static final int[][] INVALID_BLOCK_BOARD = {

            {5,3,4,6,7,8,9,1,2},
            {6,5,2,1,9,5,3,4,8},

            {1,9,8,3,4,2,5,6,7},
            {8,5,9,7,6,1,4,2,3},

            {4,2,6,8,5,3,7,9,1},
            {7,1,3,9,2,4,8,5,6},

            {9,6,1,5,3,7,2,8,4},
            {2,8,7,4,1,9,6,3,5},
            {3,4,5,2,8,6,1,7,9}
    };
    // UR-4.1
    // TEST HINT
    @Test
    public void verifyHintValue() {

        int value =
                hight.getHintValue(
                        VALID_BOARD,
                        0,
                        2
                );

        assertEquals(4, value);
    }
    // UR-4.2
    // TEST LIMIT HINT
    @Test
    public void verifyHintLimit() {

        int hintCount = 3;
        int maxHint = 3;

        boolean canUse =
                hight.canUseHint(
                        hintCount,
                        maxHint
                );

        assertFalse(canUse);
    }
    // UR-4.3
    // TEST HIGHLIGHT SAME NUMBER
    @Test
    public void verifySameValueHighlight() {

        List<CellPosition> cells =
                hight.getSameValueCells(
                        SAME_NUMBER_BOARD,
                        5
                );

        assertTrue(cells.size() > 1);
    }
    // UR-4.4
    // TEST INVALID ROW
    @Test
    public void verifyInvalidRow() {

        boolean result =
                hight.isInvalid(
                        INVALID_ROW_BOARD,
                        0,
                        0,
                        5
                );

        assertTrue(result);
    }
    // UR-4.4
    // TEST INVALID COLUMN
    @Test
    public void verifyInvalidColumn() {

        boolean result =
                hight.isInvalid(
                        INVALID_COL_BOARD,
                        0,
                        0,
                        5
                );

        assertTrue(result);
    }
    // UR-4.4
    // TEST INVALID BLOCK
    @Test
    public void verifyInvalidBlock() {

        boolean result =
                hight.isInvalid(
                        INVALID_BLOCK_BOARD,
                        0,
                        0,
                        5
                );

        assertTrue(result);
    }
    // UR-4.4
    // TEST GET INVALID CELLS
    @Test
    public void verifyGetInvalidCells() {
        List<CellPosition> invalidCells =
                hight.getInvalidCells(
                        INVALID_ROW_BOARD
                );
        assertTrue(
                invalidCells.size() > 0
        );
    }
}