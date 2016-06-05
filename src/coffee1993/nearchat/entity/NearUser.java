package coffee1993.nearchat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import coffee1993.nearchat.R;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @fileName NearByPeople.java
 * @description 附近个人实体类
 * @author zhangyi
 */
public class NearUser extends Entity implements Parcelable {

    /** 用户常量 **/

    // 共有
    public static final String AGE = "Age";
    public static final String AVATAR = "avatar";
    public static final String ONLINESTATEINT = "OnlineStateInt";
    public static final String NICKNAME = "Nickname";
    public static final String GENDER = "Gender";
    public static final String IMEI = "IMEI";
    public static final String DEVICE = "Device";
    public static final String BIRTHDAY = "birthday";
    public static final String CONSTELLATION = "Constellation";
    public static final String IPADDRESS = "Ipaddress";
    public static final String LOGINTIME = "LoginTime";

    
    
    public static final String NEARUSER = "currentUser";
    
    
    
    // 个人
    public static final String ID = "ID";
    public static final String ISCLIENT = "isClient";
    public static final String SERVERIPADDRESS = "serverIPaddress";
    public static final String ENTITY_PEOPLE = "entity_people";

    private int mAge;
    private int mAvatar;
    private int mOnlineStateInt;
    private String mNickname;
    private String mGender;
    private String mIMEI;
    private String mDevice;
    private String mBirthday;
    private String mConstellation;
    private String mIpaddress;
    private String mLogintime;

    private int mGenderId;
    private int mGenderBgId;
    private int msgCount;

    public NearUser() {
        this.msgCount = 0;
    }

    public NearUser(int age, int avatar, int onlinestate, String nickname, String gender, String IMEI,
            String device, String birthday, String constellation, String ip, String logintime) {
        this.mAge = age;
        this.mAvatar = avatar;
        this.mOnlineStateInt = onlinestate;
        this.mNickname = nickname;
        this.setGender(gender);
        this.mIMEI = IMEI;
        this.mDevice = device;
        this.mBirthday = birthday;
        this.mConstellation = constellation;
        this.mIpaddress = ip;
        this.mLogintime = logintime;

    }

    /** 共用变量 get set **/

    @JSONField(name = NearUser.AGE)
    public int getAge() {
        return this.mAge;
    }

    @JSONField(name = NearUser.AVATAR)
    public int getAvatar() {
        return this.mAvatar;
    }

    @JSONField(name = NearUser.ONLINESTATEINT)
    public int getOnlineStateInt() {
        return this.mOnlineStateInt;
    }

    @JSONField(name = NearUser.NICKNAME)
    public String getNickname() {
        return this.mNickname;
    }

    @JSONField(name = NearUser.GENDER)
    public String getGender() {
        return this.mGender;
    }

    @JSONField(name = NearUser.IMEI)
    public String getIMEI() {
        return this.mIMEI;
    }

    @JSONField(name = NearUser.DEVICE)
    public String getDevice() {
        return this.mDevice;
    }

    @JSONField(name = NearUser.BIRTHDAY)
    public String getBirthday() {
        return this.mBirthday;
    }

    @JSONField(name = NearUser.CONSTELLATION)
    public String getConstellation() {
        return this.mConstellation;
    }

    @JSONField(name = NearUser.IPADDRESS)
    public String getIpaddress() {
        return this.mIpaddress;
    }

    @JSONField(name = NearUser.LOGINTIME)
    public String getLogintime() {
        return this.mLogintime;
    }

    public void setAge(int paramAge) {
        this.mAge = paramAge;
    }

    public void setAvatar(int paramAvatar) {
        this.mAvatar = paramAvatar;
    }

    public void setOnlineStateInt(int paramOnlineState) {
        this.mOnlineStateInt = paramOnlineState;
    }

    public void setNickname(String paramNickname) {
        this.mNickname = paramNickname;
    }

    public void setGender(String paramGender) {
        this.mGender = paramGender;
        if ("女".equals(paramGender)) {
            setGenderId(R.drawable.ic_user_famale);
            setGenderBgId(R.drawable.bg_gender_famal);
        }
        else {
            setGenderId(R.drawable.ic_user_male);
            setGenderBgId(R.drawable.bg_gender_male);
        }
    }

    public void setIMEI(String paramIMEI) {
        this.mIMEI = paramIMEI;
    }

    public void setDevice(String paramDevice) {
        this.mDevice = paramDevice;
    }

    public void setBirthday(String paramBirthday) {
        this.mBirthday = paramBirthday;
    }

    public void setConstellation(String paramConstellation) {
        this.mConstellation = paramConstellation;
    }

    public void setIpaddress(String paramIpaddress) {
        this.mIpaddress = paramIpaddress;
    }

    public void setLogintime(String paramLogintime) {
        this.mLogintime = paramLogintime;
    }

    /** 个人变量 get set **/

    @JSONField(serialize = false)
    public int getGenderId() {
        return this.mGenderId;
    }

    public void setGenderId(int paramGenderId) {
        this.mGenderId = paramGenderId;
    }

    @JSONField(serialize = false)
    public int getGenderBgId() {
        return this.mGenderBgId;
    }

    public void setGenderBgId(int paramGenderBgId) {
        this.mGenderBgId = paramGenderBgId;
    }

    @JSONField(serialize = false)
    public int getMsgCount() {
        return this.msgCount;
    }

    public void setMsgCount(int paramMsgCount) {
        this.msgCount = paramMsgCount;
    }

    public static Parcelable.Creator<NearUser> getCreator() {
        return CREATOR;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mAge);
        out.writeInt(mAvatar);
        out.writeInt(mOnlineStateInt);
        out.writeString(mNickname);
        out.writeString(mGender);
        out.writeString(mIMEI);
        out.writeString(mDevice);
        out.writeString(mBirthday);
        out.writeString(mConstellation);
        out.writeString(mIpaddress);
        out.writeString(mLogintime);
        out.writeInt(msgCount);
    }

    public static final Parcelable.Creator<NearUser> CREATOR = new Parcelable.Creator<NearUser>() {

        @Override
        public NearUser createFromParcel(Parcel source) {
            NearUser user = new NearUser();
            user.setAge(source.readInt());
            user.setAvatar(source.readInt());
            user.setOnlineStateInt(source.readInt());
            user.setNickname(source.readString());
            user.setGender(source.readString());
            user.setIMEI(source.readString());
            user.setDevice(source.readString());
            user.setBirthday(source.readString());
            user.setConstellation(source.readString());
            user.setIpaddress(source.readString());
            user.setLogintime(source.readString());
            user.setMsgCount(source.readInt());
            return user;
        }

        @Override
        public NearUser[] newArray(int size) {
            return new NearUser[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
