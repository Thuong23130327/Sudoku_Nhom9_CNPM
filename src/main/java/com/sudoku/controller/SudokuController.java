package controller;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import model.SudokuEngine;
import model.SudokuGenerator;
import view.SudokuFrame;

public class SudokuController {
    private SudokuFrame view;
    private SudokuEngine engine;
    private SudokuGenerator generator;
    
    // Biến dùng để theo dõi trạng thái đang chạy hay đang dừng
    private boolean isRunning = false;
    
    // Lưu trữ trạng thái đề bài ban đầu để phục vụ chức năng Làm mới (Reset Game)
    private int[][] currentMatrix;

    public SudokuController(SudokuFrame view) {
        this.view = view;
        // Khởi tạo các thành phần Model
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();
        
        // Gắn sự kiện cho các nút bấm
        initController();
    }

    private void initController() {
        // ==========================================================
        // UR-1.1: Xử lý nút "Tạo Mới" (Generate Game)
        // ==========================================================
        view.getBtnGenerate().addActionListener(e -> {
            if (isRunning) return; // Nếu đang giải thì không cho bấm
            
            // Xóa ngẫu nhiên 40 ô để tạo currentMatrix
            int[][] newBoard = generator.generate(40); 
            
            // Khởi tạo và lưu lại bản sao của đề bài vào currentMatrix
            currentMatrix = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(newBoard[i], 0, currentMatrix[i], 0, 9);
            }

            // setBoardData sẽ tự động cấu hình isFixedCell = true (setEditable = false)
            view.setBoardData(currentMatrix);
            view.updateStatus("Đã tạo mới (ẩn 40 ô). Nhấn Giải để bắt đầu.");
        });

        // ==========================================================
        // UR-1.2: Xử lý nút "Làm Mới" (Reset Game)
        // ==========================================================
        view.getBtnReset().addActionListener(e -> {
            if (isRunning) return;
            
            if (currentMatrix != null) {
                // Đưa mảng gốc vào lại, hàm setBoardData sẽ đè lại giao diện,
                // tự động xóa các ô người chơi đã nhập và giữ nguyên ô đề bài
                view.setBoardData(currentMatrix);
                view.updateStatus("Đã làm mới ván chơi về trạng thái ban đầu!");
            } else {
                view.updateStatus("Chưa có ván đấu nào để làm mới!");
            }
        });

        // 3. Xử lý nút "GIẢI / DỪNG"
        view.getBtnSolve().addActionListener(e -> {
            if (isRunning) {
                // Nếu đang chạy -> Bấm để DỪNG
                stop();
            } else {
                // Nếu đang dừng -> Bấm để CHẠY
                start();
            }
        });
    }

    private void start() {
        // BƯỚC 1: Lấy dữ liệu từ giao diện (bao gồm cả số người dùng tự nhập)
        int[][] inputBoard = view.getBoardData();
        
        // BƯỚC 2: Format lại giao diện
        view.setBoardData(inputBoard);

        // BƯỚC 3: Cấu hình Engine để cập nhật giao diện khi chạy
        engine.setOnGenerationEvolved(ind -> {
            SwingUtilities.invokeLater(() -> {
                view.updateBoardFromIndividual(ind);
                view.updateStatus("Fitness: " + ind.getFitness() + "/162 | Gen: đang chạy...");
            });
        });

        // Cập nhật trạng thái nút bấm
        isRunning = true;
        view.getBtnSolve().setText("DỪNG");
        view.getBtnGenerate().setEnabled(false);
        view.getBtnReset().setEnabled(false); // Đổi tên nút

        // BƯỚC 4: Chạy thuật toán trên luồng riêng (SwingWorker)
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Hàm này sẽ chạy tốn thời gian
                engine.solve(inputBoard);
                return null;
            }

            @Override
            protected void done() {
                // Kết thúc:tìm thành công hoặc bị dừng
                stop();
                view.updateStatus("Thành công hoặc đã dừng!");
            }
        };
        
        worker.execute();
    }

    private void stop() {
        engine.stop(); // Gửi lệnh dừng vào Model
        isRunning = false;
        
        // Reset lại giao diện nút bấm
        view.getBtnSolve().setText("GIẢI (Start)");
        view.getBtnGenerate().setEnabled(true);
        view.getBtnReset().setEnabled(true); // Đổi tên nút
    }
}