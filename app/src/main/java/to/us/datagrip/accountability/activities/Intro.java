package to.us.datagrip.accountability.activities;

import android.content.Intent;
import android.os.Bundle;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;
import com.stephentuso.welcome.WelcomeHelper;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.utils.Settings;

public class Intro extends WelcomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected WelcomeConfiguration configuration() {
        WelcomeConfiguration config = new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorAccent)
                .page(new BasicPage(R.drawable.logo_white,
                        getString(R.string.app_name), "Accounting you can count on.")
                )
                .page(new BasicPage(R.drawable.mobile,
                        "Mobile Transactions",
                        "Analytics on your mobile transactions").background(R.color.colorDanger)
                )
                .page(new TitlePage(R.drawable.calculator,
                        "Things you hate, let us calculate.").background(R.color.colorDanger)
                )
                .swipeToDismiss(true)
                .useCustomDoneButton(true).build();
        return config;
    }

    @Override
    protected void completeWelcomeScreen() {
        Settings.getInstance().putBoolean("welcome_screen", true);
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
