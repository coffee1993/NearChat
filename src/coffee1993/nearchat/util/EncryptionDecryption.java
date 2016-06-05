package coffee1993.nearchat.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.util.Log;

/**
 * 自定义加密解密类
 * DES加密和解密过程中，密钥长度都必须是8的倍数
 * @author zy
 *
 */
public class EncryptionDecryption {
	
	private Cipher encryptCipher = null;
	private Cipher decryptCipher = null;
	private static EncryptionDecryption instance;
	private String keyDefaultStr = "NC+-2015"; //8位

	private static final String DES_ECB = "DES/ECB/PKCS5Padding";

	
	private EncryptionDecryption() throws Exception {
		Key key = getKey(keyDefaultStr.getBytes("UTF-8"));
		
		encryptCipher = Cipher.getInstance(DES_ECB);
		encryptCipher.init(Cipher.ENCRYPT_MODE,key);
		
		decryptCipher = Cipher.getInstance(DES_ECB);
		decryptCipher.init(Cipher.ENCRYPT_MODE,key);
		
	}

	public static EncryptionDecryption getInstance() throws Exception {
		if(instance==null){
			synchronized (EncryptionDecryption.class) {
				if(instance==null){
					instance = new EncryptionDecryption();
				}
			}
		}
		return instance;
		
	}
	
	public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
		return encryptCipher.doFinal(data);
	}
	
	public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
		return decryptCipher.doFinal(data);
	}
	public Key getKey(byte [] keyStr){
		byte[] key = new byte [8];//初始化为0
		//确保8位
		for (int i = 0; i < key.length && i<keyStr.length; i++) {
			key[i]=keyStr[i];
		}
		//生成密钥：
		Key key1 = new javax.crypto.spec.SecretKeySpec(key,"DES");
		//检测一下长度：
		int temp = key1.getEncoded().length;
		Log.d("Encryption", temp+"位长度");
		
		return key1;
	}
	
	 /** 
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[] 
     * hexStr2ByteArr(String strIn) 互为可逆的转换过程 
     *  
     * @param arrB 
     *            需要转换的byte数组 
     * @return 转换后的字符串 
     * @throws Exception 
     *   
     */  
    public static String byteArr2HexStr(byte[] arrB) throws Exception {  
        int iLen = arrB.length;  
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍  
        StringBuffer sb = new StringBuffer(iLen * 2);  
        for (int i = 0; i < iLen; i++) {  
            int intTmp = arrB[i];  
            // 把负数转换为正数  
            while (intTmp < 0) {  
                intTmp = intTmp + 256;  
            }  
            // 小于0F的数需要在前面补0  
            if (intTmp < 16) {  
                sb.append("0");  
            }  
            sb.append(Integer.toString(intTmp, 16));  
        }  
        return sb.toString();  
    }  
  
    /** 
     * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB) 
     * 互为可逆的转换过程 
     *  
     * @param strIn 需要转换的字符串 
     * @return 转换后的byte数组 
     * @throws Exception 
     * 
     */  
    public static byte[] hexStr2ByteArr(String strIn) throws Exception {  
        byte[] arrB = strIn.getBytes();  
        int iLen = arrB.length;  
  
        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2  
        byte[] arrOut = new byte[iLen / 2];  
        for (int i = 0; i < iLen; i = i + 2) {  
            String strTmp = new String(arrB, i, 2);  
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);  
        }  
        return arrOut;  
    }  
    
    
    /** 
     * 加密字符串 
     *  
     * @param strIn 
     *            需加密的字符串 
     * @return 加密后的字符串 
     * @throws Exception 
     */  
    public String encrypt(String strIn) throws Exception {  
        return byteArr2HexStr(encrypt(strIn.getBytes()));  
    }
  
	
}
