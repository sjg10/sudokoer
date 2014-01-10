/**
 *  Sudokoer
 *  Component SudokuSolver
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


import java.util.Stack;

import com.sjg10.sudokoer.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SudokuSolver extends AsyncTask<SudokuGrid, Integer, int[][]> {
	private SolutionActivity parent;
	private ProgressDialog dialog;
	private SudokuSolver me;
	private SudokuGrid grid;
	private boolean inconsistentInput=false;
	private int[] lastGuessLocation={0,-1};

	public SudokuSolver(SolutionActivity activity){
		me=this;
		parent=activity;
		dialog=new ProgressDialog(parent);
		dialog.setOnCancelListener(new ProgressDialog.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface di) {
				me.cancel(true);
			}
		});
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Solving");
	}

	@Override
	protected void onPreExecute() {
		parent.orientationEventListener.disable();
		dialog.show();
	}

	@Override
	protected int[][] doInBackground(SudokuGrid... sg) {
		//needed because too fast!
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Log.getStackTraceString(e);
		}
		if (isCancelled()) return null;
		grid=sg[0];
		Stack<SudokuElement> stack=grid.initialGridToStack();
		Stack<SudokuElement> tempStack=new Stack<SudokuElement>();
		if(!SudokuGrid.isConsistent(stack)){//bad input!
			inconsistentInput=true;
			return null;
		}

		while(true){
			if (stack.size()==81)
				break;//we've solved it!
			tempStack=applyRules(stack);
			if (stack==tempStack){
				tempStack=makeNewGuess(stack);
				if(tempStack.isEmpty())//couldn't make new guess!
					stack=backtrackGuesses(stack);
				else
					stack=tempStack;
			}
			if (stack.isEmpty()){//backtrack failed...no guesses at all left!
				return null;
			}
			if (isCancelled()) return null;
		}
		if (isCancelled()) return null;
		grid.solvedGridFromStack(stack);
		return grid.solutionGrid;
	}

	@SuppressWarnings("unchecked")
	private Stack<SudokuElement> makeNewGuess(Stack<SudokuElement> sudokuStack) {
		Stack<SudokuElement> stack=(Stack<SudokuElement>) sudokuStack.clone();
		SudokuElement tempElement;
		if(stack.isEmpty()){
			//no guesses (or knowns) at all have been made! (empty grid inputed)
			lastGuessLocation=new int[]{0, 0};
			stack.push(new SudokuElement(lastGuessLocation,1, false));
			return stack;
		}
		tempElement=new SudokuElement(lastGuessLocation,1, false);
		//find first available spot
		do{
			tempElement.location[1]++;
			if (tempElement.location[1]==9){
				tempElement.location[1]=0;
				tempElement.location[0]++;
			}

			if (tempElement.location[0]==9){
				//no new guesses: return empty stack
				return new Stack<SudokuElement>();
			}
		}while (grid.initialGrid[tempElement.location[0]][tempElement.location[1]]!=0);
		lastGuessLocation=tempElement.location;

		stack.push(tempElement);
		//find first compatible value
		while(!SudokuGrid.isConsistent(stack)){

			tempElement=stack.pop();
			tempElement.content++;
			stack.push(tempElement);
			if (tempElement.content==10) {
				return new Stack<SudokuElement>();
			}

		};
		return stack;
	}

	@SuppressWarnings("unchecked")
	private Stack<SudokuElement> backtrackGuesses(Stack<SudokuElement> sudokuStack) {
		Stack<SudokuElement> stack=(Stack<SudokuElement>) sudokuStack.clone();
		//Returns empty stack if we have run out of guesses!
		SudokuElement tempElement=null;
		//we find the last guess (before the consequences of that guess)
		update:
			while(true){
				if (stack.isEmpty()){
					//we have run out of guesses!
					break update;}
				tempElement=stack.pop();
				if(!tempElement.isDefinite){
					//we replace it by another guess
					while(tempElement.content<9){
						tempElement.content++;
						stack.push(tempElement);
						if(SudokuGrid.isConsistent(stack)){
							lastGuessLocation=tempElement.location;
							break update;}
						else
							tempElement=stack.pop();}
				}
			}
		return stack;
	}


	protected void onPostExecute(int[][] solutionGrid) {
		if (solutionGrid!=null){
			parent.solved=true;
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					parent.drawCanvas(solutionGrid);
				}
			}
			parent.btn.setText(parent.getResources().getString(R.string.newpuzzle));
		}
		else{//failed!
			for (int i=0;i<10;i++)
				parent.numberButtons[i].setEnabled(true);

			if(inconsistentInput){
				Toast.makeText(parent, "Input inconsistent.", Toast.LENGTH_LONG).show();
			}
			else
				Toast.makeText(parent, "No solution to input exists.", Toast.LENGTH_LONG).show();}
		dialog.dismiss();
		parent.orientationEventListener.enable();
		parent.btn.setEnabled(true);

	}
	@Override
	protected void onCancelled(){
		dialog.dismiss();
		for (int i=0;i<10;i++)
			parent.numberButtons[i].setEnabled(true);
		parent.orientationEventListener.enable();
		parent.btn.setEnabled(true);
		Toast.makeText(parent, "Cancelled", Toast.LENGTH_SHORT).show();
	}

	private Stack<SudokuElement> applyRules(Stack<SudokuElement> sudokuStack){
		//TODO: add clever rules
		return sudokuStack;
	}
}
