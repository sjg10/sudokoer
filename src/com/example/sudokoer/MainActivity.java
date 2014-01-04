package com.example.sudokoer;

import java.io.*;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MainActivity extends Activity {

	//This ensures that we retrieve things from OpenCV app.
	//TODO: make static (i.e. not from OpenCV app)!
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i("OpenCV", "OpenCV loaded successfully");
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};
	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// remove title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		installFiles();
	}

	private void installFiles() {
		//TODO: consider async:

		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("tessdata");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		//Check if we have done this before!
		File file = new File(getFilesDir().getAbsolutePath()+"/tessdata/"+files[0]);
		if(!file.exists())  {
			Log.e("Sudokoer", "Installing files");
			for(String filename : files) {
				//Start copying!
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open("tessdata/"+filename);
					File outFile = new File(getFilesDir().getAbsolutePath()+"/tessdata/", filename);
					outFile.getParentFile().mkdirs();
					out = new FileOutputStream(outFile);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch(IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}       
			}
		}
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
	public void startCamera(View view){
		//TODO: Allow start with or without camera
		Intent intent = new Intent(this, CameraActivity.class);
		startActivity(intent);
	}
}
