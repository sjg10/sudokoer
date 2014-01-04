/**
 *  Sudokoer
 *  Component SudokuRecogniser
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

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageSegMode;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

public class SudokuRecogniser extends AsyncTask<byte[], Integer, SudokuGrid> {
	private CameraActivity cameraAct;
	private SudokuRecogniser updateTask;
	private ProgressDialog dialog;
	private int width;
	private int height;

	public SudokuRecogniser(CameraActivity cam){
		updateTask=this;
		cameraAct=cam;
		dialog=new ProgressDialog(cameraAct);
		dialog.setOnCancelListener(new ProgressDialog.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface di) {
				updateTask.cancel(true);
			}
		});
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setProgress(0);
		dialog.setMax(100);
		dialog.setMessage("Recognising");

	}

	@Override
	protected void onPreExecute() {

		cameraAct.endCamera();
		dialog.show();
	}

	@Override
	protected SudokuGrid doInBackground(byte[]... data) {
		//lets decode the image and put it in B&W
		Mat raw = new Mat(1, data[0].length, CvType.CV_8U); 
		raw.put(0, 0, data[0]);
		if (isCancelled()) return null;
		publishProgress(1);
		Mat M=Highgui.imdecode(raw, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		if (isCancelled()) return null;
		publishProgress(2);
		//lets make sure it's the right way round!
		switch(cameraAct.mLastRotation){
		case Surface.ROTATION_0: //portrait
			rotate_image_90n(M, M, 90);
			break;
		case Surface.ROTATION_90: //left_landscape
			break;
		case Surface.ROTATION_180: //upside_down
			break;
		case Surface.ROTATION_270://right_landscape
			rotate_image_90n(M, M, 180);
			break;
		}
		width=M.cols();
		height=M.rows();
		if (isCancelled()) return null;
		publishProgress(3);

		//Now lets invert colours and intensify the blacks:
		//TODO:Tweak values..the OCR is so close!
		Imgproc.adaptiveThreshold(M, M, 255, Imgproc.BORDER_CONSTANT, Imgproc.THRESH_BINARY_INV, 5, 2);
		if (isCancelled()) return null;
		publishProgress(4);
		//And find the connected components:
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat contourMat=new Mat();
		M.copyTo(contourMat);
		Imgproc.findContours(contourMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		if(contours.isEmpty()) return null;
		if (isCancelled()) return null;
		publishProgress(5);

		//and retrieve the largest one:
		double maxArea = -1;
		int maxAreaIdx = -1;
		for (int idx = 0; idx < contours.size(); idx++) {
			Mat contour = contours.get(idx);
			double contourarea = Imgproc.contourArea(contour);
			if (contourarea > maxArea) {
				maxArea = contourarea;
				maxAreaIdx = idx;
			}
		}
		MatOfPoint2f contour=new MatOfPoint2f(contours.get(maxAreaIdx).toArray());
		if (isCancelled()) return null;
		publishProgress(6);



		//Lets pretend the component is a quadrilateral:
		Imgproc.approxPolyDP(contour,contour, 5.0, true);
		if(contour.rows()!=4) return null;
		if (isCancelled()) return null;
		publishProgress(7);


		//Now we match the corners to that of a square
		Point[] contourPoints=contour.toArray();
		double[] gram=new double[4];
		int[] order = {0,-1,0,-1}; //(the order of the corners cw from top left)
		for(int i=0;i<4;i++){
			gram[i]=contourPoints[0].dot(contourPoints[i]);
		}
		double bottomrightDot=gram[0];
		double topleftDot=gram[0];

		for(int i=1;i<4;i++){
			if(gram[i]<topleftDot){
				topleftDot=gram[i];
				order[0]=i;
			}
			if(gram[i]>bottomrightDot){
				bottomrightDot=gram[i];
				order[2]=i;
			}}
		Log.e("order",Integer.toString(order[0])+Integer.toString(order[1])+Integer.toString(order[2])+Integer.toString(order[3]));
		for(int i=0;i<4;i++){
			if (order[0]!=i && order[2]!=i){
				for(int j=0;j<4;j++){
					if (order[0]!=j && order[2]!=j && i!=j){
						if (contourPoints[i].x<contourPoints[j].x){
							order[3]=i;
							order[1]=j;}
						else{
							order[3]=j;
							order[1]=i;
						}
					}
				}
				break;
			}}
		if (isCancelled()) return null;
		publishProgress(8);

		//Lets create an appropriate square:
		int size= width>height?width:height;
		Point[] squarePoints={new Point(0,0),new Point(size,0),new Point(size,size),new Point(0,size)};
		Point[] out=new Point[4];
		for(int i=0;i<squarePoints.length;i++)
			out[i]=squarePoints[order[i]];
		MatOfPoint2f square=new MatOfPoint2f(out);
		if (isCancelled()) return null;
		publishProgress(9);

		//And transform to a square:
		// compute transformation matrix
		Mat H = Imgproc.getPerspectiveTransform(contour, square);
		Imgproc.warpPerspective(M, M, H,new Size(size,size));
		if (isCancelled()) return null;
		publishProgress(10);

		//Now lets initialise the OCR
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.init(cameraAct.getFilesDir().getAbsolutePath(), "eng");
		if (isCancelled()) return null;
		publishProgress(11);
		
		baseApi.setVariable("tessedit_char_whitelist", "123456789");
		baseApi.setPageSegMode(PageSegMode.PSM_SINGLE_CHAR);
		String[][] grid=new String[9][9];
		int inc=size/9; 
		Bitmap bmp = Bitmap.createBitmap(inc-16, inc-16,Bitmap.Config.ARGB_8888);
		if (isCancelled()) return null;
    	publishProgress(12);
    	
    	//And begin:
		for (int i=0;i<9;i++){
			for(int j=0;j<9;j++){
				if (isCancelled()) return null;
				publishProgress(j+i*9+13);
				//TODO: consider removing the '8's that strip the border by something more dynamic
				Utils.matToBitmap(M.submat(i*inc+8,(i+1)*inc-8,j*inc+8,(j+1)*inc-8),bmp);
				baseApi.setImage(bmp);
				grid[i][j] = baseApi.getUTF8Text();
				baseApi.clear();
				//for debug
				/*canvas = cameraAct.surfaceHolder.lockCanvas();
						if(canvas != null){

							canvas.drawBitmap(bmp, 0, 0, null);
							cameraAct.surfaceHolder.unlockCanvasAndPost(canvas);

						}
				        Log.e("FOUND",grid[i][j]);
				        try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
			}
		}
		baseApi.end();
		if (isCancelled()) return null;
		publishProgress(94);
		
		//Now lets begin to solve:
		SudokuGrid puzzle = new SudokuGrid(grid);
		Log.e("puzzle",puzzle.initialGridString());
		if (isCancelled()) return null;
		publishProgress(100);
		return puzzle;
	}

	@Override
	protected void onPostExecute(SudokuGrid grid) {
		if (dialog.isShowing()) 
			dialog.dismiss();
		if(!isCancelled()){
		if(grid==null){
			AlertDialog ad = new AlertDialog.Builder(cameraAct).create();
			ad.setTitle("Computation Failed");
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("The puzzle was not detected.\nPlease ensure puzzle fills most of the screen and is not obscured and try again.\n");  
			ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
				@Override  
				public void onClick(DialogInterface dialog, int which) {
					cameraAct.initialiseCamera(false);//TODO:Doesnt work, to replace finish()
					//cameraAct.finish();

				}  
			});  
			ad.show();  

		}
		else{
			Intent intent = new Intent(cameraAct, SolutionActivity.class);
			intent.putExtra("sudoku", grid);
			cameraAct.startActivity(intent);
		}}
	}
	public void rotate_image_90n(Mat src, Mat dst, int angle){
		if(src!=dst)
			dst=src.clone();
		angle = ((angle / 90) % 4) * 90;

		//0 : flip vertical; 1 flip horizontal
		int flip_horizontal_or_vertical = angle > 0 ? 1 : 0;
		int number = angle / 90;          

		for(int i = 0; i != number; ++i){
			Core.transpose(dst, dst);
			Core.flip(dst, dst, flip_horizontal_or_vertical);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		dialog.setProgress(progress[0]);
	}
	@Override
	protected void onCancelled(){
		Toast.makeText(cameraAct, "Cancelled", Toast.LENGTH_SHORT).show();
		cameraAct.initialiseCamera(false);//TODO:Doesnt work, to replace finish()
		//cameraAct.finish();
	}

};