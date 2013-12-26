package com.example.sudokoer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

public class SudokuComputer extends AsyncTask<byte[], Integer, String> {
	private CameraActivity cameraAct;
	private SudokuComputer updateTask;
	private ProgressDialog dialog;
	private int width;
	private int height;

	public SudokuComputer(CameraActivity cam){
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
		dialog.setMax(2);
		dialog.setMessage("Calculating");

	}

	@Override
	protected void onPreExecute() {
		cameraAct.orientationEventListener.disable();
		cameraAct.buttonTake.setEnabled(false);
		cameraAct.camera.release();

		dialog.show();
	}

	@Override
	protected String doInBackground(byte[]... data) {
		//lets decode the image and put it in B&W
		Mat raw = new Mat(1, data[0].length, CvType.CV_8U); 
		raw.put(0, 0, data[0]);
		Mat M=Highgui.imdecode(raw, Highgui.CV_LOAD_IMAGE_GRAYSCALE);

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

		//Now lets invert colours and intensify the blacks:
		//You can always tweak the values a little more:
		Imgproc.adaptiveThreshold(M, M, 255, Imgproc.BORDER_CONSTANT, Imgproc.THRESH_BINARY_INV, 9, 5);

		//And find the largest connected component:
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(M, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		if(contours.isEmpty()) return "fail";
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

		//Lets pretend the component is a quadrilateral:
		//TODO: Fiddle the epsilon so 4 corners come up more often
		Imgproc.approxPolyDP(contour,contour, 5.0, true);
		if(contour.rows()!=4) return "fail";

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
		Log.e("order",Integer.toString(order[0])+Integer.toString(order[1])+Integer.toString(order[2])+Integer.toString(order[3]));
		Log.e("points",contourPoints[0].toString()+"\n"+contourPoints[1].toString()+"\n"+contourPoints[2].toString()+"\n"+contourPoints[3].toString());
		int size= width>height?width:height;
		Point[] squarePoints={new Point(0,0),new Point(size,0),new Point(size,size),new Point(0,size)};
		Point[] out=new Point[4];
		for(int i=0;i<squarePoints.length;i++)
			out[i]=squarePoints[order[i]];
		MatOfPoint2f square=new MatOfPoint2f(out);

		if (isCancelled()) return null;

		//And transform to a square:
		// compute transformation matrix
		Mat H = Imgproc.getPerspectiveTransform(contour, square);
		Imgproc.warpPerspective(M, M, H,new Size(size,size));
		//TODO: OCR and finish!
		Bitmap bmp = Bitmap.createBitmap(size,  size,Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(M, bmp);

		//Artificial slowdown:
		for(int i=0;i<2;i++){
			try {
				if(isCancelled()) return null;
				publishProgress (i);
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Canvas  canvas = cameraAct.surfaceHolder.lockCanvas();
		if(canvas != null){

			canvas.drawBitmap(bmp, 0, 0, null); 
		}
		cameraAct.surfaceHolder.unlockCanvasAndPost(canvas);


		return null;
	}

	@Override
	protected void onPostExecute(String string) {
		if (dialog.isShowing()) 
			dialog.dismiss();
		if(string!=null){
			AlertDialog ad = new AlertDialog.Builder(cameraAct).create();
			ad.setTitle("Computation Failed");
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("The puzzle was not detected.\nPlease ensure puzzle fills most of the screen and is not obscured and try again.\n");  
			ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					cameraAct.orientationEventListener.enable();
					cameraAct.initialiseCamera(false);//TODO:Doesnt orient right first shot!

				}  
			});  
			ad.show();  

		}
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
		cameraAct.orientationEventListener.enable();
		cameraAct.initialiseCamera(false);//TODO:Doesnt orient right first shot!
		Toast.makeText(cameraAct, "Cancelled", Toast.LENGTH_SHORT).show();
	}

};