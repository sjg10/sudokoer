/**
 *  Sudokoer
 *  Component FileInstaller
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class FileInstaller extends AsyncTask<String[], Integer, String> {
	private MainActivity parent;
	private ProgressDialog dialog;
	
	public FileInstaller(MainActivity activity){
		parent=activity;
		dialog=new ProgressDialog(parent);
		dialog.setCancelable(false);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("Installing files...");
	}
	
	@Override
	protected void onPreExecute() {
		dialog.show();
	}
	
	@Override
	protected String doInBackground(String[]... files) {
		for(String filename : files[0]) {
			//Start copying!
			InputStream in = null;
			OutputStream out = null;
			try {
				in = parent.assetManager.open("tessdata/"+filename);
				File outFile = new File(parent.getFilesDir().getAbsolutePath()+"/tessdata/", filename);
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
				return null;
			}       
		}
		return "done";
	}
	@Override
	protected void onPostExecute(String out) {
		dialog.dismiss();
		if(out==null){
			AlertDialog ad = new AlertDialog.Builder(parent).create();
		    ad.setTitle("Installation Error");
		    ad.setCancelable(false); // This blocks the 'BACK' button  
		    ad.setMessage("File installation failed. Consider reinstalling the app.\n");  
		    ad.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		        	parent.finish();
		        }  
		    });  
		    ad.show(); 
		}
		
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
}
