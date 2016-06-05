package coffee1993.nearchat.net.udp;


import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import coffee1993.nearchat.message.MSGProtocol;
import coffee1993.nearchat.util.NearChatLog;

/**
 * processMessage 由 EventHub处理
 * 
 * 
 */

/**
 * 
 * 关于UDP 线程的管理类 
 * @author zhangyi
 *
 */
public class UDPManager {
	private static String TAG = "UDPMangerCopy";
	private static UDPManager instance;
	private static UDPSenderThread senderThread;
	private static UDPReceiveThread receiveThread;
	private static ExecutorService executor;
	private static final int POOL_SIZE = 5; // 单个CPU线程池大小
	private boolean isThreadPool = false; //线程池有问题
	private HashMap<String, String> mLastMsgCache; // 最后一条消息缓存，以IMEI为KEY
	private UDPManager() {
		NearChatLog.i("UDPManager UDPManager() --> UDPManger 初始化");
		receiveThread = new UDPReceiveThread();
		mLastMsgCache = new HashMap<String,String>();
		int cpuNums = Runtime.getRuntime().availableProcessors();
		NearChatLog.i("cpu Num:" +cpuNums);
		executor = Executors.newFixedThreadPool(cpuNums * POOL_SIZE); // 根据CPU数目初始化线程池
	}
	
	public static UDPManager getInstance() {
		
		if(instance == null){
			synchronized (UDPManager.class) {
				if(instance == null){
					NearChatLog.i("UDPManager getInstance() --> 第一次初始化UDPManager");
					instance = new UDPManager();
				}		
			}
		}		
		return instance;
	}	
	//
	/**
	 * @param msgProtocol
	 * @param targetIp
	 */
	public  void sendUDPdata(MSGProtocol msgProtocol,String targetIp){
		
		if(isThreadPool){
			NearChatLog.i("UDPManager sendUDPdate() --> 线程池开启");
			executor.execute(new UDPSenderThread(targetIp,msgProtocol));
		}
		else{
			//方案2 线程池调用  
			NearChatLog.i("UDPManager sendUDPdate() --> 线程池未开启");
			senderThread = new UDPSenderThread(targetIp,msgProtocol);
			senderThread.startSendThread();
		}
	}

	
	public void startReceved(){
		if(receiveThread!=null){			
			receiveThread.startReceive();
		}
	}

	public void stopReceved(){
		if(receiveThread!=null){			
			receiveThread.stopReceive();
		}
	}

	public void putLastMsgCache(String paramIMEI, String content) {
		mLastMsgCache.put(paramIMEI, content);
	}

	
	
	

	
	
	
	
}
