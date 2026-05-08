package model;

import java.util.function.Consumer;

import model.Individual;
import model.Population;

public class SudokuEngine {
    private boolean isRunning = false;

    private Consumer<Individual> onGenerationEvolved;

    public Consumer<Individual> getOnGenerationEvolved() {
		return onGenerationEvolved;
	}

    public void setOnGenerationEvolved(Consumer<Individual> callback) {
        this.onGenerationEvolved = callback;
    }

    public void stop() {
        isRunning = false;
    }

    public void solve(int[][] initialBoard) {
        isRunning = true;
        
        Population pop = new Population(initialBoard);
        
        int generation = 0;
        int bestFitness = 0;
        int count = 0;

        while (isRunning) {
            // Tiến hóa thế hệ tiếp theo
            pop.evolve();

            // Lấy ra cá thể tốt nhất hiện tại
            Individual best = pop.getIndividuals().get(0);
            int currentFitness = best.getFitness();

            // Gửi cá thể tốt nhất ra ngoài cho Controller/View cập nhật
            if (onGenerationEvolved != null) {
                onGenerationEvolved.accept(best);
            }

            // Kiểm tra điều kiện dừng
            if (currentFitness == 162) {
                System.out.println("Giải thành công tại thế hệ: " + generation);
                isRunning = false;
                break;
            }

            // Xử lý kẹt
            if (currentFitness == bestFitness) {
                count++;
            } else {
                bestFitness = currentFitness;
                count = 0;
            }

            // Nếu điểm không đổi trong 100 thế hệ -> Kẹt -> Tạo quần thể mới hoàn toàn
            if (count > 100) {
                System.out.println("!!!Không thành công, số điểm cao nhất " + currentFitness + "! Khởi tạo quần thể mới...");
                pop = new Population(initialBoard);
                count = 0;
                bestFitness = 0;
                generation = 0;
            }
            generation++;
        }
    }
}