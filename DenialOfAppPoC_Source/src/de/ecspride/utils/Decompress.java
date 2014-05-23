package de.ecspride.utils;

import android.util.Log;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * @author Stephan Huber
 *
 */
public class Decompress {
	
	private static String TAG = "Decompress";
	/**
	 * only the classes.dex file is unziped
	 */
	public static final int MANIDEX = 0; //only dex file will unzipped
	/**
	 * all files without signature folder and AndroidManifest.xml are unziped
	 */
	public static final int RESTFILES = 1; //all files without signatur and Manifest + classes will unzipped
	/**
	 * only the signature folder is unziped
	 */
	public static final int SIGNATURE = 2; // only signature
	/**
	 * only signature folder and AndroidManifest.xml is unziped
	 */
	public static final int BACKUP = 3; //cert and manifest
	/**
	 * all files are unziped
	 */
	public static final int ALL = 4;
	
	private static Pattern METAPATTERN =
        Pattern.compile("^META-INF/(.*)[.](SF|RSA|DSA|MF)$");
	private static final String CLASS = "classes.dex";
	private static final String MANIFEST ="AndroidManifest.xml";
	
	private String zipFile;
	private String path;

	/**
	 * decompress a given zip file without META-INF folder
	 * @param zipFile filename with path of zip file
	 * @param path targetpath to extract the content of zip file
	 */
	public Decompress(String zipFile, String path) {
		this.path = path;
		this.zipFile = zipFile;
		
	}
	
	public Decompress() {
		
	}

	
	public void unzip(int c) {
	
		  ZipEntry fileName;
		  
			try {
				ZipFile zip = new ZipFile(zipFile);
				ZipEntry zeDex = zip.getEntry(CLASS);
				ZipEntry zeMani = zip.getEntry(MANIFEST);
				
				if ((c == 0) && (zeDex != null) && (zeMani != null)) {
						zipWriter(zip, zeDex);
						zipWriter(zip, zeMani);
				} else if ((c == 1) || (zeDex == null)) { //if no classes.dex the whole file is unzipped
				
					Enumeration<? extends ZipEntry> files = zip.entries();
					while (files.hasMoreElements()) {
						
						//Manifest files won't extracted
						fileName = files.nextElement();
						
						if (!METAPATTERN.matcher(fileName.getName()).matches()
							&& !(fileName.getName().equals(CLASS))
							&& !(fileName.getName().equals(MANIFEST))) {
							//System.out.println(fileName.getName());
							zipWriter(zip, fileName);
						}
					}
				} else if (c == 2) {
					Enumeration<? extends ZipEntry> files = zip.entries();
					while (files.hasMoreElements()) {
						
						//Manifest files won't extracted
						fileName = files.nextElement();
						if (METAPATTERN.matcher(fileName.getName()).matches()) {
							zipWriter(zip, fileName);
						}
					}
					
				} else if (c == 3) {
					Enumeration<? extends ZipEntry> files = zip.entries();
					System.out.println("BACKUP");
					zipWriter(zip, zeMani);
					while (files.hasMoreElements()) {
						
					
						fileName = files.nextElement();
						if (METAPATTERN.matcher(fileName.getName()).matches()) {
							zipWriter(zip, fileName);
						}
						
					}
				
				}else if (c == 4) {
				
					Enumeration<? extends ZipEntry> files = zip.entries();
					while (files.hasMoreElements()) {
						
						//Manifest files won't extracted
						zipWriter(zip, files.nextElement());
					}
				
				}
							
			
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Filenotfound", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
				e.printStackTrace();
			}
		
			
		
	}
	/*writes a zipentry from a zip file to the path directory*/
	private void zipWriter(ZipFile zf, ZipEntry ze) throws IOException {
		
			String fileWithPath;
			String outputPath;
					
			InputStream is = zf.getInputStream(ze);
			fileWithPath = path + ze.getName(); //filename with whole path
			outputPath =  fileWithPath.substring(0, fileWithPath.lastIndexOf("/") + 1);
			
			File dir = new File(outputPath);
			dir.mkdirs();
			FileOutputStream fout = new FileOutputStream(fileWithPath);
			byte[] buffer = new byte[1024];
			int length;
			
			while ((length = is.read(buffer)) > 0) {
				fout.write(buffer, 0, length);
			}
			
			is.close();
			fout.close();	
	}
}