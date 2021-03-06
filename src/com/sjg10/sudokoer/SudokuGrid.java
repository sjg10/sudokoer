/**
 *  Sudokoer
 *  Component SudokuGrid
 *  (C) 2014 by Samuel Gonshaw (sjg10@imperial.ac.uk) and Yi Zhang (yi.zhang7210@gmail.com)
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
import java.util.Arrays;
import java.util.Stack;

public class SudokuGrid implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int initialGrid[][]=new int[9][9];
	public int solutionGrid[][]=new int[9][9];
	public boolean solved=false;
	private static final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23};

	public SudokuGrid(int[][] grid){
		initialGrid=grid.clone();
		for(int i=0;i<9;i++){
			for(int j=0;j<9;j++){
			solutionGrid[i][j]=0;
		}}
	}

	public void solve(SolutionActivity act){
		if (!solved){
			SudokuSolver solver=new SudokuSolver(act);
			solver.execute(this);
		}
	}
	public Stack<SudokuElement> initialGridToStack(){
		Stack<SudokuElement> returnStack = new Stack<SudokuElement>();
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				if (initialGrid[i][j]!=0)
					returnStack.addElement(new SudokuElement(new int[]{i,j},initialGrid[i][j],true));
			}
		}
		return returnStack;
	}

	public void solvedGridFromStack(Stack<SudokuElement> stack){
		SudokuElement elt=null;
		while(!stack.empty()){
			elt=stack.pop();
			solutionGrid[elt.location[0]][elt.location[1]]=elt.content;
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

	public static boolean isConsistent(Stack<SudokuElement> sudokuStack){
		if (sudokuStack.isEmpty()) return true;
		long[] products = new long[27];
		Arrays.fill(products, 1);
		for(SudokuElement element : sudokuStack){
			products[element.location[0]] *= primes[element.content-1];
			products[element.location[1]+9] *= primes[element.content-1];
			products[element.location[0]/3 * 3 + element.location[1]/3 + 18] *= primes[element.content-1];
		}
		boolean out = true;
		for(long iterator : products){
			for (int divisor : primes){
				if(iterator % (divisor*divisor)==0){
					return false;
				}
			}
		}
		return out;
	}

}
