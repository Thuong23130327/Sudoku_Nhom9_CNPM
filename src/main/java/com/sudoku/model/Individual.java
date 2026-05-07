package model;

import java.util.ArrayList;
import java.util.*;

public class Individual implements Comparable<Individual>{
	private List<Gene> genes;
	private int fitness;

	public Individual(int[][] board) {
		super();
		this.genes = new ArrayList<>();
		
		//Khởi tạo 9 Gene (9 ô 3x3)
		for (int i = 0; i < 9; i++) {
			int[] row = board[i];
			genes.add(new Gene(row));
		}
	}
		
	public Individual() {
		this.genes = new ArrayList<>();
	}

	public List<Gene> getGenes() {
		return genes;
	}

	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}
	
	public int getFitness() {
        return fitness;
    }

    // Hàm thực hiện đột biến trên cá thể
    public void mutate(double mutationRate) {
        Random rand = new Random();
        for (Gene gene : genes) {
            // Với mỗi hàng, có xác suất mutationRate sẽ bị đột biến
            if (rand.nextDouble() < mutationRate) {
                gene.mutate();
            }
        }
    }

    // Hàm tính toán Fitness
    // Max Fitness = 162 (9 hàng đã chuẩn, giờ tính 9 cột + 9 khối 3x3)
    // Mỗi cột/khối có 9 số khác nhau -> +9 điểm. Tổng 18 * 9 = 162.
    public void calculateFitness() {
        int score = 0;

        // 1. Kiểm tra các Cột (Columns)
        for (int col = 0; col < 9; col++) {
            Set<Integer> uniqueNumbers = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                uniqueNumbers.add(genes.get(row).getNumber().get(col));
            }
            score += uniqueNumbers.size(); // Càng nhiều số khác nhau càng điểm cao
        }

        // 2. Kiểm tra các Khối 3x3 (Blocks)
        for (int blockRow = 0; blockRow < 3; blockRow++) {
            for (int blockCol = 0; blockCol < 3; blockCol++) {
                Set<Integer> uniqueNumbers = new HashSet<>();
                
                // Duyệt qua 9 ô trong khối
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int r = blockRow * 3 + i;
                        int c = blockCol * 3 + j;
                        uniqueNumbers.add(genes.get(r).getNumber().get(c));
                    }
                }
                score += uniqueNumbers.size();
            }
        }

        this.fitness = score;
    }

    // Để sắp xếp giảm dần theo Fitness
    @Override
    public int compareTo(Individual other) {
        return other.fitness - this.fitness; // Sắp xếp ngược để lấy fitness cao -> thấp
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fitness: ").append(fitness).append("\n");
        for (Gene gene : genes) {
            sb.append(gene.toString()).append("\n");
        }
        return sb.toString();
    }
}
