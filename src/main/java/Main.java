
import com.sudoku.controller.SudokuController;

import javax.swing.SwingUtilities;

import com.sudoku.view.SudokuFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuFrame view = new SudokuFrame();
            new SudokuController(view);
            view.setVisible(true);
        });
    }
}
