package to.us.datagrip.accountability.activities;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.TagsAdapter;
import to.us.datagrip.accountability.models.Journal;
import to.us.datagrip.accountability.models.Tag;
import to.us.datagrip.accountability.utils.AutoSpanGridLayoutManager;

public class AddAccount extends BaseActivity {

    private static final String TAG = "AddAccount";
    private AlertDialog dialog;
    private CalendarPickerView dialogView;
    private Date dateCurrentDate;
    private int account_type = 0;
    private TagsAdapter tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setDateCurrentDate(null);
        dialogView = (CalendarPickerView) getLayoutInflater().inflate(R.layout.datepicker_dialog, null, false);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        final Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, -1);
        dialogView.init(lastYear.getTime(), nextYear.getTime());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        AppCompatSpinner spinner = (AppCompatSpinner) findViewById(R.id.account_type);
        List<String> list = new ArrayList<String>();
        list.add("Income");
        list.add("Expenses");
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                account_type = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        RecyclerView tags = (RecyclerView) findViewById(R.id.tagView);
        AutoSpanGridLayoutManager layoutManager = new AutoSpanGridLayoutManager(this, 8, 0);

        assert tags != null;
        tags.setLayoutManager(layoutManager);
        tagsAdapter = new TagsAdapter();
        tags.setAdapter(tagsAdapter);
        View done = findViewById(R.id.btn_done);
        ImageButton tag = (ImageButton) findViewById(R.id.add_tag);
        assert tag != null;
        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View inputView = LayoutInflater.from(AddAccount.this).inflate(R.layout.input_text, null);

                final EditText input = (EditText) inputView.findViewById(R.id.input_text);

                AlertDialog dialog = new AlertDialog.Builder(AddAccount.this).setNegativeButton("Dismiss", null).setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tagsAdapter.add(new Tag(input.getText().toString().trim().toLowerCase(), AddAccount.this));
                    }
                }).create();
                dialog.setView(inputView);
//                dialog.setIcon(R.drawable.tag);
                dialog.setTitle("Add Tag");
                dialog.show();
            }
        });
        assert done != null;
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
                finish();
            }
        });
        View cancel = findViewById(R.id.btn_cancel);
        assert cancel != null;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageButton calender = (ImageButton) findViewById(R.id.calender);
        assert calender != null;
        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog == null) {
                    dialog = new AlertDialog.Builder(AddAccount.this) //
                            .setTitle(R.string.date_picker_title)
                            .setView(dialogView)
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setPositiveButton("Use", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    setDateCurrentDate(dialogView.getSelectedDate());
                                    dialogInterface.dismiss();
                                }
                            })
                            .create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialogView.fixDialogDimens();
                        }
                    });

                }
                dialog.show();
            }
        });

    }

    private void save() {
        Journal account = new Journal();
        account.setType(account_type);
        account.setCreatedAt(dateCurrentDate);
        account.setAmount(getAmount());
        account.setComment(getComment());
        account.setDescription(getDescription());
        account.setTags(tagsAdapter.getTags());
        account.save(this);
    }

    public void setDateCurrentDate(Date dateCurrentDate) {
        if (dateCurrentDate == null) {
            dateCurrentDate = new Date();
        }
        this.dateCurrentDate = dateCurrentDate;
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, d MMMM");
        ((TextView) findViewById(R.id.text_date)).setText(formatter.format(dateCurrentDate));
    }

    public double getAmount() {
        EditText amount_ = (EditText) findViewById(R.id.amount);
        double amount = Double.parseDouble(amount_.getText().toString());
        return amount;
    }

    public String getDescription() {
        EditText amount_ = (EditText) findViewById(R.id.desc);
        return amount_.getText().toString();
    }

    public String getComment() {
        EditText amount_ = (EditText) findViewById(R.id.comment);
        return amount_.getText().toString();
    }

    public List<String> getTags() {
        EditText amount_ = (EditText) findViewById(R.id.comment);
        return tagsAdapter.getTags();
    }
}
