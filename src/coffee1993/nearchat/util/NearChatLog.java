package coffee1993.nearchat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import android.os.Environment;
import android.util.Log;

/**
 * @fileName LogUtils.java
 * @description 日志操作类
 * @author zhangyi
 */

public class NearChatLog {

	protected static final String TAG = "nearChatLog";
	protected static String LOG_NAME = "NearChatLog.txt";
	protected static String LOG_DIR_NAME = "NearChatLog";
	
	
	protected static final String TAG_ARROW = ">>>>>>>>>>>>>>>>>>>>>>>";

	private static final boolean IS_WRITE_TXT = true;

	private static final boolean IS_DEBUG = true;

	public static final String TIMEFORMAT = "yyyy-mm-dd HH:mm:ss";

	public static final int VERBOSS = 5;

	public static final int DEBUG = 4;

	public static final int INFO = 3;

	public static final int WARN = 2;

	public static final int ERROR = 1;
	
	private static Boolean logEncryBoolean = true;

	public static EncryptionDecryption encryptionDecryption = null;

	public EncryptionDecryption getEncryptionDecryption() throws Exception {
		if (encryptionDecryption == null) {
			return EncryptionDecryption.getInstance();
		}
		return encryptionDecryption;
	}

	public void setEncryptionDecryption(
			EncryptionDecryption encryptionDecryption) {
		this.encryptionDecryption = encryptionDecryption;
	}

	public static void log(String tag, Throwable throwable, int type) {
		log(tag, thToString(throwable), type);
	}

	public static void log(String tag, String msg, int type) {
		if (msg == null) {
			msg = "msg null";
		}
		switch (type) {
		case VERBOSS:
			v(tag, msg);
			break;
		case DEBUG:
			d(tag, msg);
			break;
		case INFO:
			i(tag, msg);
			break;
		case WARN:
			w(tag, msg);
			break;
		case ERROR:
			e(tag, msg);
			break;
		default:
			break;
		}
	}

	/**
	 * 默认 TAG = NearChatConfig.TAG_STRING
	 * 
	 * @param msg
	 */
	public static void i(String msg) {

		i(NearChatConfig.TAG_STRING, msg);

	}

	public static void e(String msg) {

		e(NearChatConfig.TAG_STRING, msg);

	}

	public static void v(String tag, String msg) {
		if (IS_DEBUG) {
			Log.v(tag, msg);
		}

		if (IS_WRITE_TXT) {
			writeLogtoFile(tag, "v"+msg);
		}

	}

	public static void d(String tag, String msg) {
		if (IS_DEBUG) {
			Log.d(tag, msg);
		}
		if (IS_WRITE_TXT) {
			writeLogtoFile(tag, "d:" + msg);
		}
	}

	public static void w(String tag, String msg) {
		if (IS_DEBUG) {
			Log.w(tag, msg);
		}
		if (IS_WRITE_TXT) {
			writeLogtoFile(tag, "w:" + msg);
		}
	}

	public static void i(String tag, String msg) {
		if (IS_DEBUG) {
			Log.i(tag, msg);
		}
		if (IS_WRITE_TXT) {
			writeLogtoFile(tag, "i:" + msg);
		}

	}

	public static void e(String tag, String msg) {
		if (IS_DEBUG) {
			Log.e(tag, msg);
		}
		if (IS_WRITE_TXT) {
			writeLogtoFile(tag, "e:" + msg);
		}

	}

	public static String getSDPath() {
	  File sdDir = null;
       boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }
        return null;
	}

	private static String thToString(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		printWriter.flush();
		String resultString = writer.toString();
		return resultString;
	}

	private static void writeLogtoFile(String tag, String logMessage) {// 新建或打开日志文件
		String logtime = DateUtil.getFormatDateTimeString(new Date(),
				DateUtil.hhmmFormat);
		String logString = logtime + "->" + logMessage;
		String path = getSDPath();
		//Sdcard/NearChatLog.txt
		File logFile = FileUtils.createNewFile(path+File.separator+LOG_NAME);
		if(logEncryBoolean){
			try {
				logString = encryptionDecryption.getInstance().encrypt(logString);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		try {
			FileWriter fileWriter = new FileWriter(logString, true);// 是否 opened;
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(logString);
			bufferedWriter.newLine();
			//bufferedWriter.flush();
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void deleteFile(){
	       File file = new File(getSDPath()+File.separator+LOG_NAME);  
	        if (file.exists()) {  
	            file.delete();  
	        }  
	}


}
