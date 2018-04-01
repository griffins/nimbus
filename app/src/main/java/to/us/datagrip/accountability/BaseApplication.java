package to.us.datagrip.accountability;

import to.us.datagrip.accountability.utils.Settings;

public class BaseApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.getInstance(getApplicationContext());
    }
}
