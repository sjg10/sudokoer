/**
 *  Sudokoer
 *  Component SolutionActivity
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

import com.sjg10.sudokoer.R;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class SolutionActivity extends Activity implements SurfaceHolder.Callback, View.OnTouchListener{
	private SudokuGrid sg;
	public boolean solved=false;
	public Button btn;
	private SurfaceHolder holder;
	private Paint thick;
	private Paint thin;
	private Paint red;
	private Paint background;
	private Paint textSolved;
	private Paint textInitial;
	private SurfaceView view;
	public Button[] numberButtons=new Button[10];
	private int[] selectedSquare={0,0};//Will be {9,x} iff no square selected
	private int[] topLeft={0,0};
	private int size=0;
	private int textVerticalAdjust=0;
	public OrientationEventListener orientationEventListener;
	private Paint gridBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_solution);

		//Set up global variables
		btn=(Button)findViewById(R.id.buttonSolve);
		for (int i=0;i<10;i++){
			numberButtons[i]=(Button)findViewById(getResources().getIdentifier("button"+Integer.toString(i), "id", getPackageName()));
		}
		preparePaint();
		


		//Set up puzzle
		sg= (SudokuGrid) getIntent().getSerializableExtra("sudoku");
		if(sg==null){
			int[][] in=new int[9][9];
			for (int i=0;i<9;i++){
				for (int j=0;j<9;j++){
					in[i][j]=0;
				}
			}
			sg=new SudokuGrid(in);
		}
		orientationEventListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				//the following is orientation dependent:
				textSolved.setTextSize(getResources().getDimension(R.dimen.myFontSize));
				textInitial.setTextSize(getResources().getDimension(R.dimen.myFontSize));
				Rect bounds=new Rect();
				textSolved.getTextBounds("0", 0, 1, bounds);
				textVerticalAdjust=(bounds.bottom-bounds.top)/2;
				drawCanvas();
			}
		};
		view=((SurfaceView)findViewById(R.id.surfaceSudoku));
		view.setOnTouchListener(this);
		holder=view.getHolder();
		holder.addCallback(this);
	}

	private void preparePaint() {
		thick=new Paint();
		thick.setColor(Color.BLACK);
		thick.setStyle(Style.STROKE);
		thick.setStrokeWidth(5);
		thin=new Paint();
		thin.setColor(Color.BLACK);
		thin.setStyle(Style.STROKE);
		thin.setStrokeWidth(2);
		red=new Paint();
		red.setColor(Color.RED);
		red.setStyle(Style.STROKE);
		red.setStrokeWidth(5);
		background=new Paint();
		background.setColor(Color.BLACK);
		background.setStyle(Style.FILL);
		gridBackground=new Paint();
		gridBackground.setColor(Color.WHITE);
		gridBackground.setStyle(Style.FILL);
		textSolved=new Paint();
		textSolved.setTextAlign(Align.CENTER);
		textSolved.setColor(Color.GRAY);
		textSolved.setTextSize(getResources().getDimension(R.dimen.myFontSize));
		textInitial=new Paint();
		textInitial.setTextAlign(Align.CENTER);
		textInitial.setColor(Color.BLACK);
		textInitial.setTextSize(getResources().getDimension(R.dimen.myFontSize));
		textInitial.setFakeBoldText(true);
		Rect bounds=new Rect();
		textSolved.getTextBounds("0", 0, 1, bounds);
		textVerticalAdjust=(bounds.bottom-bounds.top)/2;
		
	}

	public void drawCanvas() {
		Canvas c=holder.lockCanvas();
		int[] dim= min(view.getWidth(),view.getHeight());
		size=dim[0];
		if (dim[1]==0){//height bigger
			topLeft[0]=0;
			topLeft[1]=(view.getHeight()-size)/2;
		}
		else{//width bigger
			topLeft[0]=(view.getWidth()-size)/2;
			topLeft[1]=0;
		}
		//Draw background and surround
		c.drawPaint(background);
		c.drawRect(topLeft[0], topLeft[1], topLeft[0]+size,topLeft[1]+ size,gridBackground);
		c.drawRect(topLeft[0], topLeft[1], topLeft[0]+size,topLeft[1]+ size,thick);
		//Draw vertical lines

		for (int i=1; i<9;i++){
			if (i%3!=0)
				c.drawLine(topLeft[0]+(size*i)/9, topLeft[1], topLeft[0]+(size*i)/9, topLeft[1]+size, thin);
			else
				c.drawLine(topLeft[0]+(size*i)/9, topLeft[1], topLeft[0]+(size*i)/9, topLeft[1]+size, thick);
		}
		//Draw horizontal lines
		for (int i=1; i<9;i++){
			if (i%3!=0)
				c.drawLine(topLeft[0], topLeft[1]+(size*i)/9, topLeft[0]+size, topLeft[1]+(size*i)/9, thin);
			else
				c.drawLine(topLeft[0], topLeft[1]+(size*i)/9, topLeft[0]+size, topLeft[1]+(size*i)/9, thick);
		}
		//Draw numbers
		for(int i=0;i<9;i++){
			for(int j=0;j<9;j++){
				if(sg.initialGrid[i][j]!=0){
					c.drawText(Integer.toString(sg.initialGrid[i][j]), topLeft[0]+(size*(2*j+1))/18, topLeft[1]+(size*(2*i+1))/18+textVerticalAdjust, textInitial);
					Log.e("drawInit",Integer.toString(i)+Integer.toString(j));}
				else if(solved){
					c.drawText(Integer.toString(sg.solutionGrid[i][j]), topLeft[0]+(size*(2*j+1))/18, topLeft[1]+(size*(2*i+1))/18+textVerticalAdjust, textSolved);
					Log.e("drawSolved",Integer.toString(i)+Integer.toString(j));}
			}
		}
		//Draw currently selected square
		if(!solved && selectedSquare[0]!=9){
			c.drawRect(topLeft[0]+(selectedSquare[0]*size)/9, topLeft[1]+(selectedSquare[1]*size)/9,
					topLeft[0]+((selectedSquare[0]+1)*size)/9, topLeft[1]+((selectedSquare[1]+1)*size)/9, red);
		}
		holder.unlockCanvasAndPost(c);
	}

	private int[] min(int a, int b) {
		int[] out ={0,0};
		out[0]=(a<b)?a:b;
		out[1]=(a<b)?0:1;
		return out;
	}

	public void solve(View view){
		btn.setEnabled(false);
		for (int i=0;i<10;i++)
			numberButtons[i].setEnabled(false);
		if(!solved){
			sg.solve(this);
		}
		else{
			orientationEventListener.disable();
			NavUtils.navigateUpFromSameTask(this);
			finish();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			orientationEventListener.disable();
			NavUtils.navigateUpFromSameTask(this);
			finish();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawCanvas();
		orientationEventListener.enable();
		btn.setEnabled(true);
		for (int i=0;i<10;i++)
			numberButtons[i].setEnabled(true);
		Toast.makeText(this, "Review input by selecting squares and using buttons, then click solve",Toast.LENGTH_LONG).show();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!solved){
			//find which grid square was selected
			int x=(int) event.getX();
			int y=(int) event.getY();
			int column=(x-topLeft[0])/(size/9);
			int row=(y-topLeft[1])/(size/9);
			//Check if this square is in the puzzle
			if(x>=topLeft[0] && column<9 && y>=topLeft[1] && row<9){
				selectedSquare[0]=column;
				selectedSquare[1]=row;
				for (int i=0;i<10;i++)
					numberButtons[i].setEnabled(true);
			}
			else{
				selectedSquare[0]=9;
				for (int i=0;i<10;i++)
					numberButtons[i].setEnabled(false);
			}
			drawCanvas();
		}
		return false;
	}
	public void numberSelected(View view){
		for(int i=0;i<10;i++){
			if (view.getId()==numberButtons[i].getId())
				//Switched 1 and 0 due to x,y not matches column, row not row,column!
				sg.initialGrid[selectedSquare[1]][selectedSquare[0]]=i;
		}
		drawCanvas();
	}

}


