package to.us.datagrip.accountability.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import java.util.Date;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.utils.Settings;
import to.us.datagrip.accountability.views.PasscodeView;

public class PinChecker extends AppCompatActivity {
    static final String AFTER_ACTION = "to.us.datagrip.PinChecker.AFTER";
    public static final String SET_PASS_CODE_UNLOCKED = "SET_PASS_CODE_UNLOCKED";
    public static final int CHECK_PASSCODE = 4;
    private PasscodeView passcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.invalidate();
        Settings.getInstance(this);
        setContentView(R.layout.activity_pin_checker);
        passcodeView = (PasscodeView) findViewById(R.id.passcode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent, getTheme()));
        }
        passcodeView.setDelegate(new PasscodeView.PasscodeViewDelegate() {
            @Override
            public void passcodeAccepted() {
                if (getIntent().getBooleanExtra(SET_PASS_CODE_UNLOCKED, false)) {
                    Settings.getInstance().putVolatileBoolean(Settings.Constants.passCodeUnlocked, true);
                }
                Settings.getInstance(null).putVolatileLong(Settings.Constants.lockTime, new Date().getTime());
                if (getIntent().hasExtra(AFTER_ACTION)) {
                    Intent intent = new Intent(getIntent().getStringExtra(AFTER_ACTION));
                    startActivity(intent);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        passcodeView.setBackgroundAsWindowDrawable(getWindow());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        passcodeView.onShow();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }
}
