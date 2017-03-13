package to.us.datagrip.accountability;

import to.us.datagrip.accountability.utils.Settings;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.getInstance(getApplicationContext());
    }
}
