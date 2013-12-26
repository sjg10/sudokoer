package com.example.sudokoer;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.os.Process;

public class CameraActivity extends Activity implements Camera.PictureCallback,SurfaceHolder.Callback{
	public Camera camera;
	public int mLastRotation=-1;
	public OrientationEventListener orientationEventListener;
	public Button buttonTake;
	public SurfaceView surfaceView;
	public SurfaceHolder surfaceHolder;
	//This ensures that we retrieve things from OpenCV app.
	//Consider making static!



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

		if (orientationEventListener.canDetectOrientation()) {
		    orientationEventListener.enable();
		}
		initialiseCamera(true);
		
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

		
		//prepare AsyncTask to run:
		SudokuComputer sc =new SudokuComputer(this);
		sc.execute(data);
}
	
	public void takePhoto(View view){
		camera.takePicture(null, null, this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
		    AlertDialog ad = new AlertDialog.Builder(this).create();
		    ad.setTitle("Camera Error");
		    ad.setCancelable(false); // This blocks the 'BACK' button  
		    ad.setMessage("Another app may be using the camera.\nExit app and try again.\n");  
		    ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
		        @Override  
		        public void onClick(DialogInterface dialog, int which) {  
		        	Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL);          
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
		orientationEventListener.disable();
		camera.release();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	orientationEventListener.disable();
	        camera.release();
	        finish();
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

	public void initialiseCamera(boolean firstTime) {
		camera = Camera.open();
		camera.autoFocus(null);
		Camera.Parameters parameters = camera.getParameters();
	    parameters.setRotation(0);
	    parameters.setZoom(0);
	    parameters.setPictureFormat(PixelFormat.RGB_565);
	    parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);//TODO: Change to macro?
	    camera.setParameters(parameters);
	    if(firstTime){
	    	surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
	    	surfaceHolder=surfaceView.getHolder();
	    	surfaceHolder.addCallback(this);}
	    else{
	    	//still doesn't match here!
	    	surfaceCreated(surfaceHolder);
	    }
		
	}
	
	public void reorientCamera() {
	    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	    	int rot=display.getRotation();
	    	if(rot!= mLastRotation){
	    	switch(rot){
	    	case Surface.ROTATION_0: //portait
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

}
