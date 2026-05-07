package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Population {
	private List<Individual> individuals;

	public Population(int[][] board) {
		super();
		this.individuals = new ArrayList<>();
		
		for (int i = 0; i < Utility.POPULATION_SIZE; i++) {
			this.individuals.add(new Individual(board));
		}
	}

	public List<Individual> getIndividuals() {
		return individuals;
	}

	public void setIndividuals(List<Individual> individuals) {
		this.individuals = individuals;
	}
	
	public List<Individual> crossover() {
		List<Individual> children = new ArrayList<>();
		int half = Utility.POPULATION_SIZE / 2;
        
        //Tách quần thể ban đầu thành quần thể cha và mẹ
        List<Individual> fathers = individuals.subList(0, half);
        List<Individual> mothers = individuals.subList(half, Utility.POPULATION_SIZE);
        
        //Ghép cặp từng cha với từng mẹ
        for (int i = 0; i < fathers.size(); i++) {
        	for (int j = 0; j < mothers.size(); j++) {
				Individual child1 = createChild(fathers.get(i), mothers.get(j));
				Individual child2 = createChild(mothers.get(i), fathers.get(j));
				children.add(child1);
				children.add(child2);
			}
		}
		return children;
	}

	private Individual createChild(Individual father, Individual mother) {
		// TODO Auto-generated method stub
		Individual child = new Individual();
		int split = Utility.SPLIT;
		
		for (int i = 0; i < split; i++) {
			// Lấy gene tại vị trí i của bố đưa vào con
			child.getGenes().add(new Gene(father.getGenes().get(i)));
		}
		for (int i = split; i < 9; i++) {
			child.getGenes().add(new Gene(mother.getGenes().get(i)));
		}
        return child;
	}
	
	public void evolve() {
        // 1. Lai ghép để tạo con cái
        List<Individual> children = crossover();

        // 2. Gộp Cha mẹ và Con cái vào một danh sách lớn (Pool)
        List<Individual> largePool = new ArrayList<>(this.individuals);
        largePool.addAll(children);

        // 3. Đột biến và Tính điểm cho TOÀN BỘ pool
        for (Individual ind : largePool) {
            ind.mutate(Utility.MUTATION_RATE); // Đột biến
            ind.calculateFitness();            // Tính điểm lại sau khi đột biến
        }

        // 4. Sắp xếp theo Fitness (từ cao xuống thấp)
        Collections.sort(largePool);

        // 5. Chọn lọc tự nhiên (Natural Selection)
        // Chỉ giữ lại POPULATION_SIZE cá thể tốt nhất cho thế hệ sau
        this.individuals = new ArrayList<>();
        for (int i = 0; i < Utility.POPULATION_SIZE && i < largePool.size(); i++) {
            this.individuals.add(largePool.get(i));
        }
        
        // In ra cá thể tốt nhất thế hệ này để theo dõi
        System.out.println("Best Fitness: " + individuals.get(0).getFitness() + "/162");
    }
	
}
