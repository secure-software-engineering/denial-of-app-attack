package de.ecspride.utils;

	import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * 
 * @author Stephan Huber
 *
 */
	public class ZipSignature {

	    
	    //sha-160 DER encoding SHA-1:   (0x)30 21 30 09 06 05 2b 0e 03 02 1a 05 00 04 14 || H
	    private byte[] sha1Prefix = {0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00 , 0x04, 0x14};
	       
	    private Cipher cipher;
	    private MessageDigest md;

	    /**
	     * defines cipher for zipsigning
	     * @throws IOException
	     * @throws GeneralSecurityException
	     */
		public ZipSignature() throws IOException, GeneralSecurityException	{
			md = MessageDigest.getInstance("SHA1");
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		}
		
		/**
		 * encryption operation mode
		 * @param privateKey private key as pk8 file
		 * @throws InvalidKeyException
		 */
		public void initSign(PrivateKey privateKey) throws InvalidKeyException  {
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		}
		
		/**
		 * update messagedigest
		 * @param data 
		 */
		public void update(byte[] data) {
			md.update(data);
		}
		
		/**
		 * sign 
		 * @return
		 * @throws BadPaddingException
		 * @throws IllegalBlockSizeException
		 */
		public byte[] sign() throws BadPaddingException, IllegalBlockSizeException	{
			cipher.update(sha1Prefix);
	        cipher.update(md.digest());		
			return cipher.doFinal();
		}
	
}
