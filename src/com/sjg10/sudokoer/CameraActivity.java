/**
 *  Sudokoer
 *  Component CameraActivity
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

import java.io.IOException;

import com.sjg10.sudokoer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;

public class CameraActivity extends Activity implements Camera.PictureCallback,SurfaceHolder.Callback{
	public Camera camera;
	public int mLastRotation=-1;
	public OrientationEventListener orientationEventListener;
	public Button buttonTake;
	public SurfaceView surfaceView;
	public SurfaceHolder surfaceHolder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_camera);
		buttonTake=(Button)findViewById(R.id.buttonTake);

		orientationEventListener = new OrientationEventListener(this,
		        SensorManager.SENSOR_DELAY_NORMAL) {
		    @Override
		    public void onOrientationChanged(int orientation) {
			    if(camera!=null) reorientCamera();
		}

		};
		initialiseCamera(true);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		
		//prepare AsyncTask to run:
		SudokuRecogniser sc =new SudokuRecogniser(this);
		sc.execute(data);
}
	
	public void takePhoto(View view){
			camera.takePicture(null, null, this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e("Err",Log.getStackTraceString(e));
		    AlertDialog ad = new AlertDialog.Builder(this).create();
		    ad.setTitle("Camera Error");
		    ad.setCancelable(false); // This blocks the 'BACK' button  
		    ad.setMessage("Another app may be using the camera.\nExit app and try again.\n");  
		    ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
		        @Override  
		        public void onClick(DialogInterface dialog, int which) {
		        	finish();
		        }  
		    });  
		    ad.show();  
		}
		camera.startPreview();
		reorientCamera();
		buttonTake.setEnabled(true);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void initialiseCamera(boolean firstTime) {
		if (orientationEventListener.canDetectOrientation())
		    orientationEventListener.enable();
		if (camera==null){
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
	    parameters.setRotation(0);
	    parameters.setZoom(0);
	    parameters.setPictureFormat(PixelFormat.RGB_565);
	    parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);//TODO: Change to macro?
	    camera.setParameters(parameters);
	    surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
	    surfaceHolder=surfaceView.getHolder();
	    surfaceHolder.addCallback(this);
	    if(!firstTime){
	    	try{
				camera.setPreviewDisplay(surfaceHolder);}
	    	catch(IOException e){
	    	}
	    	camera.startPreview();
			reorientCamera();//TODO: doesn't actually work at this point!
			buttonTake.setEnabled(true);
	    }
	    }
		
	}
	
	public void endCamera(){
		if (camera!=null){
			camera.release();
			camera=null;
			surfaceHolder.removeCallback(this);
			orientationEventListener.disable();
		}
	}
	
	public void reorientCamera() {
	    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	    	int rot=display.getRotation();
	    	if(rot!= mLastRotation){
	    	switch(rot){
	    	case Surface.ROTATION_0: //portrait
	    		camera.setDisplayOrientation(90);
	    		break;
	    	case Surface.ROTATION_90: //left_landscape
	    		camera.setDisplayOrientation(0);
	    		break;
	    	case Surface.ROTATION_180: //upside_down
	    		camera.setDisplayOrientation(270);
	    		break;
	    	case Surface.ROTATION_270://right_landscape
	    		camera.setDisplayOrientation(180);
	    		break;
	    	}
	    	mLastRotation=rot;
		
	}}
	@Override
	public void onDestroy(){
		endCamera();
		super.onDestroy();
	}
}
