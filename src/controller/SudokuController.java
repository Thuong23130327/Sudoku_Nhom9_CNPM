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
    
    // ==============================
    // Biến dùng cho chức năng Hint
    // ==============================
    private int hintCount = 0;
    private final int MAX_HINT = 3;

    public SudokuController(SudokuFrame view) {
        this.view = view;

        // Khởi tạo các thành phần Model
        this.engine = new SudokuEngine();
        this.generator = new SudokuGenerator();

        // Gắn sự kiện cho các nút bấm
        initController();
    }

    private void initController() {

        // 1. Xử lý nút "Tạo Mới"
        view.getBtnGenerate().addActionListener(e -> {

            if (isRunning) return; // Nếu đang giải thì không cho bấm

            // Reset số lần hint
            hintCount = 0;

            // Tạo mới
            int[][] newBoard = generator.generate(45);

            // Lưu đáp án hoàn chỉnh vào engine
            engine.setSolution(generator.getSolution());

            view.setBoardData(newBoard);

            view.updateStatus(
                    "Đã tạo mới. Hint: "
                    + hintCount + "/" + MAX_HINT);
        });

        // 2. Xử lý nút "Tự Nhập / Xóa"
        view.getBtnClear().addActionListener(e -> {

            if (isRunning) return;

            view.clearBoard();

            // Reset hint
            hintCount = 0;

            view.updateStatus("Mời bạn nhập đề sudoku...");
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

        // 4. Xử lý nút Hint hỗ trợ điền
        view.getBtnHint().addActionListener(e -> {

            if (isRunning) return;

            giveHint();
        });
    }

    private void start() {

        // BƯỚC 1: Lấy dữ liệu từ giao diện
        // (bao gồm cả số người dùng tự nhập)
        int[][] inputBoard = view.getBoardData();

        // BƯỚC 2: Format lại giao diện
        // Dòng này sẽ biến các số người dùng vừa nhập
        // thành màu XANH (Blue/Gray)
        // và KHÓA lại (setEditable=false)
        // giống như đề bài ngẫu nhiên.
        view.setBoardData(inputBoard);

        // BƯỚC 3: Cấu hình Engine
        // để cập nhật giao diện khi chạy
        engine.setOnGenerationEvolved(ind -> {

            SwingUtilities.invokeLater(() -> {

                view.updateBoardFromIndividual(ind);

                view.updateStatus(
                        "Fitness: "
                        + ind.getFitness()
                        + "/162 | Gen: đang chạy...");
            });
        });

        // Cập nhật trạng thái nút bấm
        isRunning = true;

        view.getBtnSolve().setText("DỪNG");

        view.getBtnGenerate().setEnabled(false);
        view.getBtnClear().setEnabled(false);

        // Disable Hint khi đang solve
        view.getBtnHint().setEnabled(false);

        // BƯỚC 4: Chạy thuật toán trên luồng riêng
        // (SwingWorker)
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
        view.getBtnClear().setEnabled(true);

        // Enable lại Hint
        view.getBtnHint().setEnabled(true);
    }

    // Điền đúng giá trị vào ô đang chọn
  
    private void giveHint() {

        // Giới hạn số lần hint
        if (hintCount >= MAX_HINT) {

            view.updateStatus("Đã hết lượt Hint!");

            return;
        }

        // Lấy ô đang chọn
        int row = view.getSelectedRow();
        int col = view.getSelectedCol();

        // Chưa chọn ô
        if (row == -1 || col == -1) {

            view.updateStatus("Hãy chọn 1 ô trống!");

            return;
        }

        int[][] board = view.getBoardData();

        // Ô đã có số
        if (board[row][col] != 0) {

            view.updateStatus("Ô này đã có số!");

            return;
        }

        // Lấy đáp án đúng
        int[][] solution = engine.getSolution();

        // Kiểm tra solution có tồn tại không
        if (solution == null) {

            view.updateStatus("Chưa có đáp án!");

            return;
        }

        int correctValue = solution[row][col];

        // Điền vào board
        board[row][col] = correctValue;

        // Update giao diện
        view.setCellValue(row, col, correctValue);

        // Tăng số lần hint
        hintCount++;

        view.updateStatus(
                "Đã dùng Hint: "
                + hintCount + "/" + MAX_HINT);
    }
}