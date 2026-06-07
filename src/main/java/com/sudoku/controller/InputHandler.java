package com.sudoku.controller;

import com.sudoku.model.SudokuLogic;
import com.sudoku.view.SudokuFrame;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class InputHandler {
    
    private SudokuFrame view;
    private SudokuLogic logic;

    public InputHandler(SudokuFrame view, SudokuLogic logic) {
        this.view = view;
        this.logic = logic;
        initListeners();
    }



    private void initListeners() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int row = r;
                int col = c;
                JTextField cell = view.getCell(r, c);

                cell.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) { validateCell(); }
                    @Override
                    public void removeUpdate(DocumentEvent e) { validateCell(); }
                    @Override
                    public void changedUpdate(DocumentEvent e) { validateCell(); }

                    // [2.1.7] View kích hoạt DocumentListener, chuyển sự kiện đến InputHandler.validateCell().
                    private void validateCell() {
                        SwingUtilities.invokeLater(() -> {
                            String text = cell.getText();
                            if (text.isEmpty()) {
                                view.highlightErrorCell(row, col, false);
                                return;
                            }
                            try {
                                int num = Integer.parseInt(text);
                                if (num < 1 || num > 9) {
                                    view.highlightErrorCell(row, col, true);
                                    return;
                                }

                                int[][] board = view.getBoardData();
                                board[row][col] = 0;
                                
                                boolean isValid = logic.isValidRules(board, row, col, num);
                                
                                // [2.1.8] InputHandler gọi highlightErrorCell để View phản hồi trực quan (đỏ nếu sai, bình thường nếu đúng)
                                view.highlightErrorCell(row, col, !isValid);
                            } catch (NumberFormatException ex) {
                                view.highlightErrorCell(row, col, true);
                            }
                        });
                    }
                });
            }
        }
    }
}
