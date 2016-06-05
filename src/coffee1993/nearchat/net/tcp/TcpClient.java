package coffee1993.nearchat.net.tcp;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import coffee1993.nearchat.NearChatApplication;
import coffee1993.nearchat.entity.MsgEntity;
import coffee1993.nearchat.file.Constant;
import coffee1993.nearchat.file.FileState;
import coffee1993.nearchat.file.FileStyle;
import coffee1993.nearchat.message.EventHub;
import coffee1993.nearchat.message.MSGProtocolConst;
import coffee1993.nearchat.session.SessionManager;
import coffee1993.nearchat.util.DateUtil;
import coffee1993.nearchat.util.FileUtils;
import coffee1993.nearchat.util.NearChatLog;


public class TcpClient implements Runnable {
    private static final String TAG = "TcpClient";

    private Thread mThread;
    private boolean IS_THREAD_STOP = false; // 是否线程开始标志
    private boolean SEND_FLAG = false; // 是否发送广播标志
    private static Context mContext = null;
    private static TcpClient instance;
    // private ArrayList<FileStyle> fileStyles;
    // private ArrayList<FileState> fileStates;
    private ArrayList<SendFileThread> sendFileThreads;
    private SendFileThread sendFileThread;
    private static Handler mHandler = null;

    private TcpClient() {
        sendFileThreads = new ArrayList<TcpClient.SendFileThread>();
        mThread = new Thread(this);
        NearChatLog.d(TAG, "建立线程成功");

    }

    
   
    
    public static void setHandler(Handler paramHandler) {
        mHandler = paramHandler;
    }

    public Thread getThread() {
        return mThread;
    }

    /**
     * <p>
     * 获取TcpService实例
     * <p>
     * 单例模式，返回唯一实例
     */
    public static TcpClient getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new TcpClient();
        }
        return instance;
    }

    public void sendFile(ArrayList<FileStyle> fileStyles, ArrayList<FileState> fileStates,
            String target_IP) {
        while (SEND_FLAG == true)
            ;

        for (FileStyle fileStyle : fileStyles) {
            SendFileThread sendFileThread = new SendFileThread(target_IP, fileStyle.fullPath);
            sendFileThreads.add(sendFileThread);
        }
        SEND_FLAG = true;
    }

    private TcpClient(Context context) {
        this();
        NearChatLog.d(TAG, "TCP_Client初始化完毕");
    }

    public void startSend() {
        NearChatLog.d(TAG, "发送线程开启");
        IS_THREAD_STOP = false; // 使能发送标识
        if (!mThread.isAlive())
            mThread.start();
    }

    public void sendFile(String filePath, String target_IP) {
        SendFileThread sendFileThread = new SendFileThread(target_IP, filePath);
        while (SEND_FLAG == true)
            ;
        sendFileThreads.add(sendFileThread);
        SEND_FLAG = true;
    }

    public void sendFile(String filePath, String target_IP, MsgEntity.CONTENT_TYPE type) {
        SendFileThread sendFileThread = new SendFileThread(target_IP, filePath, type);
        while (SEND_FLAG == true)
            ;
        sendFileThreads.add(sendFileThread);
        FileState sendFileState = new FileState(filePath);
        NearChatApplication.sendFileStates.put(filePath, sendFileState);// 全局可访问的文件发送状态读取
        SEND_FLAG = true;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        NearChatLog.d(TAG, "TCP_Client初始化");

        while (!IS_THREAD_STOP) {
            if (SEND_FLAG) {
                for (SendFileThread sendFileThread : sendFileThreads) {
                    sendFileThread.start();
                }
                sendFileThreads.clear();
                SEND_FLAG = false;
            }

        }
    }

    public void release() {
        while (SEND_FLAG == true)
            ;
        while (sendFileThread.isAlive())
            ;
        IS_THREAD_STOP = false;
    }

    public class SendFileThread extends Thread {
        private static final String TAG = "SZU_SendFileThread";
        private boolean SEND_FLAG = true; // 是否发送广播标志
        private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE]; // 数据报内容
        private OutputStream output = null;
        private DataOutputStream dataOutput;
        private FileInputStream fileInputStream;
        private Socket socket = null;
        private String target_IP;
        private String filePath;
        private MsgEntity.CONTENT_TYPE type;

        public SendFileThread(String target_IP, String filePath) {
            this.target_IP = target_IP;
            this.filePath = filePath;
        }

        public SendFileThread(String target_IP, String filePath, MsgEntity.CONTENT_TYPE type) {
            this(target_IP, filePath);
            this.type = type;
        }

        public void sendFile() {
            int readSize = 0;
            try {
                socket = new Socket(target_IP, Constant.TCP_SERVER_RECEIVE_PORT);
                fileInputStream = new FileInputStream(new File(filePath));
                output = socket.getOutputStream();
                dataOutput = new DataOutputStream(output);
                int fileSize = fileInputStream.available();
                dataOutput.writeUTF(filePath.substring(filePath.lastIndexOf(File.separator) + 1)
                        + "!" + fileSize + "!" + SessionManager.getIMEI() + "!" + type);
                int count = 0;
                long length = 0;

                FileState fs = NearChatApplication.sendFileStates.get(filePath);
                fs.fileSize = fileSize;
                fs.type = type;
                while (-1 != (readSize = fileInputStream.read(mBuffer))) {
                    length += readSize;
                    dataOutput.write(mBuffer, 0, readSize);
                    count++;
                    fs.percent = (int) (length * 100 / fileSize);

                    switch (type) {
                        case IMAGE:
                            break;

                        case VOICE:
                            break;

                        case FILE:
                            android.os.Message msg = mHandler.obtainMessage();
                            msg.obj = fs;
                            msg.sendToTarget();

                            break;

                        default:
                            break;
                    }
                    dataOutput.flush();
                }
                NearChatLog.d(TAG, fs.fileName + "发送完毕");

                output.close();
                dataOutput.close();
                socket.close();

                switch (type) {
                    case IMAGE:
                        MsgEntity imageMsg = new MsgEntity(SessionManager.getIMEI(),
                                DateUtil.getNowtime(), fs.fileName, type);
                        imageMsg.setMsgContent(FileUtils.getNameByPath(imageMsg.getMsgContent()));
                        EventHub.getInstance().sendUDPdata(MSGProtocolConst.IPMSG_SENDMSG, target_IP, imageMsg);
                        NearChatLog.d(TAG, "图片发送完毕");
                        break;

                    case VOICE:
                        MsgEntity voiceMsg = new MsgEntity(SessionManager.getIMEI(),
                                DateUtil.getNowtime(), fs.fileName, type);
                        voiceMsg.setMsgContent(FileUtils.getNameByPath(voiceMsg.getMsgContent()));
                        EventHub.getInstance().sendUDPdata(MSGProtocolConst.IPMSG_SENDMSG, target_IP, voiceMsg);
                        NearChatLog.d(TAG, "语音发送完毕");
                        break;

                    case FILE:
                        android.os.Message msg = mHandler.obtainMessage();
                        fs.percent = 100;
                        msg.obj = fs;
                        msg.sendToTarget();
                        break;

                    default:
                        break;
                }

                NearChatApplication.sendFileStates.remove(fs.fileName);
            }
            catch (UnknownHostException e) {
                NearChatLog.d(TAG, "建立客户端socket失败");
                SEND_FLAG = false;
                e.printStackTrace();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                NearChatLog.d(TAG, "建立客户端socket失败");
                SEND_FLAG = false;
                e.printStackTrace();
            }
            finally {
                // IS_THREAD_STOP=true;
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            NearChatLog.d(TAG, "SendFileThread初始化");
            if (SEND_FLAG) {
                sendFile();
            }
        }
    }
}
