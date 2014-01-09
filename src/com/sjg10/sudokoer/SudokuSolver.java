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
	private boolean failed;

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
		grid=sg[0];
		Stack<SudokuElement> stack=grid.initialGridToStack();
		Stack<SudokuElement> tempStack=new Stack<SudokuElement>();
		if(!SudokuGrid.isConsistent(stack)){//bad input!
			failed=true;
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
			}
			if (stack.isEmpty()){//backtrack failed...no guesses at all left!
				failed=true;
				return null;
			}
			if (isCancelled()) return null;
		}
		if (isCancelled()) return null;
		grid.solvedGridFromStack(stack);
		return grid.solutionGrid;
	}

	private Stack<SudokuElement> makeNewGuess(Stack<SudokuElement> sudokuStack) {
		int contentGuess=0;
		Stack<SudokuElement> stack=sudokuStack;
		if(stack.isEmpty()){
			//no guesses (or knowns) at all have been made!
			stack.push(new SudokuElement(new int[]{0, 0},1, false));
			return stack;
		}
		do{contentGuess++;
		stack =sudokuStack;

		Stack<SudokuElement> tempStack=new Stack<SudokuElement>();//to help store what weve checked
		SudokuElement tempElement=null;//for now, iterator to get to last guess
		do{tempElement=stack.pop();
		tempStack.push(tempElement);
		}while(tempElement.isDefinite==true && !stack.isEmpty());
		//now tempElement will be our new guess

		if(stack.isEmpty())//no guess has yet been made!
			tempElement=new SudokuElement(new int[]{0, -1},1, false);
		else
			tempElement.content=contentGuess;
		
		//Now find a position for it where no initial gridspace has been filled
		//At a larger index than our last guess
		do{
			tempElement.location[1]++;
			if (tempElement.location[1]==9){
				tempElement.location[1]=0;
				tempElement.location[0]++;
			}
			if (tempElement.location[0]==9){
				//no new guesses: return empty stack
				Log.e("GUESSES","NONE");
				return new Stack<SudokuElement>();
			}
		}while (grid.initialGrid[tempElement.location[0]][tempElement.location[1]]!=0);
		Log.e("Element",tempElement.toString());
		//Now lets put the top back on stack, with our new guess
		
		while(!tempStack.isEmpty())
			stack.push(tempStack.pop());
		stack.push(tempElement);
		}while (!SudokuGrid.isConsistent(stack));
		return stack;
	}

	private Stack<SudokuElement> backtrackGuesses(Stack<SudokuElement> stack) {
		//Returns empty stack if we have run out of guesses!
		SudokuElement tempElement=null;
		boolean guessUpdated=false;
		boolean moveDownStack=false;
		//we find the last guess (before the consequences of that guess)
		while(!guessUpdated){
			if (stack.isEmpty())
				//we have run out of guesses!
				break;
			tempElement=stack.pop();
			if(!tempElement.isDefinite){
				//we replace it by another guess
				tempElement.content++;
				if (tempElement.content>9){
					while (grid.initialGrid[tempElement.location[0]][tempElement.location[1]]!=0){
						tempElement.location[1]++;
						if (tempElement.location[1]==9){
							tempElement.location[1]=0;
							tempElement.location[0]++;
						}
						if (tempElement.location[0]==9){
							//this guess is useless, lets fix an earlier one instead!
							moveDownStack=true;
							break;
						}
					}

				}

				if(!moveDownStack){
					stack.push(tempElement);
					//check the added guy is consistent:
					for(SudokuElement elt: stack){
						Log.e("Element",elt.toString());
					}
					Log.e("Element","END STACK");
					if(!SudokuGrid.isConsistent(stack))
						stack.pop();
					else //keep looking
						guessUpdated=true;
				}
				else
					moveDownStack=false;//reset for next loop
			}
		}
		return stack;
	}

	protected void onPostExecute(int[][] solutionGrid) {
		if (!failed){
			parent.solved=true;
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					parent.drawCanvas(solutionGrid);
				}
			}
			parent.btn.setText(parent.getResources().getString(R.string.newpuzzle));
			grid.solutionGrid=solutionGrid;
		}
		else{//failed!
			for (int i=0;i<10;i++)
				parent.numberButtons[i].setEnabled(true);}
		dialog.dismiss();
		parent.orientationEventListener.enable();
		parent.btn.setEnabled(true);
		if(failed){
			Toast.makeText(parent, "No solution to input exists.", Toast.LENGTH_SHORT).show();
		}
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
		return sudokuStack;
	}
}
