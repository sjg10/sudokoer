package com.example.sudokoer;

import java.io.Serializable;

public class SudokuGrid implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String initialGrid[][];
	public String solutionGrid[][];

	public SudokuGrid(String[][] grid){
		initialGrid=grid;
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (initialGrid[i][j].length()==0)
					initialGrid[i][j]="";

			}
		}
		solutionGrid=grid;
	}

	public void solve(){
		//TODO: Complete (with Yi)
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				solutionGrid[i][j]="";
			}
		}
	}

	public String initialGridString(){
		String out="-------------------\n";

		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (j%3==0)
					out=out.concat("| ");
				out= (initialGrid[i][j]=="")?out.concat("  ") :out.concat(initialGrid[i][j]+" ");
			}
			if((i+1)%3==0)
				out=out.concat("|\n-------------------\n");
			else
				out=out.concat("|\n");
		}
		return out;
	}

}
