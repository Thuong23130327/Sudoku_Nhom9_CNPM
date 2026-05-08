package com.sudoku.model;

import java.util.Random;
import com.sudoku.model.Utility;

public class SudokuGenerator {
    private int[][] board;
    private Random random = new Random();

    // Lưu đáp án hoàn chỉnh
    private int[][] solution;

    public SudokuGenerator() {
        board = new int[9][9];
    }

    // Tạo đề sudoku ngẫu nhiên
    public int[][] generate(int missingDigits) {

        // Bước 1: Copy bàn cờ gốc vào board hiện tại
        for (int i = 0; i < 9; i++) {
            System.arraycopy(Utility.BASE_GRID[i], 0, board[i], 0, 9);
        }

        // Bước 2: Thực hiện các phép biến đổi ngẫu nhiên (Shuffle)
        // Các phép này thay đổi vị trí số nhưng KHÔNG phá vỡ luật Sudoku
        shuffleRows();          // Trộn các hàng trong cùng một nhóm
        shuffleCols();          // Trộn các cột trong cùng một nhóm
        shuffleGroupRows();     // Trộn các nhóm hàng lớn (Band)
        shuffleGroupCols();     // Trộn các nhóm cột lớn (Stack)

        // Xoay bàn cờ ngẫu nhiên
        if (random.nextBoolean()) {
            transpose();
        }

        // LƯU ĐÁP ÁN HOÀN CHỈNH TRƯỚC KHI XÓA Ô 
        solution = copyBoard(board);

        // Bước 3: Xóa bớt số để tạo thành đề bài
        removeDigits(missingDigits);

        return board;
    }


    public int[][] getSolution() {
        return solution;
    }
    private int[][] copyBoard(int[][] source) {

        int[][] copy = new int[9][9];

        for (int i = 0; i < 9; i++) {

            System.arraycopy(source[i], 0, copy[i], 0, 9);
        }

        return copy;
    }

    // 1. Trộn hàng: Chỉ được đổi chỗ các hàng trong cùng một nhóm 3x3
    // Ví dụ: Có thể đổi hàng 0 với 1, nhưng KHÔNG được đổi hàng 0 với 5
    private void shuffleRows() {
        for (int group = 0; group < 9; group += 3) {

            // Lặp vài lần để xáo trộn kỹ
            for (int k = 0; k < 3; k++) {

                int row1 = group + random.nextInt(3);
                int row2 = group + random.nextInt(3);

                swapRows(row1, row2);
            }
        }
    }

    private void swapRows(int r1, int r2) {

        if (r1 == r2) return;

        int[] temp = board[r1];
        board[r1] = board[r2];
        board[r2] = temp;
    }

    // 2. Trộn cột: Chỉ đổi cột trong cùng một nhóm 3x3 (theo dọc)
    private void shuffleCols() {

        for (int group = 0; group < 9; group += 3) {

            for (int k = 0; k < 3; k++) {

                int col1 = group + random.nextInt(3);
                int col2 = group + random.nextInt(3);

                swapCols(col1, col2);
            }
        }
    }

    private void swapCols(int c1, int c2) {

        if (c1 == c2) return;

        for (int i = 0; i < 9; i++) {

            int temp = board[i][c1];

            board[i][c1] = board[i][c2];
            board[i][c2] = temp;
        }
    }

    // 3. Trộn Group: Đổi chỗ nguyên cả cụm 3 hàng (0-2)
    // với cụm (3-5) hoặc (6-8)
    private void shuffleGroupRows() {

        for (int i = 0; i < 3; i++) {

            int group1 = random.nextInt(3); // 0, 1, hoặc 2
            int group2 = random.nextInt(3);

            if (group1 != group2) {
                swapGroupRows(group1, group2);
            }
        }
    }

    private void swapGroupRows(int b1, int b2) {

        // Mỗi group có 3 hàng.
        // Group 0: row 0,1,2
        // Group 1: row 3,4,5...

        int start1 = b1 * 3;
        int start2 = b2 * 3;

        for (int i = 0; i < 3; i++) {

            swapRows(start1 + i, start2 + i);
        }
    }

    // 4. Trộn Stack: Đổi chỗ nguyên cả cụm 3 cột
    private void shuffleGroupCols() {

        for (int i = 0; i < 3; i++) {

            int stack1 = random.nextInt(3);
            int stack2 = random.nextInt(3);

            if (stack1 != stack2) {
                swapGroupCols(stack1, stack2);
            }
        }
    }

    private void swapGroupCols(int s1, int s2) {

        int start1 = s1 * 3;
        int start2 = s2 * 3;

        for (int i = 0; i < 3; i++) {

            swapCols(start1 + i, start2 + i);
        }
    }

    // 5. Chuyển vị: Đổi hàng thành cột
    // (Xoay qua đường chéo chính)
    private void transpose() {

        for (int i = 0; i < 9; i++) {

            for (int j = i + 1; j < 9; j++) {

                int temp = board[i][j];

                board[i][j] = board[j][i];
                board[j][i] = temp;
            }
        }
    }

    private void removeDigits(int count) {

        int temp = count;

        while (temp > 0) {

            int cellId = random.nextInt(81);

            int i = (cellId / 9);
            int j = cellId % 9;

            if (board[i][j] != 0) {

                board[i][j] = 0;

                temp--;
            }
        }
    }

}