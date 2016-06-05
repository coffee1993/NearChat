package coffee1993.nearchat.crashManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

public class CrashHander implements UncaughtExceptionHandler {

	private static final String TAG = "CrashHandler";
	private static final boolean DEBUG = true;
	private static final String PATH= Environment.getExternalStorageDirectory().getPath()+"CrashTest/log/";
	
	private static final String FILE_NAME = "Crash";
	private static final String FILE_NAME_SUFFIX = ".trace";
	
	private static CrashHander sInstance = new CrashHander();
	private Context mContext;
	private UncaughtExceptionHandler mDefaultCrashHandler;
	
	public static CrashHander getInstance(){
		
		return sInstance;
	}
	
	public void init(Context context){
		mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context.getApplicationContext();
	}
	
	/**
	 * Thread 为出现未被捕获的异常的线程，ex为未被捕获异常
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		//导入未捕获异常到SD卡
		try {
			dumpExcepitonToSDCard(ex);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//将异常信息上传服务器，便于分析开发改Bug
		uploadExceptionToServer();
		ex.printStackTrace();

		//如果提供了默认的异常处理器，则交给系统自己结束程序，否则就自己结束程序。
		if(mDefaultCrashHandler!=null){
			mDefaultCrashHandler.uncaughtException(thread, ex);
		}else {
			Process.killProcess(Process.myPid());
		}
	}

	private void uploadExceptionToServer() {
		// TODO upload Excepiton Message to your webServerz
		
	}

	private void dumpExcepitonToSDCard(Throwable ex) throws IOException {
		//检测SD卡是否存在
		if(!Environment.getExternalStorageDirectory().equals(Environment.MEDIA_MOUNTED)){
			if(DEBUG){
				Log.i(TAG, "sdcard unmounted ,skip dump exception");
				return;
			}
		}
		File dir = new File(PATH);
		if(!dir.exists()){
			dir.mkdirs();
		}
		long current = System.currentTimeMillis();
		String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
		
		File file = new File(PATH+FILE_NAME+time+FILE_NAME_SUFFIX);
		try {
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			printWriter.println(time);
			//
			dumpPhoneInfo(printWriter);
			printWriter.println();
			ex.printStackTrace(printWriter);
			printWriter.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 打印系统信息
	 * @param printWriter
	 * @throws NameNotFoundException 
	 */
	private void dumpPhoneInfo(PrintWriter printWriter) throws NameNotFoundException {
		PackageManager pm  = mContext.getPackageManager();
		PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
		//App version :
		printWriter.print("App version: ");
		printWriter.println(pi.versionName+"_");
		printWriter.println(pi.versionCode);
		
		//Android 版本
		printWriter.print("OS Version: ");
		printWriter.print((Build.VERSION.RELEASE)+"_");
		printWriter.println(Build.VERSION.SDK_INT);
		
		//手机制造商
		printWriter.print("Vendor: ");
		printWriter.println(Build.MANUFACTURER);
		
		//手机型号：
		printWriter.print("Model：");
		printWriter.println(Build.MODEL);
	
		//CPU架构
		printWriter.print("CUP ABI: ");
		printWriter.println(Build.CPU_ABI);
	}

}
