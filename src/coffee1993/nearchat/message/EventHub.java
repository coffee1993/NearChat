package coffee1993.nearchat.message;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import coffee1993.nearchat.ActivitiesManager;
import coffee1993.nearchat.NearChatApplication;
import coffee1993.nearchat.activity.chat.ChatActivity;
import coffee1993.nearchat.entity.Entity;
import coffee1993.nearchat.entity.MsgEntity;
import coffee1993.nearchat.entity.NearUser;
import coffee1993.nearchat.net.tcp.TcpService;
import coffee1993.nearchat.net.udp.UDPManager;
import coffee1993.nearchat.session.SessionManager;
import coffee1993.nearchat.sql.SqlDBManager;
import coffee1993.nearchat.util.ImageUtils;
import coffee1993.nearchat.util.NearChatLog;



/**
 * 消息处理中枢 事件分发中枢
 * 耦合数据持久层 和网络层 做到数据持久层和网络层的隔离
 * @author zhangyi
 */
public class EventHub {
	private static String TAG = "EventHub ";
	private List<OnNewMsgListener> msgListeners;
	private SqlDBManager sqlDBManager; //解耦合
	//线程安全 不会重复
	private static EventHub instance;
	private UDPManager udpManager;
	private NearUser mLocalUser;
	
	private ArrayList<NearUser> mUnReadPeopleList; // 未读消息的用户队列
	private HashMap<String, NearUser> mOnlineUsers; // 在线用户集合，以IMEI为KEY
	private HashMap<String, String> mLastMsgCache; // 最后一条消息缓存，以IMEI为KEY
	
	private Context mContext;
	
	private Handler mChatHandler;
	public Handler getmChatHandler() {
		return mChatHandler;
	}

	public void setmChatHandler(Handler mChatHandler) {
		NearChatLog.i("EventHub setChatActivityHandler() --> 设置handler");
		this.mChatHandler = mChatHandler;
	}

	public EventHub() {	
		udpManager = UDPManager.getInstance();
		sqlDBManager = SqlDBManager.getInstance();
		msgListeners = new ArrayList<OnNewMsgListener>();
		mOnlineUsers = new LinkedHashMap<String,NearUser>();
		mUnReadPeopleList = new ArrayList<NearUser>();   
		mLastMsgCache = new HashMap<String,String>();
		mLocalUser = SessionManager.getLocalUserInfo();
		mContext = NearChatApplication.getContext();
	}
	//检测调用顺序
	public static EventHub getInstance(){
		if(instance == null){
			synchronized (EventHub.class) {
				if(instance == null){
					NearChatLog.i("EventHub getInstance() --> 第一次初始化EventHub");
					instance = new EventHub();
				}		
			}
		}		
		return instance;
	}
	
	/**
	 * 注册回调
	 * @param listener
	 */
	public void addMsgListener(OnNewMsgListener listener) {
		if(listener!=null){
			NearChatLog.i("EventHub addMsgListener --> 添加监听器 MainTabActivity");
			msgListeners.add(listener);			
		}
	}
	//================== 消息中枢处理     start ================= 
	
	/**
	 * 网络 接收消息 处理
	 * @param msgProtocol
	 */

    public void processMessage( MSGProtocol msgProtocol,
            String senderIp) {
    	//获取发送方IMEI和ip信息
    	String senderIMEI = msgProtocol.getSenderIMEI();
    	int commandNo =  msgProtocol.getCommandNo();
    	TcpService tcpService; 	

    	switch (commandNo) {
        // 收到上线数据包，添加用户，并回送IPMSG_ANSENTRY应答。
            case MSGProtocolConst.IPMSG_BR_ENTRY: {
                NearChatLog.i(TAG, "收到上线通知");
                NearUser user =  (NearUser)msgProtocol.getAddObject();
                addUser(msgProtocol);
            	sendUDPdata(MSGProtocolConst.IPMSG_ANSENTRY, user.getIpaddress(),mLocalUser);
                NearChatLog.i(TAG, "成功发送上线应答");
            }
                break;

            // 收到上线应答，更新在线用户列表
            case MSGProtocolConst.IPMSG_ANSENTRY: {
                NearChatLog.i(TAG, "收到上线应答");
                addUser(msgProtocol);
            }
                break;

            // 收到下线广播
            case MSGProtocolConst.IPMSG_BR_EXIT: {
                removeOnlineUser(senderIMEI, 1);
                NearChatLog.i(TAG, "成功删除imei为" + senderIMEI + "的用户");
            }
                break;

            case MSGProtocolConst.IPMSG_REQUEST_IMAGE_DATA:
                NearChatLog.i(TAG, "收到IMAGE发送请求");
                tcpService = TcpService.getInstance(NearChatApplication.getContext());
                tcpService.setSavePath(NearChatApplication.IMAG_PATH);
                tcpService.startReceive();
                sendUDPdata(MSGProtocolConst.IPMSG_CONFIRM_IMAGE_DATA, senderIp,null);
                break;

            case MSGProtocolConst.IPMSG_REQUEST_VOICE_DATA:
                NearChatLog.i(TAG, "收到VOICE发送请求");
                tcpService = TcpService.getInstance(mContext);
                tcpService.setSavePath(NearChatApplication.VOICE_PATH);
                tcpService.startReceive();
                sendUDPdata(MSGProtocolConst.IPMSG_CONFIRM_VOICE_DATA, senderIp,null);
                break;

            case MSGProtocolConst.IPMSG_SENDMSG: {
                NearChatLog.i(TAG, "收到MSG消息");
                MsgEntity msg = (MsgEntity) msgProtocol.getAddObject();

                switch (msg.getContentType()) {
                    case TEXT:
                        sendUDPdata(MSGProtocolConst.IPMSG_RECVMSG, senderIp, msgProtocol.getPacketNo());
                        break;

                    case IMAGE:
                        NearChatLog.i(TAG, "收到图片信息");
                        msg.setMsgContent(NearChatApplication.IMAG_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        String THUMBNAIL_PATH = NearChatApplication.THUMBNAIL_PATH + File.separator
                                + msg.getSenderIMEI();

                        NearChatLog.d(TAG, "缩略图文件夹路径:" + THUMBNAIL_PATH);
                        NearChatLog.d(TAG, "图片文件路径:" + msg.getMsgContent());

                        ImageUtils.createThumbnail(mContext, msg.getMsgContent(), THUMBNAIL_PATH
                                + File.separator);
                        break;

                    case VOICE:
                        NearChatLog.i(TAG, "收到录音信息");
                        msg.setMsgContent(NearChatApplication.VOICE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        NearChatLog.d(TAG, "文件路径:" + msg.getMsgContent());
                        break;

                    case FILE:
                        NearChatLog.i(TAG, "收到文件 发送请求");
                        tcpService = TcpService.getInstance(mContext);
                        tcpService.setSavePath(NearChatApplication.FILE_PATH);
                        tcpService.startReceive();
                        sendUDPdata(MSGProtocolConst.IPMSG_CONFIRM_FILE_DATA, senderIp,null);
                        msg.setMsgContent(NearChatApplication.FILE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        NearChatLog.d(TAG, "文件路径:" + msg.getMsgContent());
                        break;
                }

                // 加入数据库
                sqlDBManager.addChattingInfo(senderIMEI, SessionManager.getIMEI(), msg.getSendTime(),
                        msg.getMsgContent(), msg.getContentType());

                // 加入未读消息列表
                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;
                pMessage.obj = msg;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v == null) {
                    addUnReadPeople(getOnlineUser(senderIMEI)); // 添加到未读用户列表
                    for (int i = 0; i < msgListeners.size(); i++) {
                        android.os.Message pMsg = new android.os.Message();
                        pMsg.what = MSGProtocolConst.IPMSG_RECVMSG;
                        msgListeners.get(i).onMsgRecive(pMsg);
                    }
                }
                else {
                    v.processMessage(pMessage);
                }
                addLastMsgCache(senderIMEI, msg); // 添加到消息缓存
                NearChatApplication.playNotification();

            }
                break;

            default:
                NearChatLog.i(TAG, "收到命令：" + commandNo);

                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v != null) {
                    v.processMessage(pMessage);
                }

                break;

        } 
    }
    //================== 消息中枢处理     end   ================= 

	
	/** =====  EventHub 业务逻辑 start =============
	 */
	
	/**
     * 添加用户到在线列表中 (线程安全的)
     * 
     * @param paramIPMSGProtocol
     *            包含用户信息的IPMSGProtocol字符串
     */
    private void addUser(MSGProtocol paramIPMSGProtocol) {
        String receiveIMEI = paramIPMSGProtocol.getSenderIMEI();
        if (NearChatApplication.isDebugmode) {
            NearUser newUser = (NearUser) paramIPMSGProtocol.getAddObject();
            addOnlineUser(receiveIMEI, newUser);
            sqlDBManager.addUserInfo(newUser);
        }
        else {
            if (!SessionManager.isLocalUser(receiveIMEI)) {
                NearUser newUser = (NearUser) paramIPMSGProtocol.getAddObject();
                addOnlineUser(receiveIMEI, newUser);
                sqlDBManager.addUserInfo(newUser);
            }
        }
        NearChatLog.i(TAG, "成功添加imei为" + receiveIMEI + "的用户");

    }
    public synchronized void addOnlineUser(String paramIMEI, NearUser paramObject) {
        mOnlineUsers.put(paramIMEI, paramObject);
        for (int i = 0; i < msgListeners.size(); i++) {
            android.os.Message pMsg = new android.os.Message();
            pMsg.what = MSGProtocolConst.IPMSG_BR_ENTRY;
            msgListeners.get(i).onMsgRecive(pMsg);
        }
        NearChatLog.d(TAG, "addUser | mOnlineUsersNum：" + mOnlineUsers.size());
    }

    public NearUser getOnlineUser(String paramIMEI) {
        return mOnlineUsers.get(paramIMEI);
    }

    public HashMap<String, NearUser> getOnlineUserMap() {
        return mOnlineUsers;
    }
    
    /**
     * 新增未读消息用户
     * 
     * @param people
     */
    public void addUnReadPeople(NearUser people) {
        if (!mUnReadPeopleList.contains(people))
            mUnReadPeopleList.add(people);
    }

    /**
     * 获取未读消息队列
     * 
     * @return
     */
    public ArrayList<NearUser> getUnReadPeopleList() {
        return mUnReadPeopleList;
    }

    /**
     * 获取未读用户数
     * 
     * @return
     */
    public int getUnReadPeopleSize() {
        return mUnReadPeopleList.size();
    }

    /**
     * 移除指定未读用户
     * 
     * @param people
     */
    public void removeUnReadPeople(NearUser people) {
        if (mUnReadPeopleList.contains(people))
            mUnReadPeopleList.remove(people);
    }


	/**
	 * 移除 监听
	 * @param mainTabActivity
	 */
	public void removeMsgListener(OnNewMsgListener mainTabActivity) {
		this.msgListeners.remove(mainTabActivity);
	}
	
    /**
     * 移除消息缓存
     * 
     * @param paramIMEI
     *            需要清除缓存的用户IMEI
     */
    public void removeLastMsgCache(String paramIMEI) {
        mLastMsgCache.remove(paramIMEI);
    }
    
    public void clearMsgCache() {
        mLastMsgCache.clear();
    }
    public void clearUnReadMessages() {
        mUnReadPeopleList.clear();
    }
    /**
     * 获取消息缓存
     * 
     * @param paramIMEI
     *            需要获取消息缓存记录的用户IMEI
     * @return
     */
    public String getLastMsgCache(String paramIMEI) {
        return mLastMsgCache.get(paramIMEI);
    }
    
    
	
	
	
	
	//================== UDP模块     start ================= 
    /**
     * 移除在线用户
     * 
     * @param paramIMEI
     *            需要移除的用户IMEI
     * @param paramtype
     *            操作类型，0:清空在线列表，1:移除指定用户
     */
    public void removeOnlineUser(String paramIMEI, int paramtype) {
        if (paramtype == 1) {
            mOnlineUsers.remove(paramIMEI);
            for (int i = 0; i < mOnlineUsers.size(); i++) {
                android.os.Message pMsg = new android.os.Message();
                pMsg.what = MSGProtocolConst.IPMSG_BR_EXIT;
                msgListeners.get(i).onMsgRecive(pMsg);
            }

        }
        else if (paramtype == 0) {
            mOnlineUsers.clear();
        }
        //0
        NearChatLog.i("EventHub removeOnlineUser() --> removeUser | mOnlineUsersNum：" + mOnlineUsers.size());
    }

    /** 用户下线通知 **/
  	public void notifyOffline() {
  		sendUDPdata(MSGProtocolConst.IPMSG_BR_EXIT, MSGProtocolConst.BROADCATS_IP,null);
  	}	
  	
  	/** 用户上线通知 **/
  	public void notifyOnline() {
  		NearUser nearUser = SessionManager.getLocalUserInfo();
  		if(nearUser!=null){		
  			NearChatLog.i("EventHub MSG_USER_ONLINE notifyOnline");			
  			sendUDPdata(MSGProtocolConst.IPMSG_BR_ENTRY, MSGProtocolConst.BROADCATS_IP, nearUser);
  		}else {
  			NearChatLog.i("SessionManager 当前用户为null");
  		}
  	}
    
	/**
	 * 开启UDP线程接收数据包
	 */
	public void startReceved() {
		if(udpManager!=null){
			udpManager.startReceved();			
		}
	}
	
	/**
	 * 暂停UDP线程接收数据包
	 */
	public void stopReceved(){
		if(udpManager!=null){			
			udpManager.stopReceved();
		}
	}

	/**
	 * 发送数据包
	 * 
	 * 	命令集合添加命令 不立刻执行 还是执行 先执行 先完成功能 再改善功能
	 * 封装MSGProtocol
	 * @param commandNo
	 * @param targetIp
	 * @param addObj
	 */
	public void sendUDPdata(int commandNo,String targetIp, Object addObj){
		NearChatLog.i("EventHub sendUDPdate() --> 封装 MSGProtocol");
		MSGProtocol msgProtocol = null;
		//发送时封装自己的IMEI
		String iMEI = SessionManager.getIMEI();
		//addType选择之后
		if(addObj==null){
			NearChatLog.i("EventHub sendUDPdate() --> addObj == null");
			msgProtocol = new MSGProtocol(iMEI,commandNo);
			udpManager.sendUDPdata(msgProtocol, targetIp);
		}
		else if(addObj instanceof String){
			NearChatLog.i("EventHub sendUDPdate() --> addObj == String");
			msgProtocol = new MSGProtocol(iMEI, commandNo, (String)addObj);
			udpManager.sendUDPdata(msgProtocol, targetIp);
		}else if(addObj instanceof Entity){
			NearChatLog.i("EventHub sendUDPdate() --> addObj == Entity");
			msgProtocol = new MSGProtocol(iMEI,commandNo,(Entity)addObj);
			udpManager.sendUDPdata(msgProtocol, targetIp);
		}
	}
	
	
	/**
     * 新增用户缓存，缓存的简单的字符串消息
     * 
     * @param paramIMEI
     *            新增记录的对应用户IMEI
     * @param paramMsg
     *            需要缓存的消息对象
     */
    public void addLastMsgCache(String paramIMEI, MsgEntity msg) {
        //TODO 缓存的对全是字符串
    	StringBuffer content = new StringBuffer();
        switch (msg.getContentType()) {
            case FILE:
                content.append("<FILE>: ").append(msg.getMsgContent());
                break;
            case IMAGE:
                content.append("<IMAGE>: ").append(msg.getMsgContent());
                break;
            case VOICE:
                content.append("<VOICE>: ").append(msg.getMsgContent()); //voicepath
                break;
            default:
                content.append(msg.getMsgContent());
                break;
        }
        if (msg.getMsgContent().isEmpty()) {
            content.append(" ");
        }
        
        //TODO 网络扩展接口
        udpManager.putLastMsgCache(paramIMEI, content.toString());
    }

    /** 刷新用户列表 **/
    public void refreshUsers() {
    	removeOnlineUser(null, 0);
    	notifyOnline();    	
    }

	//======================UDP 模块  end ================
    /** =====  EventHub 业务逻辑 end =============
	 */
	
}
