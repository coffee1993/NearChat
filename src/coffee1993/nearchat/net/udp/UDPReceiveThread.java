package coffee1993.nearchat.net.udp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import coffee1993.nearchat.message.EventHub;
import coffee1993.nearchat.message.MSGProtocol;
import coffee1993.nearchat.message.MSGProtocolConst;
import coffee1993.nearchat.session.SessionManager;
import coffee1993.nearchat.util.NearChatLog;

public class UDPReceiveThread implements Runnable {
	private Boolean isThreadRunning;
	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;
	
	private static final int BUFFERLENGTH = 1024; //缓冲大小1M 1024字节
    private byte[] receiveBuffer = new byte[BUFFERLENGTH];
    private Thread receiveThread;
	
    public UDPReceiveThread() {
    	NearChatLog.i("UDPReceiveThread UDPReceiveThread() --> 初始化");
    }
    
	@Override
	public void run() {
		while(isThreadRunning){
			NearChatLog.i("UDPRevceveThread run() --> UDP循环监听端口 ："+datagramSocket.getLocalPort());
			try {
				if(datagramSocket==null){
					NearChatLog.i("UDPReceiveThread() --> Socket null");
					return ;
				}
				datagramSocket.receive(datagramPacket);
				
				//接受到数据 果然localPort就是接收到数据的端口
				NearChatLog.i("UDPRevceveThread run() --> 接收到数据成功 localPort ："+datagramSocket.getLocalPort());		
				
			} catch (IOException e) {
				isThreadRunning = false;
				datagramPacket = null;
				if(datagramSocket!=null){
					datagramSocket.close();
					datagramSocket=null;
				}
				receiveThread = null;
				NearChatLog.e("UDPRevceveThread run() --> 接受数据包失败IO异常 线程停止");
				e.printStackTrace();
				break;
			}
			if(datagramPacket.getLength()==0){
				NearChatLog.e("UDPRevceveThread run() --> 接受数据包为空");		
				continue;
			}
			
			//datagramSocket适合接受字符串  所以可用json字符串来传递对象 //指定什么编码好呢?
			String receiveData = "";
			try {
				receiveData	= new String(receiveBuffer, 0, datagramPacket.getLength(),"utf-8");
				//解析数据 消息回调
				//NearChatLog.i("UDPRevceveThread run() --> 接收到的数据String:"+receiveData);	
				
				
			} catch (UnsupportedEncodingException e) {
				NearChatLog.i("UDPRevceveThread run() --> 系统编码utf-8问题");
				e.printStackTrace();
			}
			  // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。

			//FastJson不支持多态 所以需要手动解析
			MSGProtocol msgProtocol = new MSGProtocol(receiveData);			
			//消息处理中枢处理消息 
			if(msgProtocol!=null){
				//排除收到自身的广播
				NearChatLog.i("UDPReceiveThread run() --> 发送者IMEI :"+ msgProtocol.getSenderIMEI());
				NearChatLog.i("UDPReceiveThread run() --> 本机 IMEI :"+ SessionManager.getIMEI());
				String senderIp =  datagramPacket.getAddress().getHostAddress();
				if(!msgProtocol.getSenderIMEI().equals(SessionManager.getIMEI())){					
					NearChatLog.i("UDPReceiveThread run() --> EventHub processMessage ");					
					EventHub.getInstance().processMessage(msgProtocol,senderIp);
				}else{
					NearChatLog.i("UDPReceiveThread run() --> processMessage 自己发送的数据,忽略");
				}
			}
			//
			if (datagramPacket != null) {
				 datagramPacket.setLength(BUFFERLENGTH);
	        }
		}
		if (datagramSocket != null) {
	        	datagramSocket.close();
	            datagramSocket = null;
	    }
	}
	
	
	/**
	 * 
	 * 初始化Socket
	 */
	public void initSocket(){
		try {
		if(datagramSocket == null){
				datagramSocket = new DatagramSocket(MSGProtocolConst.UDP_RECEVE_SOCKET);
				NearChatLog.i("UDPReceiveThread initSocket() --> 初始化 datagramSocket,datagramPacket PORT: "+MSGProtocolConst.UDP_RECEVE_SOCKET);
			}
		
		if(datagramPacket == null){
			datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void startReceive(){
		if(receiveThread==null){
			isThreadRunning = true;
			receiveThread = new Thread(this);
			
			initSocket();
			
			
			NearChatLog.i("UDPReceiveThread startReceive() --> UDPThread startReceve 开启UDP线程  ");
			receiveThread.start();
		}
	}
	
	public void stopReceive(){
	   isThreadRunning = false;
        if (receiveThread != null)
        	receiveThread.interrupt();
        receiveThread = null;
        NearChatLog.i("UDPReceiveThread stopReceive() --> stopUDPSocketThread() 线程停止");		
	}
	

	
}
