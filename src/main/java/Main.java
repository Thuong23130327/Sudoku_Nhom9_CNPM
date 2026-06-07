
import javax.swing.SwingUtilities;
import com.sudoku.view.MainFrame;

public class Main {
    public static void main(String[] args) {
        // Khởi chạy ứng dụng an toàn trên Event Dispatch Thread (EDT) của Swing
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo và hiển thị Menu chính (Nơi xử lý chọn chế độ và độ khó)
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}