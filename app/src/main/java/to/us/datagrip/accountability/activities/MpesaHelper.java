package to.us.datagrip.accountability.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.MpesaAdapter;
import to.us.datagrip.accountability.models.DatabaseHelper;
import to.us.datagrip.accountability.models.Journal;
import to.us.datagrip.accountability.utils.Misc;
import to.us.datagrip.accountability.utils.PayloadRunnable;
import to.us.datagrip.accountability.utils.mpesa.Transaction;
import to.us.datagrip.accountability.utils.mpesa.TransactionParser;

public class MpesaHelper extends BaseActivity {

    private static final String TAG = "MPESA";
    private MpesaAdapter adapter;
    RecyclerView recyclerView;
    private Paint paint = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa_helper);
        init();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (adapter.isSelectMode()) {
            adapter.setSelectMode(false);
        } else {
            super.onBackPressed();
        }
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = adapter.getItem(position);
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());

                if (direction == ItemTouchHelper.LEFT) {
                    adapter.removeItem(position);
                } else {
                    Journal journal = new Journal();
                    journal.setAmount(transaction.getAmount());
                    journal.setCreatedAt(transaction.getTransactionDate());
                    journal.setDescription(String.format("%s %s %s", transaction.getTypeText(), transaction.getOtherName() != null ? transaction.getOtherName(true) : "", transaction.getOtherAccountNo() != null ? transaction.getOtherAccountNo() : ""));
                    journal.setType(transaction.getType() == 2 ? Journal.INCOME : Journal.EXPENDITURE);
                    journal.setComment(getString(R.string.auto_mpesa) + " REF-NO:" + transaction.getReferenceNo());
                    journal.addTag("mpesa");
                    journal.save(getApplicationContext());
                    Snackbar.make(recyclerView, transaction.getReferenceNo() + " imported", Snackbar.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                }
                db.markRead(transaction);
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if (dX > 0) {
                        paint.setColor(MpesaHelper.this.getResources().getColor(R.color.colorAccent));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        canvas.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismis_done);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        canvas.drawBitmap(icon, null, icon_dest, paint);
                    } else if (dX == 0) {
                        //do nothing
                    } else {
                        paint.setColor(MpesaHelper.this.getResources().getColor(R.color.colorDanger));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        canvas.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismis_cancel);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        canvas.drawBitmap(icon, null, icon_dest, paint);
                    }
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void init() {
        if (!hasSmsPermission()) {
            return;
        }
        adapter = new MpesaAdapter();
        recyclerView = (RecyclerView) findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(null));
        recyclerView.setAdapter(adapter);
        Misc utils = new Misc(this);
        utils.execute(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                return getTransactions();
            }
        }, new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                List<Transaction> transactions = (List<Transaction>) result;
                loadTransactions(transactions);
                return null;
            }
        });
        initSwipe();
    }

    private void loadTransactions(List<Transaction> transactions) {
        adapter.clear();
        if (transactions.size() == 0) {
            return;
        }
        for (Transaction transaction : transactions) {
            if (transaction.getType() >= 2 && transaction.getType() <= 7) {
                adapter.add(transaction);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Transaction> getTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();

        DatabaseHelper db = new DatabaseHelper(this);
        if (cursor.getCount() != 0) {
            do {
                String text = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                try {
                    if (address.contentEquals("MPESA")) {
                        Transaction transaction = TransactionParser.from(text);
                        db.addMPesa(transaction);
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        transactions.addAll(db.getUnreadMpesa());
        return transactions;
    }

    private boolean hasSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            askPermission();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions Granted");
            init();
        } else {
            Log.d(TAG, "Permissions Denied");
            finish();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 4);
    }
}
