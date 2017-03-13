package to.us.datagrip.accountability.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import to.us.datagrip.accountability.R;

public class Settings {
    private static Settings instance;
    private static Boolean isTablet = true;
    public static int statusBarHeight = 0;
    private Context mContext;
    private boolean setup = false;
    private static float density = 1;
    private static Point displaySize = new Point();
    private HashMap<String, Object> volatilePrefs;

    private Settings(Context context) {
        setContext(context);
        init();
    }

    public static boolean isLocked() {
        if (Settings.getInstance() == null) {
        }
        if (Settings.getInstance(null).pinSet()) {
            double lockTime = Settings.getInstance(null).getDouble(Constants.lockTime, 6000);
            double timeout = new Date().getTime() - Settings.getInstance(null).getVolatileDouble(Constants.lockTime, 0);
            if (timeout > lockTime) {
                return true;
            }
        }
        return false;
    }

    public boolean pinSet() {
        return !Settings.getInstance(null).getString(Constants.passCode, "").equalsIgnoreCase("");
    }

    public void setPin(String pin) {
        try {
            String hash = PasswordStorage.createHash(pin.toCharArray());
            Settings.getInstance().putString(Constants.passCode, hash);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "A cryptographic error occurred setting pin", Toast.LENGTH_LONG).show();
        }
    }

    public void removePin() {
        Settings.getInstance().putString(Constants.passCode, "");
    }

    public long getDouble(String key, int _default) {
        return getPrefs().getLong(key, _default);
    }

    public void putLong(String key, long value) {
        SharedPreferences prefs = getPrefs();
        prefs.edit().putLong(key, value).apply();
    }

    public double getLong(String key, long _default) {
        SharedPreferences prefs = getPrefs();
        return prefs.getLong(key, _default);
    }

    private void init() {
        if (getString("name", "").equalsIgnoreCase("")) {
            setSetup(false);
        } else {
            setSetup(true);
        }

        density = getContext().getResources().getDisplayMetrics().density;

        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            Display display = manager.getDefaultDisplay();
            if (display != null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                display.getMetrics(displayMetrics);
                if (android.os.Build.VERSION.SDK_INT < 13) {
                    displaySize.set(display.getWidth(), display.getHeight());
                } else {
                    display.getSize(displaySize);
                }
            }
        }

        if (isTablet == null) {
            isTablet = getContext().getResources().getBoolean(R.bool.isTablet);
        }
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
        }
    }

    public static boolean isTablet() {
        return isTablet;
    }

    public Uri getProfileUri() {
        String path = this.mContext.getFilesDir().getAbsolutePath().concat("/profile.jpg");
        return Uri.parse("file://" + path);
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isSetup() {
        return setup;
    }

    private void setSetup(boolean setup) {
        this.setup = setup;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public static Settings getInstance(Context context) {
        if (instance == null) {
            if (context != null) {
                instance = new Settings(context);
            }
        }
        return instance;
    }

    public static Settings getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Settings has not initialized from a valid context");
        }
        return instance;
    }

    public void putString(String key, String value) {
        SharedPreferences prefs = getPrefs();
        prefs.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences prefs = getPrefs();
        prefs.edit().putBoolean(key, value).apply();
    }

    public void putVolatileBoolean(String key, boolean value) {
        Map<String, Object> prefs = (Map<String, Object>) getVolatilePrefs();
        prefs.put(key, (Boolean) value);
    }

    public void putVolatileLong(String key, long value) {
        Map<String, Object> prefs = (Map<String, Object>) getVolatilePrefs();
        prefs.put(key, (Long) value);
    }

    public double getVolatileDouble(String key, long _default) {
        Map<String, Object> prefs = (Map<String, Object>) getVolatilePrefs();
        double value = _default;
        try {
            value = Double.parseDouble(String.valueOf(prefs.get(key)));
        } catch (Exception ignored) {

        }
        return value;
    }

    public static Point getDisplaySize() {
        return displaySize;
    }

    public static void setDisplaySize(Point displaySize) {
        Settings.displaySize = displaySize;
    }

    public String getString(String key, String _default) {
        SharedPreferences prefs = getPrefs();
        return prefs.getString(key, _default);
    }

    public boolean getBoolean(String key, boolean _default) {
        SharedPreferences prefs = getPrefs();
        return prefs.getBoolean(key, _default);
    }

    private SharedPreferences getPrefs() {
        return getContext().getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
    }

    public static int dp(int value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static float getDensity() {
        return density;
    }


    public boolean checkPasscode(String password) {
        try {
            String hash = getString(Constants.passCode, "\n");
            boolean status = PasswordStorage.verifyPassword(password, hash);
            return status;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void invalidate() {
        instance = null;
    }

    public static int getViewInset(View view) {
        if (view == null || Build.VERSION.SDK_INT < 21 || view.getHeight() == displaySize.y || view.getHeight() == displaySize.y - statusBarHeight) {
            return 0;
        }
        try {
            Field mAttachInfoField = View.class.getDeclaredField("mAttachInfo");
            mAttachInfoField.setAccessible(true);
            Object mAttachInfo = mAttachInfoField.get(view);
            if (mAttachInfo != null) {
                Field mStableInsetsField = mAttachInfo.getClass().getDeclaredField("mStableInsets");
                mStableInsetsField.setAccessible(true);
                Rect insets = (Rect) mStableInsetsField.get(mAttachInfo);
                return insets.bottom;
            }
        } catch (Exception e) {
            Log.e("Home", e.toString());
        }
        return 0;
    }

    private Map<String, ?> getVolatilePrefs() {
        if (volatilePrefs == null) {
            volatilePrefs = new HashMap<>();
        }
        return volatilePrefs;
    }

    public boolean getVolatileBoolean(String key, boolean _default) {
        Map<String, Object> prefs = (Map<String, Object>) getVolatilePrefs();
        boolean value = _default;
        try {
            value = Boolean.parseBoolean(String.valueOf(prefs.get(key)));
        } catch (Exception ignored) {

        }
        return value;
    }

    public static String hash(String text) {
        return Misc.getHash(text);
    }

    public interface Constants {
        String lockTime = "lockTime";
        String passCode = "passCode";
        String passCodeUnlocked = "passCodeUnlocked";
        String lockTimeString = "lockTimeString";
        String mpesa = "mpesa";
    }
}