package com.sudoku.test;

import com.sudoku.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SudokuAutomationTest {
    private SudokuLogic logic = new SudokuLogic();

    private int[][] matrix;
    private boolean expectedValid;
    private String testName;

    public SudokuAutomationTest(String testName, int[][] matrix, boolean expectedValid) {
        this.testName = testName;
        this.matrix = matrix;
        this.expectedValid = expectedValid;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Test Bảng Hợp Lệ", SudokuTestData.VALID_BOARD, true },
                { "Test Trùng Hàng (UR-3.4)", SudokuTestData.INVALID_ROW, false },
                { "Test Trùng Cột (UR-3.4)", SudokuTestData.INVALID_COL, false },
                { "Test Trùng Khối (UR-3.4)", SudokuTestData.INVALID_BLOCK_BOARD, false },
                { "Test Trạng Thái Chưa Xong (UR-3.2)", SudokuTestData.NEARLY_COMPLETE_BOARD, false },
                { "Test Bảng Đã Xong (UR-3.2)", SudokuTestData.VALID_BOARD, true }
        });
    }

    @Test
    public void verifySudokuValidation() {
        // Kiểm tra tính đầy đủ (UR-3.2)
        if (testName.contains("Trạng Thái") || testName.contains("Đã Xong")) {
            assertEquals("Lỗi kiểm tra hoàn thành tại: " + testName,
                    expectedValid, logic.isBoardComplete(matrix));
        }
        // Kiểm tra luật Sudoku (UR-3.4)
        else {
            boolean result = logic.isValidRules(matrix, 0, 0, matrix[0][0] == 0 ? 1 : matrix[0][0]);
            assertEquals("Thất bại tại kịch bản: " + testName, expectedValid, result);
        }
    }

    @Test
    public void verifyAutoSolverPerformance() {
        if (testName.equals("Test Bảng Hợp Lệ")) {
            int[][] puzzle = new int[9][9];
            for(int i=0; i<9; i++) System.arraycopy(SudokuTestData.PUZZLE_TO_SOLVE[i], 0, puzzle[i], 0, 9);

            long startTime = System.currentTimeMillis();
            boolean isSolved = logic.solveSudoku(puzzle);
            long endTime = System.currentTimeMillis();

            assertTrue("Thuật toán solver không giải được bảng!", isSolved);
            System.out.println("Thời gian giải tự động: " + (endTime - startTime) + "ms");

            assertArrayEquals("Kết quả máy giải không khớp đáp án chuẩn!", SudokuTestData.VALID_BOARD, puzzle);
        }
    }
}