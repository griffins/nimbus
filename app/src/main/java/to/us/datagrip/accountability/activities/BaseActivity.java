package to.us.datagrip.accountability.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Date;

import to.us.datagrip.accountability.utils.Settings;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PinChecker.CHECK_PASSCODE) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!Settings.isLocked()) {
            Settings.getInstance(null).putVolatileLong(Settings.Constants.lockTime, new Date().getTime());
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!Settings.isLocked()) {
            Settings.getInstance(null).putVolatileLong(Settings.Constants.lockTime, new Date().getTime() + 1000 * 60 * 60);
        } else {
            Intent intent = new Intent(this, PinChecker.class);
            startActivityForResult(intent, PinChecker.CHECK_PASSCODE);
        }
    }
}
