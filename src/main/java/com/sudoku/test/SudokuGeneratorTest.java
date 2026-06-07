package com.sudoku.test;

import com.sudoku.model.SudokuGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

public class SudokuGeneratorTest {

    @Test
    public void testGenerateCorrectDimensions() {
        SudokuGenerator generator = new SudokuGenerator();
        int[][] board = generator.generate(20);

        assertEquals("Bàn cờ phải có đúng 9 hàng", 9, board.length);
        assertEquals("Bàn cờ phải có đúng 9 cột", 9, board[0].length);
    }

    @Test
    public void testGenerateMissingDigits() {
        SudokuGenerator generator = new SudokuGenerator();

        // Kiểm tra tạo ván đấu mức Dễ (20 ô trống)
        int[][] boardEasy = generator.generate(20);
        assertEquals("Mức dễ phải đục đúng 20 lỗ", 20, countEmptyCells(boardEasy));

        // Kiểm tra tạo ván đấu mức Khó (50 ô trống)
        int[][] boardHard = generator.generate(50);
        assertEquals("Mức khó phải đục đúng 50 lỗ", 50, countEmptyCells(boardHard));
    }

    @Test
    public void testSolutionIsComplete() {
        SudokuGenerator generator = new SudokuGenerator();
        generator.generate(35); // Tạo đề trung bình
        int[][] solution = generator.getSolution();

        // Đảm bảo mảng đáp án không được chứa bất kỳ ô trống (số 0) nào
        assertEquals("Đáp án gốc không được chứa ô trống", 0, countEmptyCells(solution));
    }

    // Hàm phụ trợ đếm số ô trống (giá trị 0) trên bàn cờ
    private int countEmptyCells(int[][] board) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    count++;
                }
            }
        }
        return count;
    }
}