package coffee1993.nearchat.util;

import android.content.Context;
import android.content.SharedPreferences;
import coffee1993.nearchat.NearChatApplication;
import coffee1993.nearchat.entity.NearUser;
import coffee1993.nearchat.session.SessionManager;

public class SharePreferenceUtils {
    private static final String GlobalSharedName = "LocalUserInfo";
    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;

    public SharePreferenceUtils() {
    	//LocalUserInfo 创建
        mSP = NearChatApplication.getInstance().getSharedPreferences(GlobalSharedName,
                Context.MODE_PRIVATE);
        mEditor = mSP.edit();
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public String getIMEI() {
        return mSP.getString(NearUser.IMEI, "");
    }

    public String getNickname() {
        return mSP.getString(NearUser.NICKNAME, "");
    }

    public int getAvatarId() {
        return mSP.getInt(NearUser.AVATAR, 0);
    }

    public String getBirthday() {
        return mSP.getString(NearUser.BIRTHDAY, "000000");
    }

    public int getOnlineStateId() {
        return mSP.getInt(NearUser.ONLINESTATEINT, 0);
    }

    public String getGender() {
        return mSP.getString(NearUser.GENDER, "获取失败");
    }

    public int getAge() {
        return mSP.getInt(NearUser.AGE, -1);
    }

    public String getConstellation() {
        return mSP.getString(NearUser.CONSTELLATION, "获取失败");
    }

    public String getLogintime() {
        return mSP.getString(NearUser.LOGINTIME, "获取失败");
    }

    public void setNickname(String paramNickname) {
        mEditor.putString(NearUser.NICKNAME, "");
    }

    public void setIMEI(String paramIMEI) {
        mEditor.putString(NearUser.IMEI, paramIMEI);
    }

    public void setAvatarId(int paramAvatar) {
        mEditor.putInt(NearUser.AVATAR, paramAvatar);
    }

    public void setBirthday(String paramBirthday) {
        mEditor.putString(NearUser.BIRTHDAY, SessionManager.getBirthday());
    }

    public void setOnlineStateId(int paramOnlineStateId) {
        mEditor.putInt(NearUser.ONLINESTATEINT, paramOnlineStateId);
    }

    public void setGender(String paramGender) {
        mEditor.putString(NearUser.GENDER, paramGender);
    }

    public void setAge(int paramAge) {
        mEditor.putInt(NearUser.AGE, paramAge);
    }

    public void setConstellation(String paramConstellation) {
        mEditor.putString(NearUser.CONSTELLATION, paramConstellation);
    }

    public void setLogintime(String paramLongtime) {
        mEditor.putString(NearUser.LOGINTIME, paramLongtime);
    }
}
