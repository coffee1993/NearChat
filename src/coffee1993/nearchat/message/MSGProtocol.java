package coffee1993.nearchat.message;


import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import coffee1993.nearchat.entity.Entity;
import coffee1993.nearchat.entity.MsgEntity;
import coffee1993.nearchat.entity.NearUser;
import coffee1993.nearchat.util.JsonUtils;
import coffee1993.nearchat.util.NearChatLog;

import com.alibaba.fastjson.annotation.JSONField;


public class MSGProtocol {
	
	/**
	 * 
	 * UDP数据包 ，协议设计：
	 * 
	 * 数据包编号 发送者IMEI 命令编号 附加数据类型 附加对象 附加消息 
	 * 
	 */
	
    private static final String TAG = "SZU_IPMSGPProtocol";
    private static final String PACKETNO = "packetNo";
    private static final String COMMANDNO = "commandNo";
    private static final String ADDOBJECT = "addObject";
    private static final String ADDSTR = "addStr";
    private static final String ADDTYPE = "addType";

    private String packetNo;// 数据包编号
    private String senderIMEI; // 发送者IMEI
    private int commandNo; // 命令
    private ADDITION_TYPE addType; // 附加数据类型
    private Entity addObject; // 附加对象
    private String addStr; // 附加信息

    public MSGProtocol() {
        this.packetNo = getSeconds();
    }

    public enum ADDITION_TYPE {
        USER, MSG, STRING
    }

    // 根据协议字符串初始化
    public MSGProtocol(String paramProtocolJSON) {
        try {
            JSONObject protocolJSON = new JSONObject(paramProtocolJSON);
            packetNo = protocolJSON.getString(PACKETNO);
            commandNo = protocolJSON.getInt(COMMANDNO);
            senderIMEI = protocolJSON.getString(NearUser.IMEI);
            if (protocolJSON.has(ADDTYPE)) { // 若有附加信息
                String addJSONStr = null;
                if (protocolJSON.has(ADDOBJECT)) { // 若为Entity类型
                    addJSONStr = protocolJSON.getString(ADDOBJECT);
                }
                else if (protocolJSON.has(ADDSTR)) { // 若为String类型
                    addJSONStr = protocolJSON.getString(ADDSTR);
                }
                switch (ADDITION_TYPE.valueOf(protocolJSON.getString(ADDTYPE))) {
                    case USER: // 为用户数据
                        addObject = JsonUtils.getObject(addJSONStr, NearUser.class);
                        break;

                    case MSG: // 为消息数据
                        addObject = JsonUtils.getObject(addJSONStr, MsgEntity.class);
                        break;

                    case STRING: // 为String数据
                        addStr = addJSONStr;
                        break;

                    default:
                        break;
                }

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            NearChatLog.e(TAG, "非标准JSON文本");
        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, Entity paramObject) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addObject = paramObject;
        if (paramObject instanceof MsgEntity) { // 若为Message对象
            this.addType = ADDITION_TYPE.MSG;
        }
        else if (paramObject instanceof NearUser) { // 若为NearByPeople对象
            this.addType = ADDITION_TYPE.USER;
        }
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo, String paramStr) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addStr = paramStr;
        this.addType = ADDITION_TYPE.STRING;
    }

    public MSGProtocol(String paramSenderIMEI, int paramCommandNo) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
    }

    @JSONField(name = PACKETNO)
    public String getPacketNo() {
        return this.packetNo;
    }

    public void setPacketNo(String paramPacketNo) {
        this.packetNo = paramPacketNo;
    }

    @JSONField(name = NearUser.IMEI)
    public String getSenderIMEI() {
        return this.senderIMEI;
    }

    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

    @JSONField(name = ADDTYPE)
    public ADDITION_TYPE getAddType() {
        return this.addType;
    }

    public void setAddType(ADDITION_TYPE paramType) {
        this.addType = paramType;
    }

    @JSONField(name = COMMANDNO)
    public int getCommandNo() {
        return this.commandNo;
    }

    public void setCommandNo(int paramCommandNo) {
        this.commandNo = paramCommandNo;
    }

    @JSONField(name = ADDOBJECT)
    public Entity getAddObject() {
        return this.addObject;
    }

    public void setAddObject(Entity paramObject) {
        this.addObject = paramObject;
    }

    @JSONField(name = ADDSTR)
    public String getAddStr() {
        return this.addStr;
    }

    public void setAddStr(String paramStr) {
        this.addStr = paramStr;
    }

    // 输出协议JSON串
    @JSONField(serialize = false)
    public String getProtocolJSON() {
        return JsonUtils.createJsonString(this);
    }

    // 得到数据包编号，毫秒数
    @JSONField(serialize = false)
    private String getSeconds() {
        Date nowDate = new Date();
        return Long.toString(nowDate.getTime());
    }

}
