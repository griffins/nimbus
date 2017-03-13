package to.us.datagrip.accountability.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import to.us.datagrip.accountability.utils.Settings;
import to.us.datagrip.accountability.utils.mpesa.Transaction;
import to.us.datagrip.accountability.utils.mpesa.TransactionParser;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String TAG = "Database";
    private String journalsTable = "CREATE TABLE journals (id INTEGER AUTO_INCREMENT PRIMARY KEY , amount INTEGER , type TEXT,description TEXT ,comment TEXT,tags TEXT  , created_at INTEGER)";
    private String mpesaTable = "CREATE TABLE mpesa (id INTEGER AUTO_INCREMENT PRIMARY KEY ,text TEXT , hash TEXT UNIQUE , status TEXT)";

    public DatabaseHelper(Context context) {
        super(context, "acc.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(journalsTable);
        sqLiteDatabase.execSQL(mpesaTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old, int new_) {
        int updateTo = old + 1;
        while (updateTo <= new_) {
            switch (updateTo) {
                case 2:
                    sqLiteDatabase.execSQL(mpesaTable);
                    break;
            }
            updateTo++;
        }
    }

    public void addJournal(Journal journal) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("amount", journal.getAmount());
        values.put("description", journal.getDescription());
        values.put("comment", journal.getComment());
        values.put("created_at", journal.getDateTime());

        String tags = "";
        for (String tag : journal.getTags()) {
            if (tags.length() == 0) {
                tags = tag;
            } else {
                tags += "," + tag;
            }
        }
        values.put("tags", tags);
        values.put("type", journal.getType() == Journal.EXPENDITURE ? "expenditure" : "income");

        db.insertOrThrow("journals", null, values);
        db.close();
    }


    public List<Journal> getJournals(int offset, int limit, boolean desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Journal> journals = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM journals ORDER BY created_at " + ((!desc) ? "ASC" : "DESC"), null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Journal journal = new Journal();
                journal.setAmount(cursor.getInt(cursor.getColumnIndex("amount")));
                journal.setComment(cursor.getString(cursor.getColumnIndex("comment")));
                journal.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                journal.setType(cursor.getString(cursor.getColumnIndex("type")).equalsIgnoreCase("income") ? Journal.INCOME : Journal.EXPENDITURE);
                journal.setComment(cursor.getString(cursor.getColumnIndex("comment")));
                journal.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndex("created_at"))));
                String _tags = cursor.getString(cursor.getColumnIndex("tags"));
                String[] tags = _tags.split(",");
                journal.setTags(Arrays.asList(tags));
                journals.add(journal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return journals;
    }

    private void dropAllTables(SQLiteDatabase database) {
        String tables[] = new String[]{"journal", "mpesa"};
        for (String table : tables) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
        }
    }

    public void addMPesa(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("text", transaction.getText());
        values.put("hash", Settings.hash(transaction.getText()));
        values.put("status", "unread");
        try {
            db.insertOrThrow("mpesa", null, values);
        } catch (Exception ignored) {

        }
        db.close();
    }

    public List<Transaction> getUnreadMpesa() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM mpesa WHERE status='unread' ORDER BY id DESC", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                try {
                    Transaction transaction = TransactionParser.from(cursor.getString(cursor.getColumnIndex("text")));
                    transactions.add(transaction);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactions;
    }

    public void markRead(Transaction transaction) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE mpesa SET status = 'imported' WHERE hash=? ", new String[]{Settings.hash(transaction.getText())});
        db.close();
    }
}