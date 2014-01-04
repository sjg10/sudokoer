package com.example.sudokoer;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
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
		Log.e("tb",tb.toString());
		sg= (SudokuGrid) getIntent().getSerializableExtra("sudoku");
		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(1);

		//populate grid with current info
		elements=new EditText[9][9];
		for (int i=0;i<9;i++){
			TableRow tr=new TableRow(this);
			for (int j=0;j<9;j++){
				elements[i][j]=new EditText(this);
				elements[i][j].setFilters(filterArray);
				if (sg!=null)
					elements[i][j].setText(sg.initialGrid[i][j]);
				elements[i][j].setInputType(InputType.TYPE_CLASS_NUMBER);

				tr.addView(elements[i][j]);
			}
			//TODO: fix spacing of table and add borders
			tb.addView(tr);
		}
		if(sg!=null)
			Toast.makeText(this, "Review input then click solve",Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Input Sudoko then click solve",Toast.LENGTH_LONG).show();
	}
	public void solve(View view){
		if(solved){
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					sg.initialGrid[i][j]=elements[i][j].getText().toString();
					elements[i][j].setEnabled(false);
				}
			}	
			sg.solve();//TODO: run in background?
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					elements[i][j].setText(sg.solutionGrid[i][j]);
				}
			}
			solved=true;
			btn.setText("New Puzzle");
		}
		else{
			finish();
		}
	}
}


