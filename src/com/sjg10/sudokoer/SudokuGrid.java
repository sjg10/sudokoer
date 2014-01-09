/**
 *  Sudokoer
 *  Component SudokuGrid
 *  (C) 2014 by Samuel Gonshaw (sjg10@imperial.ac.uk)
 *  
 *  Sudokoer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Sudokoer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Sudokoer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sjg10.sudokoer;

import java.io.Serializable;

public class SudokuGrid implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int initialGrid[][];
	public int solutionGrid[][];
	public boolean solved=false;

	public SudokuGrid(int[][] grid){
		initialGrid=grid;
		solutionGrid=grid;
	}

	public void solve(SolutionActivity act){
		if (!solved){
			SudokuSolver solver=new SudokuSolver(act);
			solver.execute(this);
		}
	}

	public String initialGridString(){
		String out="-------------------\n";

		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (j%3==0)
					out=out.concat("| ");
				out= (initialGrid[i][j]==0)?out.concat("  ") :out.concat(Integer.toString(initialGrid[i][j])+" ");
			}
			if((i+1)%3==0)
				out=out.concat("|\n-------------------\n");
			else
				out=out.concat("|\n");
		}
		return out;
	}

}
