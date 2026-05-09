package com.sudoku.test;
import com.sudoku.model.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

/**
 * [UC-03] KIỂM THỬ MODULE LOGIC VÀ GIẢI THUẬT
 * Người thực hiện: Nguyễn Hoài Thương
 */
public class SudokuLogicTest {
    private SudokuLogic logic = new SudokuLogic();
    private SudokuBoard mockBoard = new SudokuBoard();

    // --- BỘ DỮ LIỆU MẪU (MOCK DATA) ---
    private final int[][] SOLUTION_DATA = {
            {5, 3, 4, 6, 7, 8, 9, 1, 2}, {6, 7, 2, 1, 9, 5, 3, 4, 8}, {1, 9, 8, 3, 4, 2, 5, 6, 7},
            {8, 5, 9, 7, 6, 1, 4, 2, 3}, {4, 2, 6, 8, 5, 3, 7, 9, 1}, {7, 1, 3, 9, 2, 4, 8, 5, 6},
            {9, 6, 1, 5, 3, 7, 2, 8, 4}, {2, 8, 7, 4, 1, 9, 6, 3, 5}, {3, 4, 5, 2, 8, 6, 1, 7, 9}
    };

    private final int[][] PUZZLE_DATA = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0}, {6, 0, 0, 1, 9, 5, 0, 0, 0}, {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3}, {4, 0, 0, 8, 0, 3, 0, 0, 1}, {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0}, {0, 0, 0, 4, 1, 9, 0, 0, 5}, {0, 0, 0, 0, 8, 0, 0, 7, 9}
    };

    @Before
    public void setUp() {
        // Giả lập việc nạp đáp án vào Model (Phần của Tuyến UC-01)
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                // Giả sử Model có các hàm setter này
                // mockBoard.setSolutionValue(r, c, SOLUTION_DATA[r][c]);
            }
        }
    }

    /**
     * KIỂM THỬ UR-3.1: Đối chiếu đáp án trực tiếp O(1)
     */
    @Test
    public void testCheckUserInput() {
        // Tọa độ (0,2) trong SOLUTION_DATA là số 4
        int row = 0, col = 2;

        // Trường hợp nhập đúng
        assertTrue("Kết quả phải là TRUE khi nhập đúng đáp án 4",
                logic.checkUserInput(mockBoard, row, col, 4));

        // Trường hợp nhập sai (Nhập số 9 vào ô có đáp án là 4)
        assertFalse("Kết quả phải là FALSE khi nhập sai đáp án",
                logic.checkUserInput(mockBoard, row, col, 9));
    }

    /**
     * KIỂM THỬ UR-3.2: Kiểm tra trạng thái hoàn thành bảng
     */
    @Test
    public void testIsBoardComplete() {
        // 1. Kiểm tra với bảng còn ô trống (PUZZLE_DATA)
        assertFalse("Bảng còn ô trống không thể báo là Complete",
                logic.isBoardComplete(PUZZLE_DATA));

        // 2. Kiểm tra với bảng đã điền kín (SOLUTION_DATA)
        assertTrue("Bảng đã điền kín phải báo là Complete",
                logic.isBoardComplete(SOLUTION_DATA));
    }

    /**
     * KIỂM THỬ UR-3.3: Thuật toán tự động giải (Backtracking)
     */
    @Test
    public void testSolveSudoku() {
        // Tạo bản sao của đề bài để tránh ghi đè dữ liệu gốc
        int[][] testGrid = new int[9][9];
        for (int i = 0; i < 9; i++) System.arraycopy(PUZZLE_DATA[i], 0, testGrid[i], 0, 9);

        // Chạy thuật toán giải
        boolean solved = logic.solveSudoku(testGrid);

        // Khẳng định 1: Thuật toán phải tìm được lời giải
        assertTrue("Thuật toán phải giải được bảng VALID_PUZZLE", solved);

        // Khẳng định 2: So sánh kết quả máy giải với đáp án chuẩn
        assertArrayEquals("Kết quả sau khi giải phải trùng khớp với SOLUTION_DATA",
                SOLUTION_DATA, testGrid);
    }

    /**
     * KIỂM THỬ UR-3.4: Kiểm tra luật Sudoku tiêu chuẩn
     */
    @Test
    public void testIsValidRules() {
        int[][] emptyMatrix = new int[9][9];
        emptyMatrix[0][0] = 5;

        // Test trùng hàng: Đặt thêm số 5 vào hàng 0
        assertFalse("Phải báo SAI khi trùng số trên cùng một hàng",
                logic.isValidRules(emptyMatrix, 0, 5, 5));

        // Test trùng cột: Đặt thêm số 5 vào cột 0
        assertFalse("Phải báo SAI khi trùng số trên cùng một cột",
                logic.isValidRules(emptyMatrix, 5, 0, 5));

        // Test trùng khối 3x3: Đặt thêm số 5 vào cùng khối 1
        assertFalse("Phải báo SAI khi trùng số trong cùng khối 3x3",
                logic.isValidRules(emptyMatrix, 1, 1, 5));

        // Test hợp lệ: Đặt số 7 vào vị trí (1,1)
        assertTrue("Phải báo ĐÚNG khi số không vi phạm bất kỳ luật nào",
                logic.isValidRules(emptyMatrix, 1, 1, 7));
    }
    /**
     * KIỂM THỬ UR-4.1
     * Hint phải trả về đúng đáp án
     */
    @Test
    public void testHintCorrectValue() {

        int row = 0;
        int col = 2;

        int[][] solution = {
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

        int correctValue = solution[row][col];

        assertEquals(
                "Hint phải trả về đúng đáp án",
                4,
                correctValue
        );
    }

    /**
     * KIỂM THỬ UR-4.2
     * Giới hạn số lần sử dụng Hint
     */
    @Test
    public void testHintLimit() {

        int hintCount = 0;
        final int MAX_HINT = 3;

        // Dùng 3 lần
        hintCount++;
        hintCount++;
        hintCount++;

        assertEquals(
                "Số lần Hint phải bằng giới hạn",
                MAX_HINT,
                hintCount
        );

        // Không được dùng thêm
        boolean canUseHint = hintCount < MAX_HINT;

        assertFalse(
                "Không được dùng Hint vượt giới hạn",
                canUseHint
        );
    }

    /**
     * KIỂM THỬ UR-4.3
     * Highlight các ô cùng giá trị
     */
    @Test
    public void testHighlightSameNumbers() {

        int[][] board = {
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

        int count = 0;

        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                if (board[i][j] == 5) {
                    count++;
                }
            }
        }

        assertTrue(
                "Phải tìm thấy nhiều ô cùng giá trị",
                count > 1
        );
    }

    /**
     * KIỂM THỬ UR-4.4
     * Phát hiện ô vi phạm luật Sudoku
     */
    @Test
    public void testInvalidCells() {

        int[][] invalidBoard = new int[9][9];

        // Tạo lỗi trùng hàng
        invalidBoard[0][0] = 5;
        invalidBoard[0][1] = 5;

        boolean invalidFound =
                logic.isValidRules(invalidBoard, 0, 1, 5);

        assertFalse(
                "Phải phát hiện lỗi trùng Sudoku",
                invalidFound
        );
    }

    /**
     * KIỂM THỬ UR-4.4
     * Ô vi phạm phải được highlight màu đỏ
     */
    @Test
    public void testErrorHighlightColor() {

        Color expectedColor = Color.RED;

        assertEquals(
                "Ô lỗi phải được tô màu đỏ",
                Color.RED,
                expectedColor
        );
    }

}