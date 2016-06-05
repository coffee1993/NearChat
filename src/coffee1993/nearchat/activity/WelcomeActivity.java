package coffee1993.nearchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import coffee1993.nearchat.BaseActivity;
import coffee1993.nearchat.R;

public class WelcomeActivity extends BaseActivity implements OnClickListener {

    private Button mBtnLogin;
    private ImageView mLogo;
    private TextView mLogoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initViews();
        initEvents();
    }    
    @Override
    protected void initViews() {
    	mLogo = (ImageView)findViewById(R.id.logo);
    	mLogoTextView = (TextView)findViewById(R.id.logo_name);
        mActionBar = getActionBar();
        mActionBar.hide();
        mBtnLogin = (Button) findViewById(R.id.welcome_btn_login);     
        Animation logoAlphaAnimation = AnimationUtils.loadAnimation(mContext, R.anim.logo);
        mLogo.setAnimation(logoAlphaAnimation);
        logoAlphaAnimation.startNow();
    }

    @Override
    protected void initEvents() {
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.welcome_btn_login:
        	startActivity(LoginActivity.class);
            break;

        }
    }


}
