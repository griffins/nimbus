package to.us.datagrip.accountability.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import to.us.datagrip.accountability.models.DayAccount;
import to.us.datagrip.accountability.models.Journal;

public class Misc {
    private Context mContext;

    public static boolean putFileContents(String file, byte[] contents) {
        return putFileContents(file, contents, false);
    }

    public static String title(String s) {
        if (s == null) {
            return s;
        }
        boolean uppercase = true;
        StringBuilder builder = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (uppercase) {
                builder.append(Character.toUpperCase(c));
                uppercase = false;
            } else {
                builder.append(Character.toLowerCase(c));
            }
            if (c == ' ' || c == '\n' || c == '\t') {
                uppercase = true;
            }
        }
        return builder.toString();
    }

    public static boolean putFileContents(String file, byte[] contents, boolean append) {

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(contents);
            outputStream.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getFileContents(String path) {
        String contents = "";
        File formFile = new File(path);
        if (!formFile.exists()) {
            return null;
        }
        try {
            Scanner fileReader = new Scanner(formFile);
            while (fileReader.hasNext()) {
                contents += fileReader.nextLine();
            }
            return contents;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Misc(Context context) {
        this.mContext = context;
    }


    public boolean checkNetwork() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static String getHash(String string) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");

            m.reset();
            m.update(string.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void execute(PayloadRunnable worker, PayloadRunnable after) {
        new BackGroundTask(after).execute(worker);
    }

    public static List<DayAccount> getDayAcounts(List<Journal> journals) {
        List<DayAccount> accounts = new ArrayList<DayAccount>();
        for (Journal journal : journals) {
            boolean added = false;
            for (DayAccount account : accounts) {
                if (DateUtils.isSameDay(account.getAccountDate(), journal.getCreatedAt())) {
                    account.addAccount(journal);
                    added = true;
                    break;
                }
            }
            if (!added) {
                DayAccount dayAccount = new DayAccount();
                dayAccount.addAccount(journal);
                accounts.add(dayAccount);
            }
        }
        return accounts;
    }

    public class BackGroundTask extends AsyncTask<PayloadRunnable, Object, Object> {
        private final PayloadRunnable afterRunable;

        public BackGroundTask(PayloadRunnable after) {
            this.afterRunable = after;
        }

        @Override
        protected Object doInBackground(PayloadRunnable... runnables) {
            int x = 0;
            Object[] result = new Object[runnables.length];
            for (PayloadRunnable runnable : runnables) {
                result[x] = runnable.run(null);
                x++;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object o_) {
            super.onPostExecute(o_);
            Object objects[] = (Object[]) o_;
            for (Object o : objects) {
                afterRunable.run(o);
            }
        }
    }
}