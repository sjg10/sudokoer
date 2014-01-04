/**
 *  Sudokoer
 *  Component SolutionActivity
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


package com.example.sudokoer;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.InputType;

public class SolutionActivity extends Activity {
	private SudokuGrid sg;
	private EditText[][] elements;
	private TableLayout tb;
	private boolean solved=false;
	private Button btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_solution);
		btn=(Button)findViewById(R.id.buttonSolve);
		tb=(TableLayout)findViewById(R.id.gridSudoku);
		sg= (SudokuGrid) getIntent().getSerializableExtra("sudoku");
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(1);
		
		
		//populate grid with current info
		//TODO: speed up somehow!
		elements=new EditText[9][9];
		EditText previous = null;
		for (int i=0;i<9;i++){
			TableRow tr=new TableRow(this);
			for (int j=0;j<9;j++){
				elements[i][j]=new EditText(this);
				elements[i][j].setBackgroundResource(R.drawable.cell_shape);
				elements[i][j].setFilters(filterArray);
				if (sg!=null)
					elements[i][j].setText(sg.initialGrid[i][j]);
				elements[i][j].setInputType(InputType.TYPE_CLASS_NUMBER);
				
				if(i!=0 && j!=0)
					elements[i][j].setNextFocusDownId(previous.getId());
				tr.addView(elements[i][j]);
				previous=elements[i][j];
			}
			//TODO: fix spacing of table and add borders
			tb.addView(tr);
		}
		elements[8][8].setNextFocusDownId(elements[0][0].getId());
		btn.setEnabled(true);
		
		if(sg!=null)
			Toast.makeText(this, "Review input then click solve",Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Input Sudoko then click solve",Toast.LENGTH_LONG).show();

	}
	public void solve(View view){
		btn.setEnabled(false);
		String tmp[][]=new String[9][9];
		if(!solved){
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					tmp[i][j]=elements[i][j].getText().toString();
					elements[i][j].setEnabled(false);
				}
			}
			if(sg==null)
				sg=new SudokuGrid(tmp);
			else
				sg.initialGrid=tmp;
			sg.solve();//TODO: run in background?
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					elements[i][j].setText(sg.solutionGrid[i][j]);
				}
			}
			solved=true;
			btn.setText(getResources().getString(R.string.newpuzzle));
			btn.setEnabled(true);
		}
		else{
			NavUtils.navigateUpFromSameTask(this);
			finish();
		}
	}
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
        	NavUtils.navigateUpFromSameTask(this);
			finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}


