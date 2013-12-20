package com.example.sudokoer;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
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
import android.widget.Toast;
import android.os.Process;

public class CameraActivity extends Activity implements Camera.PictureCallback,SurfaceHolder.Callback{
	public Camera camera;
	public int mLastRotation=-1;
	public OrientationEventListener orientationEventListener; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_camera);
		
		orientationEventListener = new OrientationEventListener(this,
		        SensorManager.SENSOR_DELAY_NORMAL) {
		    @Override
		    public void onOrientationChanged(int orientation) {
			    if (camera!=null){
			    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			    	int rot=display.getRotation();
			    	if(rot!= mLastRotation){
			    	switch(rot){
			    	case Surface.ROTATION_0: 
			    		camera.setDisplayOrientation(90);
			    		break;
			    	case Surface.ROTATION_90: 
			    		camera.setDisplayOrientation(0);
			    		break;
			    	case Surface.ROTATION_180: 
			    		camera.setDisplayOrientation(270);
			    		break;
			    	case Surface.ROTATION_270:
			    		camera.setDisplayOrientation(180);
			    		break;
			    	}
			    	mLastRotation=rot;}
			    	
			    }
		}};

		if (orientationEventListener.canDetectOrientation()) {
		    orientationEventListener.enable();
		}
		camera = Camera.open();
		camera.autoFocus(null);
		Camera.Parameters parameters = camera.getParameters();
	    parameters.setRotation(0);
	    parameters.setZoom(0);
	    camera.setParameters(parameters);
	    //this.onConfigurationChanged(this.getResources().getConfiguration());
	    orientationEventListener.onOrientationChanged(0);
		SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
		SurfaceHolder surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Toast.makeText(this, "Picture taken", Toast.LENGTH_LONG).show();
		//TODO: begin work!
	}
	
	public void takePhoto(View view){
		camera.takePicture(null, this, null);
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
		Button btn = (Button)findViewById(R.id.buttonTake);
		btn.setEnabled(true);
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
}
