package to.us.datagrip.accountability.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.MenuAdapter;
import to.us.datagrip.accountability.models.MenuItem;
import to.us.datagrip.accountability.utils.PayloadRunnable;
import to.us.datagrip.accountability.utils.Settings;

public class Profile extends BaseActivity {
    private static final String TAG = "Profile";

    public Profile() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        final RecyclerView listView = (RecyclerView) findViewById(R.id.profileScrollView);
        // Set the onScrollListener
        final MenuAdapter adapter = new MenuAdapter();
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Preferences");
        refreshProfile();

        MenuItem menuItem = new MenuItem();
        menuItem.setPrimaryText("");
        menuItem.setType(MenuItem.TYPES.SECTION_TITLE);
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_TITLE);
        menuItem.setPrimaryText("Personal details");
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_DOUBLE_LINE);
        menuItem.setPrimaryText(Settings.getInstance(this).getString("name", "Enter name"));
        menuItem.setSecondaryText("Display name");
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(final Object e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Enter display name");
                builder.setNegativeButton("Dismiss", null);
                View view = getLayoutInflater().inflate(R.layout.input_text, null);
                final TextView text = (TextView) view.findViewById(R.id.input_text);
                text.setHint("Name");
                builder.setView(view);
                builder.setPositiveButton("Use", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = text.getText().toString().trim();
                        ((MenuItem) e).setPrimaryText(name);
                        Settings.getInstance(getApplicationContext()).putString("name", name);
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.create().show();
                return null;
            }
        });
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.DIVIDER_THIN);
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_DOUBLE_LINE);
        menuItem.setPrimaryText(Settings.getInstance(this).getString("phone", "Enter phone"));
        menuItem.setSecondaryText("Phone");
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(final Object e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Enter phone number");
                builder.setNegativeButton("Dismiss", null);
                View view = getLayoutInflater().inflate(R.layout.input_text, null);
                final TextView text = (TextView) view.findViewById(R.id.input_text);
                text.setHint("Phone");
                builder.setView(view);
                builder.setPositiveButton("Use", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = text.getText().toString().trim();
                        ((MenuItem) e).setPrimaryText(name);
                        Settings.getInstance(getApplicationContext()).putString("phone", name);
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.create().show();
                return null;
            }
        });

        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.DIVIDER_THICK);
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_TITLE);
        menuItem.setPrimaryText("Settings");
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_ONE_LINE);
        menuItem.setPrimaryText("Language");
        menuItem.setSecondaryText("English");
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_CHECK);
        menuItem.setPrimaryText("Enable M-Pesa");
        menuItem.setSecondaryText("");
        menuItem.setValue(Settings.getInstance().getBoolean(Settings.Constants.mpesa, false));
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                MenuItem menu = (MenuItem) result;
                Settings.getInstance().putBoolean(Settings.Constants.mpesa, menu.getBoolean());
                adapter.notifyDataSetChanged();
                return null;
            }
        });
        adapter.add(menuItem);


        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.DIVIDER_THICK);
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_TITLE);
        menuItem.setPrimaryText("Privacy and Security");
        menuItem.setSecondaryText("");
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_ONE_LINE);
        menuItem.setPrimaryText("Passcode lock");
        menuItem.setSecondaryText("");
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                Intent intent = new Intent(Profile.this, Passcode.class);
                startActivity(intent);
                return null;
            }
        });
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.DIVIDER_THIN);
        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_TITLE);
        menuItem.setPrimaryText(String.format("About %s", getString(R.string.app_name)));
        menuItem.setSecondaryText("");
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                Intent intent = new Intent(Profile.this, About.class);
                startActivity(intent);
                return null;
            }
        });
        adapter.add(menuItem);
    }

    private void refreshProfile() {

    }
}