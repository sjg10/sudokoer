/**
 *  Sudokoer
 *  Component MainActivity
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

import java.io.*;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.sjg10.sudokoer.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;


public class MainActivity extends Activity {
	private boolean autoInput=false;
	public AssetManager assetManager;
	//This ensures that we retrieve things from OpenCV app.
	//TODO: Consider making static (i.e. not from OpenCV app)!
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
		assetManager = getAssets();
		installFiles();

	}

	private void installFiles() {
		String[] files = null;
		try {
			files = assetManager.list("tessdata");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		//Check if we have done this before!
		File file = new File(getFilesDir().getAbsolutePath()+"/tessdata/"+files[files.length-1]);
		if(!file.exists())  {
			Log.i("Sudokoer", "Installing files");
			FileInstaller fi = new FileInstaller(this);
			fi.execute(files);
		}
	}

	public void onRbClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		// Check which radio button was clicked
		if (checked){
			switch(view.getId()) {
			case R.id.rbAuto:
				autoInput=true;
				break;
			case R.id.rbManual:
				autoInput=false;
				break;
			}
		}
	}
	public void startSudoku(View view){
		Intent intent;
		if (autoInput)
			intent = new Intent(this, CameraActivity.class);
		else
			intent = new Intent(this, SolutionActivity.class);
		startActivity(intent);
	}
}
