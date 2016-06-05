package coffee1993.nearchat.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import coffee1993.nearchat.message.MSGProtocol;
import coffee1993.nearchat.message.MSGProtocolConst;
import coffee1993.nearchat.util.NearChatLog;

import com.alibaba.fastjson.JSON;


public class UDPSenderThread implements Runnable{

	private InetAddress targetIp;
	private String targetIpStr;
	private DatagramPacket datagramPacket;
	private DatagramSocket datagramSocket;
	private MSGProtocol msgProtocol;
	private static final int SENDBUFFER = 1024;
	private byte[] sendbuffer = new byte[SENDBUFFER];
	private Thread sendThread;
	
	
	public UDPSenderThread(String targetIp,MSGProtocol msgProtocol) {
		this.targetIpStr = targetIp;
		this.msgProtocol = msgProtocol;
	}
	
	
	@Override
	public void run() {
		String msgProtocolJSON = "";
		try {
			
			targetIp = InetAddress.getByName(targetIpStr);
			//将协议转换成 JSON数据字符串格式的字节数组
			msgProtocolJSON = JSON.toJSONString(msgProtocol);
			NearChatLog.i("UDPSenderThread run()--> 发送的数据 jsonStr"+msgProtocolJSON+" -->>> 端口号:"+MSGProtocolConst.UDP_RECEVE_SOCKET+"-->>> IP:"+targetIpStr);
			
			
			sendbuffer = msgProtocolJSON.getBytes("utf-8");			
			
			//the port of the target host.
			datagramPacket = new DatagramPacket(sendbuffer, sendbuffer.length,targetIp,MSGProtocolConst.UDP_RECEVE_SOCKET);
			datagramSocket = new DatagramSocket();
			datagramSocket.send(datagramPacket);
			
			//getPort 获取这个socket获取的远程端口 为啥是1不是RECEVE_SOCKET
			NearChatLog.i("UDPSenderThread run()--> 数据包发送成功"+"ip"+targetIpStr+"port "+" local port"+datagramSocket.getLocalPort());
		} catch (IOException e) {			
			
			//当自己开启热点后，无法获取线上的小伙伴 发送Socket数据包失败 原因：
			NearChatLog.e("UDPSenderThread run()--> UDP 数据包发送失败"+"ip"+targetIpStr+" ,port "+datagramSocket.getPort()+" getLocalport():"+datagramSocket.getLocalPort());
			e.printStackTrace();
		}
		
	}
	
	
	public void startSendThread(){
		
		NearChatLog.i("startSendThread() --> 开启单线程 发送UDP数据包");
		sendThread = new Thread(this);
		sendThread.start();		
	}

	
	
}
