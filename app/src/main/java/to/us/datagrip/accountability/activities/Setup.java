package to.us.datagrip.accountability.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import to.us.datagrip.accountability.R;

public class Setup extends AppCompatActivity {
    private ViewGroup bottomPages;
    private ViewPager viewPager;
    private String[] messages = new String[]{"Hi! Am Private books",
            "I will help you keep track of your finances easily.",
            "No worries! All your data is safe"};
    private int[] images = new int[]{R.mipmap.ic_launcher,
            R.drawable.planning, R.drawable.safe,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_setup);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        createPager();
    }

    private void createPager() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        bottomPages = (ViewGroup) findViewById(R.id.bottom_pages);
        viewPager.setAdapter(new IntroAdapter());
        viewPager.setPageMargin(0);
        viewPager.setOffscreenPageLimit(1);

    }

    private class IntroAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;

            if (position < 3) {
                view = View.inflate(container.getContext(), R.layout.daisy, null);
                TextView intro = (TextView) view.findViewById(R.id.intro_text);
                intro.setText(messages[position]);
                ImageView introImage = (ImageView) view.findViewById(R.id.intr_image);
                introImage.setImageResource(images[position]);
            } else {
                view = View.inflate(container.getContext(), R.layout.start, null);
                view.findViewById(R.id.letsgo).setOnClickListener(new View.OnClickListener() {
                    @Override

                    public void onClick(View view) {
                        Intent setup = new Intent(view.getContext(), Profile.class);
                        startActivity(setup);
                        finish();
                    }
                });
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            int count = bottomPages.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = bottomPages.getChildAt(a);
                if (a == position) {
                    child.setBackgroundColor(0xff2ca5e0);
                } else {
                    child.setBackgroundColor(0xffbbbbbb);
                }
            }

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }
}
