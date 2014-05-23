package de.ecspride.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import android.content.Context;
import android.content.res.AssetManager;
import de.ecspride.Settings;

/**
 * 
 * @author Stephan Huber and Siegfried Rasthofer
 *
 */
public class FileUtils {
	
	private static ArrayList<String> fileList = new ArrayList<String>();
	private static int absPathLength;
	/**
	 * list directory and subdirectory content of a folder
	 * @param absolutePath	to the folder to list
	 * @return arrayList of files with folder and subfolders
	 */
	public static ArrayList<String> getDirList(String absolutePath) {
		fileList.clear(); 
		absPathLength = absolutePath.length();
		 File f = new File(absolutePath);
		 listDir(f);
		 return fileList;
	}
		
	/*creates recursivly file list with all subdirs from a given folder without absolut path folder */
	private static void listDir(File dir) {
		
			File[] files = dir.listFiles();
		
			
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
		
					listDir(files[i]);
					
				} else {
					fileList.add(files[i].getPath().substring(absPathLength));
				}
			}
	  }
	
	public static void copyBrokenApkFromAssetsToInternalStorage(Context context, String targetFolder) throws IOException{		
		copyAssetFolder(context.getAssets(), Settings.brokenApkFolderName, targetFolder);
	}
	

	public static void addManipulatedManifestToInternalStorage(Context context, final String packageName, String targetFolder) throws IOException{
		FileOutputStream out = new FileOutputStream(targetFolder + File.separator + "AndroidManifest.xml");
    	InputStream in = context.getApplicationContext().getAssets().open(Settings.manifestFilePathInAssets);
    	try{
	    	manipulateAndroidManifest(in, out, packageName);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
                
    	in.close();
        out.close();
	}
	
	/**
	 * This method is responsible for changing the package name of the AndroidManifest.xml (binary)
	 * @param is: AndroidManifest.xml in binary format
	 * @param fos: target file
	 * @param packageName: package name to instrument
	 * @throws Exception
	 */
	public static void manipulateAndroidManifest(InputStream is, FileOutputStream fos, final String packageName) throws Exception {
		byte[] xml = new byte[is.available()];
		is.read(xml);
		is.close();
		
		AxmlReader reader = new AxmlReader(xml);
		AxmlWriter writer = new AxmlWriter();
		
		reader.accept(new AxmlVisitor(writer) {
			public NodeVisitor first(String ns, String name) {
				if (name.equalsIgnoreCase("manifest")) {
					return new NodeVisitor(super.first(ns, name)) {
						public void attr(String ns, String name, int resourceId, int type, Object obj) {
							if (name.equalsIgnoreCase("package")) {
								super.attr(ns, name, resourceId, TYPE_STRING, packageName);
							} else {
								super.attr(ns, name, resourceId, type, obj);
							}
							
							
						}
						public void end() {
							super.end();
						}
					};
				}
				return super.first(ns, name);
			} 
		 
		});
		byte[] modified = writer.toByteArray();
		fos.write(modified);
	}
	
    
    private static boolean copyAssetFolder(AssetManager assetManager,
            String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager, 
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else 
                    res &= copyAssetFolder(assetManager, 
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAsset(AssetManager assetManager,
            String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
          in = assetManager.open(fromAssetPath);
          new File(toPath).createNewFile();
          out = new FileOutputStream(toPath);
          copyFile(in, out);
          in.close();
          in = null;
          out.flush();
          out.close();
          out = null;
          return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
    
    public static void purgeDirectory(File dir) {
        for (File file: dir.listFiles()) {
            if (file.isDirectory()) purgeDirectory(file);
            file.delete();
        }
    }
    
}
