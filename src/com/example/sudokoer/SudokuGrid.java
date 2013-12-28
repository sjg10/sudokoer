package com.example.sudokoer;

public class SudokuGrid {
	private String initialGrid[][];
	private String solutionGrid[][];

	public SudokuGrid(String[][] grid){
		initialGrid=grid;
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (initialGrid[i][j].length()==0)
					initialGrid[i][j]="o";

			}
		}
		solutionGrid=grid;
	}
	
	public String[][] solve(){
		//TODO: Complete (Yi)
		return solutionGrid;
	}
	
	public String initialGridString(){
		String out="-------------------\n";

		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (j%3==0)
					out=out.concat("| ");
				out= out.concat(initialGrid[i][j]+" ");
			}
			if((i+1)%3==0)
				out=out.concat("|\n-------------------\n");
			else
				out=out.concat("|\n");
		}
		return out;
	}
	
}
