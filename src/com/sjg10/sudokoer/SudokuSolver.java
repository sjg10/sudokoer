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

import com.example.sudokoer.R;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

public class SudokuSolver extends AsyncTask<SudokuGrid, Integer, String[][]> {
	private SolutionActivity parent;
	private ProgressDialog dialog;
	private SudokuSolver me;
	private SudokuGrid grid;
	
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
		dialog.show();
	}
	
	@Override
	protected String[][] doInBackground(SudokuGrid... sg) {
		grid=sg[0];
		String[][] solutionGrid=new String[9][9];
		//TODO: Replace following test lines with actual solver:
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				solutionGrid[i][j]="";
			}
		}
		if (isCancelled()) return null;
		return solutionGrid;
	}
	
	protected void onPostExecute(String[][] solutionGrid) {
		if (solutionGrid!=null){
		for (int i=0;i<9;i++){
			for (int j=0;j<9;j++){
				parent.elements[i][j].setText(solutionGrid[i][j]);
			}
		}
		parent.solved=true;
		parent.btn.setText(parent.getResources().getString(R.string.newpuzzle));
		grid.solved=true;
		grid.solutionGrid=solutionGrid;
		}
		dialog.dismiss();
		parent.btn.setEnabled(true);
	}
	@Override
	protected void onCancelled(){
		Toast.makeText(parent, "Cancelled", Toast.LENGTH_SHORT).show();
	}
}
