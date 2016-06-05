package coffee1993.nearchat;


import java.util.Stack;

import android.content.Context;
import coffee1993.nearchat.activity.chat.ChatActivity;
import coffee1993.nearchat.message.EventHub;

public class ActivitiesManager {

    private static Stack<BaseActivity> queue;
    private static EventHub mListener;
    private static ChatActivity mChatActivity;

    public static void init(Context context) {
        queue = new Stack<BaseActivity>();
        //
      //  mListener = EventHub.getInstance();
    }

    public static void addActivity(BaseActivity activity) {
        queue.add(activity);
    }

    public static void finishActivity(BaseActivity activity) {
        if (activity != null) {
            queue.remove(activity);
        }
    }

    public static void finishAllActivities() {
    	
    	mListener = EventHub.getInstance();
    	mListener.notifyOffline();
        mListener.stopReceved();
        mListener = null;
        for (BaseActivity activity : queue) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        queue.clear();
    }

    public static int getActivitiesNum() {
        if (!queue.isEmpty()) {
            return queue.size();
        }
        return 0;
    }

    public static BaseActivity getCurrentActivity() {
        if (!queue.isEmpty()) {
            return queue.lastElement();
        }
        return null;
    }

    public static void initChatActivity(ChatActivity pActivity) {
        mChatActivity = pActivity;
    }

    public static void removeChatActivity() {
        mChatActivity = null;
    }

    public static ChatActivity getChatActivity() {
        return mChatActivity;
    }

}
