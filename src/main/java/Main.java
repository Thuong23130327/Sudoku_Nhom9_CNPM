import com.sudoku.controller.SudokuController;
import javax.swing.SwingUtilities;
import com.sudoku.view.SudokuFrame;
import com.sudoku.view.WelcomeDialog;

public class Main {
    public static void main(String[] args) {
        // 1.1.0: Tiền điều kiện (Current State): Ứng dụng đã khởi chạy thành công, hệ thống đang ở trạng thái chờ (IDLE).
        SwingUtilities.invokeLater(() -> {
            WelcomeDialog welcome = new WelcomeDialog(null);
            welcome.setVisible(true);

            int missingDigits = welcome.getSelectedMissingDigits();
            if (missingDigits != -1) {
                SudokuFrame view = new SudokuFrame();
                SudokuController controller = new SudokuController(view);
                view.setVisible(true);
                // Tạo bảng lần đầu theo độ khó
                controller.generateBoardWithDifficulty(missingDigits);
            } else {
                // 1.3.3: Main nhận kết quả missingDigits == -1 khi khởi động và gọi System.exit(0) để thoát ứng dụng.
                System.exit(0);
            }
        });
    }
}
