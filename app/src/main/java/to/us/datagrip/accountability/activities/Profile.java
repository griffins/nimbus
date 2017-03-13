package to.us.datagrip.accountability.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.MenuAdapter;
import to.us.datagrip.accountability.models.MenuItem;
import to.us.datagrip.accountability.utils.Crop;
import to.us.datagrip.accountability.utils.PayloadRunnable;
import to.us.datagrip.accountability.utils.Settings;

public class Profile extends BaseActivity {
    private static final String TAG = "Profile";
    // The height of your fully expanded header view (same than in the xml layout)
    private int headerHeight;
    // The height of your fully collapsed header view. Actually the Toolbar height (56dp)
    private int minHeaderHeight;

    // The left margin of the Toolbar title (according to specs, 72dp)
    private int toolbarTitleLeftMargin;
    private int minHeaderTranslation;

    // Header views
    private View headerView;
    private TextView headerTitle;
    private FloatingActionButton headerFab;
    private CircleImageView headerImage;
    private int scrollHeight = 0;

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
        headerHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        minHeaderHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
        toolbarTitleLeftMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_left_margin);
        minHeaderTranslation = -headerHeight +
                getResources().getDimensionPixelOffset(R.dimen.action_bar_height);

        headerView = findViewById(R.id.headerView);

        headerTitle = (TextView) headerView.findViewById(R.id.header_title);
        headerImage = (CircleImageView) headerView.findViewById(R.id.header_picture);
        headerFab = (FloatingActionButton) findViewById(R.id.fab);
        assert headerFab != null;
        headerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPhoto();
            }
        });

        // Set the onScrollListener
        final MenuAdapter adapter = new MenuAdapter();
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addOnScrollListener(new ProfileOnScrollListenr());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Profile and Prefs");
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

    private void requestPhoto() {
        Crop.pickImage(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = getProfileUri();
        Crop cropper;
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            cropper = new Crop(data.getData());
            cropper.asSquare().withMaxSize(500, 500).output(uri).start(this);
        } else if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                refreshProfile();
            } else {
                Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshProfile() {
        setHomeDrawableFromUri(getProfileUri());
    }


    public void setTitle(String title) {
        headerTitle.setText(title);
    }

    public void setHomeDrawableFromUri(Uri drawable) {
        headerImage.setImageDrawable(null);
        headerImage.setImageURI(drawable);
    }

    public class ProfileOnScrollListenr extends RecyclerView.OnScrollListener {
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            scrollHeight += dy;
            // Scroll ratio (0 <= ratio <= 1).
            // The ratio value is 0 when the header is completely expanded,
            // 1 when it is completely collapsed

            float offset = 1 - Math.max(
                    (float) (-minHeaderTranslation - scrollHeight) / -minHeaderTranslation, 0f);

            int translationY = (int) Math.max(minHeaderHeight, (headerHeight * (1 - offset)));
            Log.d(TAG, "Y translation" + translationY);
            Log.d(TAG, "offset:" + offset);
            ViewGroup.LayoutParams params = headerView.getLayoutParams();
            params.height = translationY;
            if (Build.VERSION.SDK_INT > 11) {
                headerTitle.setTranslationX(toolbarTitleLeftMargin * offset);
                headerImage.setTranslationX(toolbarTitleLeftMargin * offset);
                headerFab.setAlpha(1 - offset);
            }
        }
    }

    public Uri getProfileUri() {
        String path = this.getFilesDir().getAbsolutePath().concat("/profile.jpg");
        return Uri.parse("file://" + path);
    }
}