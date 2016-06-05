package coffee1993.nearchat.activity.chat;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import coffee1993.nearchat.ActivitiesManager;
import coffee1993.nearchat.NearChatApplication;
import coffee1993.nearchat.R;
import coffee1993.nearchat.activity.ImageBrowserActivity;
import coffee1993.nearchat.activity.OtherProfileActivity;
import coffee1993.nearchat.adapter.ChatAdapter;
import coffee1993.nearchat.entity.MsgEntity;
import coffee1993.nearchat.entity.MsgEntity.CONTENT_TYPE;
import coffee1993.nearchat.entity.NearUser;
import coffee1993.nearchat.message.MSGProtocolConst;
import coffee1993.nearchat.net.tcp.TcpClient;
import coffee1993.nearchat.session.SessionManager;
import coffee1993.nearchat.sql.SqlDBManager;
import coffee1993.nearchat.util.AudioRecorderUtils;
import coffee1993.nearchat.util.FileUtils;
import coffee1993.nearchat.util.ImageUtils;
import coffee1993.nearchat.util.NearChatLog;
import coffee1993.nearchat.view.EmoteInputView;
import coffee1993.nearchat.view.EmoticonsEditText;
import coffee1993.nearchat.view.MultiListView;


public class ChatActivity extends BaseMessageActivity implements OnItemClickListener {

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitiesManager.initChatActivity(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 监听返回键
    @Override
    public void onBackPressed() {
        if (mLayoutMessagePlusBar.isShown()) {
            hidePlusBar();
        }
        else {
            finish();
        }
    	
    }

    @Override
    public void finish() {
        ActivitiesManager.removeChatActivity();

        if (null != mSqlDBManager) {// 关闭数据库连接
            mSqlDBManager.close();
            mSqlDBManager = null;
        }
        mRecordThread = null;
        super.finish();
    }

    @Override
    protected void initViews() {
        mClvList = (MultiListView) findViewById(R.id.chat_clv_list);
        mIvTextDitorAudio = (ImageView) findViewById(R.id.chat_textditor_iv_audio); //voice
        mIbTextDitorKeyBoard = (ImageButton) findViewById(R.id.chat_textditor_ib_keyboard); //voice change keyboard
       // mLayoutScroll = (ScrollLayout) findViewById(R.id.chat_slayout_scroll);
       // mLayoutRounds = (LinearLayout) findViewById(R.id.chat_layout_rounds);
        mInputView = (EmoteInputView) findViewById(R.id.chat_eiv_inputview);
        mEetTextDitorEditer = (EmoticonsEditText) findViewById(R.id.chat_textditor_eet_editer);
        mBtnTextDitoraudioSend  = (Button)findViewById(R.id.chat_textditor_btn_audio); //voice send 
        mIbTextDitorEmote = (ImageButton) findViewById(R.id.chat_textditor_ib_emote); //emotee
        
        mIbTextDitorPlus = (ImageButton) findViewById(R.id.chat_textditor_ib_plus); //+
        mBtnTextDitorSend = (Button) findViewById(R.id.chat_textditor_btn_send); //send
    
       
        
       
        mLayoutFullScreenMask = (LinearLayout) findViewById(R.id.fullscreen_mask);
        mLayoutMessagePlusBar = (LinearLayout) findViewById(R.id.message_plus_layout_bar);
        mLayoutMessagePlusPicture = (LinearLayout) findViewById(R.id.message_plus_layout_picture);
        mLayoutMessagePlusCamera = (LinearLayout) findViewById(R.id.message_plus_layout_camera);
        mLayoutMessagePlusFile = (LinearLayout) findViewById(R.id.message_plus_layout_file);
    }

    @Override
    protected void initEvents() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mClvList.setStackFromBottom(true);
        mClvList.setFastScrollEnabled(true);

       // mLayoutScroll.setOnScrollToScreen(this);
        mIbTextDitorPlus.setOnClickListener(this);
        mIbTextDitorEmote.setOnClickListener(this);
        mIbTextDitorKeyBoard.setOnClickListener(this);
        mBtnTextDitorSend.setOnClickListener(this);
        mIvTextDitorAudio.setOnClickListener(this);
        
        if(mEetTextDitorEditer==null){
        	showAlertDialog("222", "www");
        }else {			
        	mEetTextDitorEditer.addTextChangedListener(this); 
        	mEetTextDitorEditer.setVisibility(View.VISIBLE);
		}
		
        //mEetTextDitorEditer.setOnTouchListener(this);
        mIbTextDitorPlus.setOnClickListener(this);
        mBtnTextDitoraudioSend.setOnClickListener(this); //Voice
        mBtnTextDitoraudioSend.setOnTouchListener(this);
        mLayoutFullScreenMask.setOnTouchListener(this);
        mLayoutMessagePlusPicture.setOnClickListener(this);
        mLayoutMessagePlusCamera.setOnClickListener(this);
        mLayoutMessagePlusFile.setOnClickListener(this);

    }

    private void init() {
        mID = SessionManager.getLocalUserID();
        mNickName = SessionManager.getNickname();
        mIMEI = SessionManager.getIMEI();
        mChatUser = getIntent().getParcelableExtra(NearUser.ENTITY_PEOPLE);
        mSqlDBManager = new SqlDBManager(this);
        mSenderID = mSqlDBManager.getIDByIMEI(mChatUser.getIMEI()); //数据库获取

        mMessagesList = new ArrayList<MsgEntity>();
        mMessagesList = mSqlDBManager.getScrollMessageOfChattingInfo(0, 5, mSenderID, mID);
        
    
        mInputView.setEditText(mEetTextDitorEditer);
        
        
        setTitle(mChatUser.getNickname());
        initfolder();

        mAdapter = new ChatAdapter(ChatActivity.this, mMessagesList);
        mAdapter.setListView(mClvList);
        mAdapter.setChatUser(mChatUser);
        mClvList.setAdapter(mAdapter);        
        mClvList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    // actionBar的监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_userinfo:
                Intent intent = new Intent(ChatActivity.this, OtherProfileActivity.class);
                intent.putExtra(NearUser.ENTITY_PEOPLE, mChatUser);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return true;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (recordState == RECORD_OFF) {
                    switch (v.getId()) {
                        case R.id.chat_textditor_eet_editer:
                            mIbTextDitorEmote.setVisibility(View.VISIBLE);
                            showKeyBoard();
                            break;

                        case R.id.fullscreen_mask:
                            hidePlusBar();
                            break;
                        
                        case R.id.chat_textditor_btn_audio:
                        	downY = event.getY();
                        	isMove = false;
                        	startRecord();                        	
                        	mBtnTextDitoraudioSend.setText("松开 结束");
                        	showVoiceDialog(0);
                        	break;
                            
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (recordState == RECORD_ON) {
                    float moveY = event.getY();
                    if (moveY - downY < -50) {
                        isMove = true;
                        showVoiceDialog(1);
                    }
                    else if (moveY - downY < -20) {
                        isMove = false;
                        showVoiceDialog(0);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (recordState == RECORD_ON) {
                    stopRecord();
                    if (mRecordDialog.isShowing()) {
                        mRecordDialog.dismiss();
                        mBtnTextDitoraudioSend.setText("按住 说话");
                    }
                    if (!isMove) {
                        if (recodeTime < MIN_RECORD_TIME) {
                            showWarnToast(R.string.chat_toast_record_shorttime);
                        }
                        else {
                            mVoicePath = mAudioRecorder.getVoicePath();
                            sendMessage(mVoicePath, CONTENT_TYPE.VOICE);
                            refreshAdapter();
                        }
                    }

                    isMove = false;
                }
                if (mAudioRecorder != null)
                    mAudioRecorder = null;
                break;

        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_textditor_ib_plus:
                if (!mLayoutMessagePlusBar.isShown()) {
                    showPlusBar();
                }
                break;

            case R.id.chat_textditor_ib_emote:
                mEetTextDitorEditer.requestFocus();
                if (mInputView.isShown()) {
                    hideKeyBoard();
                }
                else {
                    hideKeyBoard();
                    mInputView.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.chat_textditor_ib_keyboard:
            	reAudioChangeiView();	
                break;

            case R.id.chat_textditor_btn_send:
                String content = mEetTextDitorEditer.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    mEetTextDitorEditer.setText(null);
                    sendMessage(content, CONTENT_TYPE.TEXT);
                    refreshAdapter();
                }
                break;

            case R.id.chat_textditor_iv_audio:            	
            	audioChangeview();
                break;
                
                                
            case R.id.message_plus_layout_picture:
                ImageUtils.selectPhoto(ChatActivity.this);
                hidePlusBar();
                break;

            case R.id.message_plus_layout_camera:
                mCameraImagePath = ImageUtils.takePicture(ChatActivity.this);
                hidePlusBar();
                break;

            case R.id.message_plus_layout_file:
                showFileChooser();
                hidePlusBar();
                break;
        }
    }

    private void audioChangeview() {
    	//改变布局
		mEetTextDitorEditer.setVisibility(View.GONE);
		mBtnTextDitoraudioSend.setVisibility(View.VISIBLE);
		mBtnTextDitoraudioSend.setText("按住 说话");
		//改变录音按键
		mIvTextDitorAudio.setVisibility(View.GONE);
		mIbTextDitorKeyBoard.setVisibility(View.VISIBLE);
	}
    
	private void reAudioChangeiView() {
		mBtnTextDitoraudioSend.setVisibility(View.GONE);
		mEetTextDitorEditer.setVisibility(View.VISIBLE);
		mIbTextDitorKeyBoard.setVisibility(View.GONE);
		mIvTextDitorAudio.setVisibility(View.VISIBLE);
	}

	@Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    
    //
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {   	
    	if (TextUtils.isEmpty(s)) {
        	mBtnTextDitorSend.setVisibility(View.GONE);
        	mIbTextDitorPlus.setVisibility(View.VISIBLE);
        }
        else {
        	mIbTextDitorPlus.setVisibility(View.GONE);
            mBtnTextDitorSend.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ImageUtils.INTENT_REQUEST_CODE_ALBUM:
                if (data == null) {
                	NearChatLog.i("ChatActivity onActivityResult():  data ==null");
                    return;
                }
                if (resultCode == RESULT_OK) {
                    if (data.getData() == null) {
                    	NearChatLog.i("ChatActivity onActivityResult():  requestCode OK data ==null");
                        return;
                    }
                    if (!FileUtils.isSdcardExist()) {
                    	NearChatLog.i("ChatActivity onActivityResult():  no sdcard");
                        showShortToast(R.string.toast_sdcard_unavailable);
                        return;
                    }
                    Uri uri = data.getData();
                    String[] proj = { MediaStore.Images.Media.DATA };
                    NearChatLog.i("ChatActivity onActivityResult():  照片获取");
                    Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                    if (cursor != null) {
                    	NearChatLog.i("ChatActivity onActivityResult():  照片获取");
                        int column_index = cursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                            
                            String path = cursor.getString(column_index);
                            
                            mCameraImagePath = path;
                            NearChatLog.i("onActivityResult INTENT_REQUEST_CODE_ALBUM "+ mCameraImagePath);
                            Bitmap bitmap = ImageUtils.getBitmapFromPath(path);
                            if (ImageUtils.bitmapIsLarge(bitmap)) {
                                ImageUtils.cropPhoto(this, this, path);
                            }
                            else {
                                if (path != null) {
                                    ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH
                                            + File.separator);
                                    sendMessage(path, CONTENT_TYPE.IMAGE);
                                    refreshAdapter();
                                }
                            }
                        }
                    }
                }
                break;

            case ImageUtils.INTENT_REQUEST_CODE_CAMERA:
            	NearChatLog.i("ChatActivity onActivityResult() 相机获取图片");
                if (resultCode == RESULT_OK) {
                    if (mCameraImagePath != null) {
                        mCameraImagePath = ImageUtils.savePhotoToSDCard(
                                ImageUtils.CompressionPhoto(mScreenWidth, mCameraImagePath, 2),
                                NearChatApplication.CAMERA_IMAGE_PATH, null);
                        ImageUtils.fliterPhoto(this, this, mCameraImagePath);
                    }
                }
                break;

            case ImageUtils.INTENT_REQUEST_CODE_CROP:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra("path");
                    mCameraImagePath = path;
                    if (path != null) {
                        ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH + File.separator);
                        sendMessage(path, CONTENT_TYPE.IMAGE);
                        refreshAdapter();
                    }
                }
                break;

            case ImageUtils.INTENT_REQUEST_CODE_FLITER:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra("path");
                    if (path != null) {
                        ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH + File.separator);
                        sendMessage(path, CONTENT_TYPE.IMAGE);
                        refreshAdapter();
                    }
                }
                break;

            case FILE_SELECT_CODE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = uri.getPath();
                    if (path != null) {
                        sendFilePath = path;
                        sendMessage(sendFilePath, CONTENT_TYPE.FILE);
                        refreshAdapter();
                    }
                }
            }
                break;
        }
    }

    public boolean isThisActivityMsg(MsgEntity msg) {
        String senderIMEI = msg.getSenderIMEI();
        if (mChatUser.getIMEI().equals(senderIMEI)) {
            mMessagesList.add(msg);
            return true;
        }
        return false;

    }

    public void processMessage(android.os.Message pMsg) {
        switch (pMsg.what) {
            case MSGProtocolConst.IPMSG_SENDMSG:
                if (isThisActivityMsg((MsgEntity) pMsg.obj)) {
                    handler.sendEmptyMessage(1);
                }
                break;

            case MSGProtocolConst.IPMSG_CONFIRM_IMAGE_DATA: { // 图片开始发送
                NearChatLog.i("接收方确认图片请求,发送的文件为" + mCameraImagePath);
                tcpClient = TcpClient.getInstance(ChatActivity.this);
                tcpClient.startSend();
                tcpClient.sendFile(mCameraImagePath, mChatUser.getIpaddress(),
                        MsgEntity.CONTENT_TYPE.IMAGE);
            }
                break;

            case MSGProtocolConst.IPMSG_CONFIRM_VOICE_DATA: { // 语音开始发送
                NearChatLog.i("接收方确认语音请求,发送的文件为" + mVoicePath);
                tcpClient = TcpClient.getInstance(ChatActivity.this);
                tcpClient.startSend();
                if (FileUtils.isFileExists(mVoicePath))
                    tcpClient.sendFile(mVoicePath, mChatUser.getIpaddress(),
                            MsgEntity.CONTENT_TYPE.VOICE);
            }
                break;

            case MSGProtocolConst.IPMSG_CONFIRM_FILE_DATA: { // 文件开始发送
                NearChatLog.i("接收方确认文件请求,发送的文件为" + sendFilePath);
                tcpClient = TcpClient.getInstance(ChatActivity.this);
                tcpClient.startSend();
                if (FileUtils.isFileExists(sendFilePath))
                    tcpClient.sendFile(sendFilePath, mChatUser.getIpaddress(),
                            MsgEntity.CONTENT_TYPE.FILE);
            }
                break;

        }
        // End of switch
    }

    private void startRecord() {
        recordState = RECORD_ON;
        RECORD_FILENAME = System.currentTimeMillis()
                + coffee1993.nearchat.util.TextUtils.getRandomNumStr(3);

        mAudioRecorder = new AudioRecorderUtils();
        mAudioRecorder.setVoicePath(VOICE_PATH, RECORD_FILENAME);
        mRecordThread = new Thread(recordThread);

        try {
            mAudioRecorder.start();
            mRecordThread.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    /*    showVoiceDialog(0);*/

    }

    private void stopRecord() {
        recordState = RECORD_OFF;
        try {
            mRecordThread.interrupt();
            mRecordThread = null;
            mAudioRecorder.stop();
        }
        catch (IOException e) {
            
            e.printStackTrace();
        }
        voiceValue = 0.0;
    }

    // 录音线程
    private Runnable recordThread = new Runnable() {

        @Override
        public void run() {
            recodeTime = 0.0f;
            while (recordState == RECORD_ON) {
                // 限制录音时长
                // if (recodeTime >= MAX_RECORD_TIME && MAX_RECORD_TIME != 0) {
                // imgHandle.sendEmptyMessage(0);
                // } else
                {
                    try {
                        Thread.sleep(200);
                        recodeTime += 0.2;
                        // 获取音量，更新dialog
                        if (!isMove) {
                            voiceValue = mAudioRecorder.getAmplitude();
                            handler.sendEmptyMessage(0);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    };

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0: // 更新录音音量
                    setDialogImage();
                    break;

                case 1:
                    refreshAdapter(); // 更新消息显示
                    break;

                default:
                    break;
            }

        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      
        MsgEntity msg = mMessagesList.get((int) id);
        switch (msg.getContentType()) {
            case IMAGE:
                Intent imgIntent = new Intent(mContext, ImageBrowserActivity.class);
                imgIntent
                        .putExtra(ImageBrowserActivity.IMAGE_TYPE, ImageBrowserActivity.TYPE_PHOTO);
                imgIntent.putExtra(ImageBrowserActivity.PATH, msg.getMsgContent());
                startActivity(imgIntent);
                overridePendingTransition(R.anim.zoom_enter, 0);
                break;

            case VOICE:
                // 播放录音
                final ImageView imgView = (ImageView) view
                        .findViewById(R.id.voice_message_iv_msgimage);
                if (!isPlay) {
                    mMediaPlayer = new MediaPlayer();
                    String filePath = msg.getMsgContent();
                    try {
                        mMediaPlayer.setDataSource(filePath);
                        mMediaPlayer.prepare();
                        imgView.setImageResource(R.drawable.voicerecord_stop);
                        isPlay = true;
                        mMediaPlayer.start();
                        // 设置播放结束时监听
                        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (isPlay) {
                                    imgView.setImageResource(R.drawable.voicerecord_right);
                                    isPlay = false;
                                    mMediaPlayer.stop();
                                    mMediaPlayer.release();
                                }
                            }
                        });
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        isPlay = false;
                    }
                    else {
                        isPlay = false;
                        mMediaPlayer.release();
                    }
                    imgView.setImageResource(R.drawable.voicerecord_right);
                }

                break;

            case FILE:
            	//TODO 有bug
                Intent fileIntent = new Intent();
                fileIntent.setType("*/*");
                fileIntent.setData(Uri.parse("file://"
                        + FileUtils.getPathByFullPath(msg.getMsgContent())));
                mContext.startActivity(fileIntent);
                break;

            default:
                break;

        }

    }

}
