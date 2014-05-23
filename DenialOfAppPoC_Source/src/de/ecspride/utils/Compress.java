package de.ecspride.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * used for compressing android application files into a package
 * @author Stephan Huber
 *
 */
public class Compress {
	 
	  private static final int BUFFER = 2048;
	  private static final String TAG = "Compress";
	  
	
	  private String noCompressExt[] = {
			  ".jpg", ".jpeg", ".png", ".gif",
			  ".wav", ".mp2", ".mp3", ".ogg", ".aac",
			  ".mpg", ".mpeg", ".mid", ".midi", ".smf", ".jet",
			  ".rtttl", ".imy", ".xmf", ".mp4", ".m4a",
			  ".m4v", ".3gp", ".3gpp", ".3g2", ".3gpp2",
			  ".amr", ".awb", ".wma", ".wmv", ".arsc"
	  };

	 
	  private String absolutePath;
	  private String zipFile;
	  private ArrayList<String> fileList = new ArrayList<String>();
	  private Activity activity;

	  /**
	   * Constructor for compressing a file
	   * @param absolutePath folder with source content
	   * @param zipFile output file with path
	   */
	  public Compress(Activity activity, String absolutePath, String zipFile) {
	    this.absolutePath = absolutePath;
	    this.zipFile = zipFile;
	    this.activity = activity;
	
	  }
	  
	  public Compress(String absolutPath, String zipFile) {
		  this.absolutePath = absolutPath;
		  this.zipFile = zipFile;
		  
	  }
	  
	  public Compress() {
		  
	  }
	  
	  public void zip() throws Exception {
		  this.zip(true);
	  }
	  

	
	  /**
	   * compress all files with subdirs and content in a zip file
	 * @throws CompressionException 
	   */
	  public void zip(boolean useSDcard) throws Exception {
		  //prepare blacklist for no compression files 
		  HashSet<String> extension = new HashSet<String>();
		   for (String s : noCompressExt) {
			   extension.add(s);
		   }

		  fileList = FileUtils.getDirList(absolutePath);
		try  {
	      BufferedInputStream origin = null;
	      FileOutputStream dest = null;
	      if (useSDcard) {
	    	  dest = new FileOutputStream(zipFile);
	      } else {
	    	  dest = activity.openFileOutput(zipFile, Context.MODE_WORLD_READABLE);
	      }
	      	      
	     ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	     out.setLevel(9);
	      
	      
	      byte data[] = new byte[BUFFER];
	      
	      CRC32 crc = new CRC32();
	    
	      for (String fileName : fileList) {
	        //inputstream needs whole absolute path 
	        FileInputStream fi = new FileInputStream(absolutePath + fileName);
	        origin = new BufferedInputStream(fi, BUFFER);
	        //zip contains only path of target folder
	        File file = new File(absolutePath + fileName);
	        ZipEntry entry = new ZipEntry(fileName);
	        if (isUncompressible(fileName, extension)) {

	        	entry.setMethod(ZipEntry.STORED);
	        	entry.setCompressedSize(file.length());
	        	entry.setSize(file.length());
	        	  	
	        	//crc32
	        	int bytesRead; 
	         	byte[] buffer = new byte[1024];
	        	BufferedInputStream bis = new BufferedInputStream(
	                     new FileInputStream(file), 8192);
	            crc.reset();
	            while ((bytesRead = bis.read(buffer)) != -1) {
	                  crc.update(buffer, 0, bytesRead);
	            } 
	        	
	        	entry.setCrc(crc.getValue());
	        	out.putNextEntry(entry);
	        } else {
	           out.putNextEntry(entry);
	        }
	        int count;
	        while ((count = origin.read(data, 0, BUFFER)) != -1) {
	          out.write(data, 0, count);
	        }
	        origin.close();
	      }

	      out.close();
	    } catch(Exception e) {
	    	Log.e(TAG, " Errror compressing a file " + e);
	    	throw new Exception();
	    }
	
	  }

	  /*
	   * checks if the file to compress has a blacklist ending and hasn't be compressed
	   */
	  private boolean isUncompressible(String fileName, HashSet<String> extensions) {
		  if (fileName.indexOf(".") > 0) {
			  String ext;
			  ext = fileName.substring(fileName.indexOf("."), fileName.length());
			  if (extensions.contains(ext)) {
				  return true;
			  } else {
				  return false;
			  }
			  
		  } else {
			  return false;
		  }
	  }
	

}