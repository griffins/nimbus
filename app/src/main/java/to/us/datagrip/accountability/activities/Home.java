package to.us.datagrip.accountability.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.CustomViewAbove;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.stephentuso.welcome.WelcomeHelper;

import java.util.Date;
import java.util.List;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.AccountsAdapter;
import to.us.datagrip.accountability.adapters.MenuAdapter;
import to.us.datagrip.accountability.models.DatabaseHelper;
import to.us.datagrip.accountability.models.DayAccount;
import to.us.datagrip.accountability.models.Journal;
import to.us.datagrip.accountability.models.MenuItem;
import to.us.datagrip.accountability.utils.Misc;
import to.us.datagrip.accountability.utils.PayloadRunnable;
import to.us.datagrip.accountability.utils.Settings;
import to.us.datagrip.accountability.views.SquareImageView;

public class Home extends BaseActivity {

    private static final String TAG = "Home";
    private DrawerArrowDrawable drawerArrowDrawable;
    private SlidingMenu menu;
    private AccountsAdapter adapter;
    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Settings.getInstance().getBoolean("welcome_screen", false)) {
            WelcomeHelper welcomeScreen = new WelcomeHelper(this, Intro.class);
            welcomeScreen.show(savedInstanceState);
            finish();
            return;
        } else if (!Settings.getInstance().isSetup()) {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_accountability);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.fab);
        assert addFab != null;
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, AddAccount.class);
                startActivity(intent);
            }
        });

        Resources resources = getResources();
        drawerArrowDrawable = new DrawerArrowDrawable(this);
        drawerArrowDrawable.setColor(resources.getColor(android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(drawerArrowDrawable);
        drawerArrowDrawable.setSpinEnabled(true);
        drawerArrowDrawable.setProgress(0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setMenu(R.layout.menu);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeEnabled(true);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        menu.setOnPageChangeListener(new CustomViewAbove.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float offset = (Math.abs(positionOffset));
                offset += (offset / 0.8175f) * (1 - 0.8175f);
                drawerArrowDrawable.setProgress(offset);
            }

            @Override
            public void onPageSelected(int position) {
//                drawerArrowDrawable.setProgress(0f);
            }
        });
        initActivity();
        initMenu(menu.getMenu());
    }

    private void initMenu(View menu) {

        TextView name = (TextView) menu.findViewById(R.id.name);
        name.setText(Settings.getInstance(this).getString("name", ""));
        SquareImageView img = (SquareImageView) menu.findViewById(R.id.profPic);
        try {
            img.setImageURI(Settings.getInstance(this).getProfileUri());
        } catch (Exception ignored) {

        }

        MenuAdapter menuAdapter = new MenuAdapter();
        RecyclerView menuList = (RecyclerView) findViewById(R.id.menuList);
        menuList.setAdapter(menuAdapter);
        adapter.clear();
        menuList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        MenuItem menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_MENU);
        menuItem.setIcon(R.drawable.ic_smart_phone);
        menuItem.setPrimaryText("M-Pesa");
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                Intent intent = new Intent("to.us.datagrip.Mpesa");
                startActivity(intent);
                return null;
            }
        });

        menuItem.setSecondaryText("");

        if (Settings.getInstance().getBoolean(Settings.Constants.mpesa, false)) {
            menuAdapter.add(menuItem);
        }
        menuItem = new MenuItem();
        menuItem.setIcon(R.drawable.ic_income);
        menuItem.setType(MenuItem.TYPES.SECTION_MENU);
        menuItem.setPrimaryText("Income");
        menuItem.setSecondaryText("");

        menuAdapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_MENU);
        menuItem.setIcon(R.drawable.ic_expense);
        menuItem.setPrimaryText("Expenditure");
        menuItem.setSecondaryText("");

        menuAdapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.DIVIDER_THIN);

        menuAdapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_MENU);
        menuItem.setIcon(R.drawable.ic_graph);
        menuItem.setPrimaryText("Analysis");
        menuItem.setSecondaryText("");

        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                Intent intent = new Intent("to.us.datagrip.Analysis");
                startActivity(intent);
                return null;
            }
        });

        menuAdapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_MENU);
        menuItem.setPrimaryText("Settings");
        menuItem.setSecondaryText("");
        menuItem.setIcon(R.drawable.ic_settings);
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);
                return null;
            }
        });
        menuAdapter.add(menuItem);

    }

    private void initActivity() {
        listView = (RecyclerView) findViewById(R.id.listView);
        assert listView != null;
        adapter = new AccountsAdapter();
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);
        // initSwipe();
    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void setDefaultSwipeDirs(int defaultSwipeDirs) {
                Log.d(TAG, String.format("Directions: %s", defaultSwipeDirs));
                super.setDefaultSwipeDirs(defaultSwipeDirs);
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Log.d(TAG, String.format("Direction: %s", direction));
                if (direction == ItemTouchHelper.UP) {
                    adapter.collapseItem(position);
                } else if (direction == ItemTouchHelper.DOWN) {
                    adapter.expandItem(position);
                }
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                Log.d(TAG, String.format("Dy:%s Dx:%s", dY, dX));
                Bitmap icon;
                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    Paint paint = new Paint();
                    if (dY > 0) {
                        paint.setColor(getResources().getColor(R.color.colorAccent));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        canvas.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismis_done);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        canvas.drawBitmap(icon, null, icon_dest, paint);
                    } else if (dY == 0) {
                        //do nothing
                    } else {
                        paint.setColor(getResources().getColor(R.color.colorDanger));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                        canvas.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_dismis_cancel);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                        canvas.drawBitmap(icon, null, icon_dest, paint);
                    }
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(listView);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (Settings.getInstance(this).isSetup()) {
            loadAccounts();
            initMenu(menu.getMenu());
        }
    }

    private void loadAccounts() {
        Misc player = new Misc(this);
        player.execute(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                List<Journal> journals = db.getJournals(0, 10, true);
                return Misc.getDayAcounts(journals);
            }
        }, new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                List<DayAccount> accounts = (List<DayAccount>) result;
                adapter.clear();
                if (accounts.size() > 0) {
                    for (DayAccount account : accounts) {
                        account.setAction(getAccountActions());
                        adapter.add(account);
                    }
                } else {
                    Log.d(TAG, "Nothing added");
                }
                return null;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (adapter.isSelectMode()) {
            adapter.setSelectMode(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Creating options menu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accountabilty, menu);
        if (!Settings.isLocked()) {
            menu.getItem(0).setIcon(R.drawable.lock_open);
        }

        if (!Settings.getInstance().pinSet()) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                menu.toggle(true);
                break;
            case R.id.action_lock:
                if (!Settings.isLocked()) {
                    Settings.getInstance().putVolatileLong(Settings.Constants.lockTime, 0);
                    item.setIcon(R.drawable.lock_close);
                } else {
                    Settings.getInstance().putVolatileLong(Settings.Constants.lockTime, new Date().getTime());
                    item.setIcon(R.drawable.lock_open);
                }
                Settings.isLocked();
                break;
        }
        return true;
    }

    private PayloadRunnable getAccountActions() {
        return new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                if (result != null) {

                }
                return null;
            }
        };
    }
}