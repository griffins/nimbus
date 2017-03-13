/*
 * This is the source code of Telegram for Android v. 3.x.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package to.us.datagrip.accountability.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.utils.Settings;

public class PasscodeView extends FrameLayout {
    private Activity activity;

    private Drawable backgroundDrawable;
    private FrameLayout numbersFrameLayout;
    private ArrayList<TextView> numberTextViews;
    private ArrayList<TextView> lettersTextViews;
    private ArrayList<FrameLayout> numberFrameLayouts;
    private FrameLayout passwordFrameLayout;
    private ImageView eraseView;
    private EditText passwordEditText;
    private AnimatingTextView passwordEditText2;
    private FrameLayout backgroundFrameLayout;
    private TextView passcodeTextView;
    private ImageView checkImage;
    private int keyboardHeight = 0;

    private Rect rect = new Rect();

    private PasscodeViewDelegate delegate;
    private Window window;

    public PasscodeView(Context context) {
        super(context);
        init(context);
    }

    public PasscodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PasscodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(GONE);
        backgroundDrawable = getBackground();
        backgroundFrameLayout = new FrameLayout(context);
        addView(backgroundFrameLayout);
        LayoutParams layoutParams = (LayoutParams) backgroundFrameLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        backgroundFrameLayout.setLayoutParams(layoutParams);

        passwordFrameLayout = new FrameLayout(context);
        addView(passwordFrameLayout);
        layoutParams = (LayoutParams) passwordFrameLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        passwordFrameLayout.setLayoutParams(layoutParams);

        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(R.drawable.passcode_logo);
        passwordFrameLayout.addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        if (Settings.getDensity() < 1) {
            layoutParams.width = Settings.dp(30);
            layoutParams.height = Settings.dp(30);
        } else {
            layoutParams.width = Settings.dp(40);
            layoutParams.height = Settings.dp(40);
        }

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layoutParams.bottomMargin = Settings.dp(100);
        imageView.setLayoutParams(layoutParams);

        passcodeTextView = new TextView(context);
        passcodeTextView.setTextColor(0xffffffff);
        passcodeTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passcodeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        passwordFrameLayout.addView(passcodeTextView);
        layoutParams = (LayoutParams) passcodeTextView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.bottomMargin = Settings.dp(62);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        passcodeTextView.setLayoutParams(layoutParams);

        passwordEditText2 = new AnimatingTextView(context);
        passwordFrameLayout.addView(passwordEditText2);
        layoutParams = (LayoutParams) passwordEditText2.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = Settings.dp(70);
        layoutParams.rightMargin = Settings.dp(70);
        layoutParams.bottomMargin = Settings.dp(6);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        passwordEditText2.setLayoutParams(layoutParams);

        passwordEditText = new EditText(context);
        passwordEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
        passwordEditText.setTextColor(0xffffffff);
        passwordEditText.setMaxLines(1);
        passwordEditText.setLines(1);
        passwordEditText.setGravity(Gravity.CENTER_HORIZONTAL);
        passwordEditText.setSingleLine(true);
        passwordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordEditText.setBackgroundDrawable(null);
        passwordFrameLayout.addView(passwordEditText);
        layoutParams = (LayoutParams) passwordEditText.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = Settings.dp(70);
        layoutParams.rightMargin = Settings.dp(70);
        layoutParams.bottomMargin = Settings.dp(6);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        passwordEditText.setLayoutParams(layoutParams);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    processDone();
                    return true;
                }
                return false;
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordEditText.length() == 4) {
                    processDone();
                }
            }
        });
        if (Build.VERSION.SDK_INT < 11) {
            passwordEditText.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.clear();
                }
            });
        } else {
            passwordEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }
            });
        }

        checkImage = new ImageView(context);
        checkImage.setImageResource(R.drawable.passcode_check);
        checkImage.setScaleType(ImageView.ScaleType.CENTER);
        checkImage.setBackgroundResource(R.drawable.bar_selector_lock);
        passwordFrameLayout.addView(checkImage);
        layoutParams = (LayoutParams) checkImage.getLayoutParams();
        layoutParams.width = Settings.dp(60);
        layoutParams.height = Settings.dp(60);
        layoutParams.bottomMargin = Settings.dp(4);
        layoutParams.rightMargin = Settings.dp(10);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        checkImage.setLayoutParams(layoutParams);
        checkImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                processDone();
            }
        });

        FrameLayout lineFrameLayout = new FrameLayout(context);
        lineFrameLayout.setBackgroundColor(0x26ffffff);
        passwordFrameLayout.addView(lineFrameLayout);
        layoutParams = (LayoutParams) lineFrameLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = Settings.dp(1);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        layoutParams.leftMargin = Settings.dp(20);
        layoutParams.rightMargin = Settings.dp(20);
        lineFrameLayout.setLayoutParams(layoutParams);

        numbersFrameLayout = new FrameLayout(context);
        addView(numbersFrameLayout);
        layoutParams = (LayoutParams) numbersFrameLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        numbersFrameLayout.setLayoutParams(layoutParams);

        lettersTextViews = new ArrayList<>(10);
        numberTextViews = new ArrayList<>(10);
        numberFrameLayouts = new ArrayList<>(10);
        for (int a = 0; a < 10; a++) {
            TextView textView = new TextView(context);
            textView.setTextColor(0xffffffff);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            textView.setGravity(Gravity.CENTER);
            textView.setText(String.format(Locale.US, "%d", a));
            numbersFrameLayout.addView(textView);
            layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.width = Settings.dp(50);
            layoutParams.height = Settings.dp(50);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            textView.setLayoutParams(layoutParams);
            numberTextViews.add(textView);

            textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textView.setTextColor(0x7fffffff);
            textView.setGravity(Gravity.CENTER);
            numbersFrameLayout.addView(textView);
            layoutParams = (LayoutParams) textView.getLayoutParams();
            layoutParams.width = Settings.dp(50);
            layoutParams.height = Settings.dp(20);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            textView.setLayoutParams(layoutParams);
            switch (a) {
                case 0:
                    textView.setText("+");
                    break;
                case 2:
                    textView.setText("ABC");
                    break;
                case 3:
                    textView.setText("DEF");
                    break;
                case 4:
                    textView.setText("GHI");
                    break;
                case 5:
                    textView.setText("JKL");
                    break;
                case 6:
                    textView.setText("MNO");
                    break;
                case 7:
                    textView.setText("PQRS");
                    break;
                case 8:
                    textView.setText("TUV");
                    break;
                case 9:
                    textView.setText("WXYZ");
                    break;
                default:
                    break;
            }
            lettersTextViews.add(textView);
        }
        eraseView = new ImageView(context);
        eraseView.setScaleType(ImageView.ScaleType.CENTER);
        eraseView.setImageResource(R.drawable.passcode_delete);
        numbersFrameLayout.addView(eraseView);
        layoutParams = (LayoutParams) eraseView.getLayoutParams();
        layoutParams.width = Settings.dp(50);
        layoutParams.height = Settings.dp(50);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        eraseView.setLayoutParams(layoutParams);
        for (int a = 0; a < 11; a++) {
            FrameLayout frameLayout = new FrameLayout(context);
            frameLayout.setBackgroundResource(R.drawable.bar_selector_lock);
            frameLayout.setTag(a);
            if (a == 10) {
                frameLayout.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        passwordEditText.setText("");
                        passwordEditText2.eraseAllCharacters(true);
                        return true;
                    }
                });
            }
            frameLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tag = (Integer) v.getTag();
                    switch (tag) {
                        case 0:
                            passwordEditText2.appendCharacter("0");
                            break;
                        case 1:
                            passwordEditText2.appendCharacter("1");
                            break;
                        case 2:
                            passwordEditText2.appendCharacter("2");
                            break;
                        case 3:
                            passwordEditText2.appendCharacter("3");
                            break;
                        case 4:
                            passwordEditText2.appendCharacter("4");
                            break;
                        case 5:
                            passwordEditText2.appendCharacter("5");
                            break;
                        case 6:
                            passwordEditText2.appendCharacter("6");
                            break;
                        case 7:
                            passwordEditText2.appendCharacter("7");
                            break;
                        case 8:
                            passwordEditText2.appendCharacter("8");
                            break;
                        case 9:
                            passwordEditText2.appendCharacter("9");
                            break;
                        case 10:
                            passwordEditText2.eraseLastCharacter();
                            break;
                    }
                    if (passwordEditText2.length() == 4) {
                        processDone();
                    }
                }
            });
            numberFrameLayouts.add(frameLayout);
        }
        for (int a = 10; a >= 0; a--) {
            FrameLayout frameLayout = numberFrameLayouts.get(a);
            numbersFrameLayout.addView(frameLayout);
            layoutParams = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams.width = Settings.dp(100);
            layoutParams.height = Settings.dp(100);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            frameLayout.setLayoutParams(layoutParams);
        }

    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setBackgroundAsWindowDrawable(Window window) {
        this.window = window;
    }


    public interface PasscodeViewDelegate {
        void passcodeAccepted();
    }


    private class AnimatingTextView extends FrameLayout {

        private ArrayList<TextView> characterTextViews;
        private ArrayList<TextView> dotTextViews;
        private StringBuilder stringBuilder;
        private String DOT = "\u2022";
        private Runnable dotRunnable;
        private AnimatorSet currentAnimation;

        public AnimatingTextView(Context context) {
            super(context);
            characterTextViews = new ArrayList<>(4);
            dotTextViews = new ArrayList<>(4);
            stringBuilder = new StringBuilder(4);

            for (int a = 0; a < 4; a++) {
                TextView textView = new TextView(context);
                textView.setTextColor(0xffffffff);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                textView.setGravity(Gravity.CENTER);
                if (Build.VERSION.SDK_INT > 10) {
                    textView.setAlpha(0);
                    textView.setPivotX(Settings.dp(25));
                    textView.setPivotY(Settings.dp(25));
                }
                addView(textView);
                LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.width = Settings.dp(50);
                layoutParams.height = Settings.dp(50);
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                textView.setLayoutParams(layoutParams);
                characterTextViews.add(textView);

                textView = new TextView(context);
                textView.setTextColor(0xffffffff);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                textView.setGravity(Gravity.CENTER);
                textView.setText(DOT);

                if (Build.VERSION.SDK_INT > 10) {
                    textView.setAlpha(0);
                    textView.setPivotX(Settings.dp(25));
                    textView.setPivotY(Settings.dp(25));
                }
                addView(textView);
                layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.width = Settings.dp(50);
                layoutParams.height = Settings.dp(50);
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                textView.setLayoutParams(layoutParams);
                dotTextViews.add(textView);
            }
        }

        private int getXForTextView(int pos) {
            return (getMeasuredWidth() - stringBuilder.length() * Settings.dp(30)) / 2 + pos * Settings.dp(30) - Settings.dp(10);
        }

        public void appendCharacter(String c) {
            if (stringBuilder.length() == 4) {
                return;
            }
            try {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception e) {
                Log.e("tmessages", e.toString());
            }


            ArrayList<Animator> animators = new ArrayList<>();
            final int newPos = stringBuilder.length();
            stringBuilder.append(c);

            TextView textView = characterTextViews.get(newPos);
            textView.setText(c);
            if (Build.VERSION.SDK_INT > 10) {
                textView.setTranslationX(getXForTextView(newPos));
                animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0, 1));
                animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0, 1));
                animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0, 1));
                animators.add(ObjectAnimator.ofFloat(textView, "translationY", Settings.dp(20), 0));
                textView = dotTextViews.get(newPos);
                textView.setTranslationX(getXForTextView(newPos));
                textView.setAlpha(0);
                animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0, 1));
                animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0, 1));
                animators.add(ObjectAnimator.ofFloat(textView, "translationY", Settings.dp(20), 0));

                for (int a = newPos + 1; a < 4; a++) {
                    textView = characterTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                    }

                    textView = dotTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                    }
                }

                if (dotRunnable != null) {
                    cancelRunOnUIThread(dotRunnable);
                }
                dotRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (dotRunnable != this) {
                            return;
                        }
                        ArrayList<Animator> animators = new ArrayList<>();
                        if (Build.VERSION.SDK_INT > 10) {

                            TextView textView = characterTextViews.get(newPos);
                            animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                            animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                            animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                            textView = dotTextViews.get(newPos);
                            animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 1));
                            animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 1));
                            animators.add(ObjectAnimator.ofFloat(textView, "alpha", 1));

                            if (currentAnimation != null) {
                                currentAnimation.cancel();
                            }

                            currentAnimation = new AnimatorSet();
                            currentAnimation.setDuration(150);

                            currentAnimation.playTogether(animators);
                            currentAnimation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (currentAnimation != null && currentAnimation.equals(animation)) {
                                        currentAnimation = null;
                                    }
                                }
                            });

                            currentAnimation.start();
                        }
                    }
                };
                runOnUIThread(dotRunnable);

                for (int a = 0; a < newPos; a++) {
                    textView = characterTextViews.get(a);
                    animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                    animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                    animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                    animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                    animators.add(ObjectAnimator.ofFloat(textView, "translationY", 0));
                    textView = dotTextViews.get(a);
                    animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                    animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 1));
                    animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 1));
                    animators.add(ObjectAnimator.ofFloat(textView, "alpha", 1));
                    animators.add(ObjectAnimator.ofFloat(textView, "translationY", 0));
                }

                if (currentAnimation != null) {
                    currentAnimation.cancel();
                }
                currentAnimation = new AnimatorSet();
                currentAnimation.setDuration(150);
                currentAnimation.playTogether(animators);

                currentAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (currentAnimation != null && currentAnimation.equals(animation)) {
                            currentAnimation = null;
                        }
                    }
                });

                currentAnimation.start();

            }
        }

        public String getString() {
            return stringBuilder.toString();
        }

        public int length() {
            return stringBuilder.length();
        }

        public void eraseLastCharacter() {
            if (stringBuilder.length() == 0) {
                return;
            }
            try {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception e) {
                Log.e("tmessages", e.toString());
            }

            ArrayList<Animator> animators = new ArrayList<>();
            int deletingPos = stringBuilder.length() - 1;
            if (deletingPos != 0) {
                stringBuilder.deleteCharAt(deletingPos);
            }
            if (Build.VERSION.SDK_INT > 10) {

                for (int a = deletingPos; a < 4; a++) {
                    TextView textView = characterTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "translationY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                    }

                    textView = dotTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "translationY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                    }
                }

                if (deletingPos == 0) {
                    stringBuilder.deleteCharAt(deletingPos);
                }

                for (int a = 0; a < deletingPos; a++) {
                    TextView textView = characterTextViews.get(a);
                    animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                    textView = dotTextViews.get(a);
                    animators.add(ObjectAnimator.ofFloat(textView, "translationX", getXForTextView(a)));
                }

                if (dotRunnable != null) {
                    cancelRunOnUIThread(dotRunnable);
                    dotRunnable = null;
                }

                if (currentAnimation != null) {
                    currentAnimation.cancel();
                }
                currentAnimation = new AnimatorSet();
                currentAnimation.setDuration(150);
                currentAnimation.playTogether(animators);
                currentAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (currentAnimation != null && currentAnimation.equals(animation)) {
                            currentAnimation = null;
                        }
                    }
                });

                currentAnimation.start();
            }
        }

        private void eraseAllCharacters(final boolean animated) {
            if (stringBuilder.length() == 0) {
                return;
            }
            if (dotRunnable != null) {
                runOnUIThread(dotRunnable);
                dotRunnable = null;
            }
            if (currentAnimation != null) {
                if (Build.VERSION.SDK_INT > 10) {
                    currentAnimation.cancel();
                }
                currentAnimation = null;
            }
            stringBuilder.delete(0, stringBuilder.length());
            if (animated && Build.VERSION.SDK_INT > 10) {
                ArrayList<Animator> animators = new ArrayList<>();

                for (int a = 0; a < 4; a++) {
                    TextView textView = characterTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                    }

                    textView = dotTextViews.get(a);
                    if (textView.getAlpha() != 0) {
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleX", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "scaleY", 0));
                        animators.add(ObjectAnimator.ofFloat(textView, "alpha", 0));
                    }
                }

                currentAnimation = new AnimatorSet();
                currentAnimation.setDuration(150);
                currentAnimation.playTogether(animators);
                currentAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (currentAnimation != null && currentAnimation.equals(animation)) {
                            currentAnimation = null;
                        }
                    }
                });
                currentAnimation.start();
            } else {
                for (int a = 0; a < 4; a++) {
                    TextView textView = characterTextViews.get(a);
                    textView.setAlpha(0);
                    textView = dotTextViews.get(a);
                    textView.setAlpha(0);
                }
            }
        }


        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

            for (int a = 0; a < 4; a++) {
                if (a < stringBuilder.length() && Build.VERSION.SDK_INT > 10) {
                    TextView textView = characterTextViews.get(a);
                    textView.setAlpha(0);
                    textView.setScaleX(1);
                    textView.setScaleY(1);
                    textView.setTranslationY(0);
                    textView.setTranslationX(getXForTextView(a));

                    textView = dotTextViews.get(a);
                    textView.setAlpha(1);
                    textView.setScaleX(1);
                    textView.setScaleY(1);
                    textView.setTranslationY(0);
                    textView.setTranslationX(getXForTextView(a));
                } else {
                    TextView textView = characterTextViews.get(a);
                    if (Build.VERSION.SDK_INT > 10) {
                        textView.setAlpha(0);
                    }
                    textView = dotTextViews.get(a);
                    if (Build.VERSION.SDK_INT > 10) {
                        textView.setAlpha(0);
                    }
                }
            }
            super.onLayout(changed, left, top, right, bottom);
        }

    }

    private void cancelRunOnUIThread(Runnable dotRunnable) {
    }

    private void runOnUIThread(Runnable dotRunnable) {
        if (activity != null) {
            activity.runOnUiThread(dotRunnable);
        }
    }

    public void setDelegate(PasscodeViewDelegate delegate) {
        this.delegate = delegate;
    }

    private void processDone() {
        String password = passwordEditText2.getString();

        if (password.length() == 0) {
            onPasscodeError();
            return;
        }
        if (!Settings.getInstance(getContext()).checkPasscode(password)) {
            passwordEditText.setText("");
            passwordEditText2.eraseAllCharacters(true);
            onPasscodeError();
            return;
        }
        passwordEditText.clearFocus();
        Settings.hideKeyboard(passwordEditText);

        if (Build.VERSION.SDK_INT >= 14) {
        } else {
            setVisibility(View.GONE);
        }

        setOnTouchListener(null);
        if (delegate != null) {
            delegate.passcodeAccepted();
        }
    }

    private void shakeTextView(final float x, final int num) {
        if (num == 6) {
            passcodeTextView.clearAnimation();
            return;
        }
        if (Build.VERSION.SDK_INT > 10) {
            AnimatorSet animatorSetProxy = new AnimatorSet();
            animatorSetProxy.playTogether(ObjectAnimator.ofFloat(passcodeTextView, "translationX", Settings.dp((int) x)));
            animatorSetProxy.setDuration(50);
            animatorSetProxy.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shakeTextView(num == 5 ? 0 : -x, num + 1);
                }
            });
            animatorSetProxy.start();
        }
    }

    private void onPasscodeError() {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(200);
        }
        shakeTextView(2, 0);
    }


    public void onShow() {
        Activity parentActivity = (Activity) getContext();
        if (parentActivity != null) {
            View currentFocus = parentActivity.getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
                Settings.hideKeyboard(((Activity) getContext()).getCurrentFocus());
            }
        }
        setActivity(parentActivity);
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 14) {
            setAlpha(1.0f);
            setTranslationY(0);
            this.clearAnimation();
        }

        if (backgroundDrawable != null) {
            backgroundFrameLayout.setBackgroundColor(0x8f000000);
        } else {
            backgroundFrameLayout.setBackgroundColor(0xff517c9e);
        }

        passcodeTextView.setText(R.string.EnterYourPasscode);

        numbersFrameLayout.setVisibility(VISIBLE);
        passwordEditText.setVisibility(GONE);
        passwordEditText2.setVisibility(VISIBLE);
        checkImage.setVisibility(GONE);
        setVisibility(VISIBLE);
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordEditText.setText("");
        passwordEditText2.eraseAllCharacters(false);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = Settings.getDisplaySize().y - (Build.VERSION.SDK_INT >= 21 ? 0 : Settings.statusBarHeight);

        LayoutParams layoutParams;

        if (!Settings.isTablet() || getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams = (LayoutParams) passwordFrameLayout.getLayoutParams();
            layoutParams.width = width / 2;
            layoutParams.height = Settings.dp(140);
            layoutParams.topMargin = (height - Settings.dp(140)) / 2;
            passwordFrameLayout.setLayoutParams(layoutParams);

            layoutParams = (LayoutParams) numbersFrameLayout.getLayoutParams();
            layoutParams.height = height - Settings.dp(16);
            layoutParams.leftMargin = width / 2;
            layoutParams.topMargin = 0;
            layoutParams.width = width / 2;
            numbersFrameLayout.setLayoutParams(layoutParams);
        } else {
            int top = 0;
            int left = 0;
            if (Settings.isTablet()) {
                if (width > Settings.dp(498)) {
                    left = (width - Settings.dp(498)) / 2;
                    width = Settings.dp(498);
                }
                if (height > Settings.dp(528)) {
                    top = (height - Settings.dp(528)) / 2;
                    height = Settings.dp(528);
                }
            }
            layoutParams = (LayoutParams) passwordFrameLayout.getLayoutParams();
            layoutParams.height = height / 3;
            layoutParams.width = width;
            layoutParams.topMargin = top;
            layoutParams.leftMargin = left;
            passwordFrameLayout.setTag(top);
            passwordFrameLayout.setLayoutParams(layoutParams);

            layoutParams = (LayoutParams) numbersFrameLayout.getLayoutParams();
            layoutParams.height = height / 3 * 2;
            layoutParams.leftMargin = left;
            layoutParams.topMargin = height - layoutParams.height + top;
            layoutParams.width = width;
            numbersFrameLayout.setLayoutParams(layoutParams);
        }

        int sizeBetweenNumbersX = (layoutParams.width - Settings.dp(50) * 3) / 4;
        int sizeBetweenNumbersY = (layoutParams.height - Settings.dp(50) * 4) / 5;

        for (int a = 0; a < 11; a++) {
            LayoutParams layoutParams1;
            int num;
            if (a == 0) {
                num = 10;
            } else if (a == 10) {
                num = 11;
            } else {
                num = a - 1;
            }
            int row = num / 3;
            int col = num % 3;
            int top;
            if (a < 10) {
                TextView textView = numberTextViews.get(a);
                TextView textView1 = lettersTextViews.get(a);
                layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams1 = (LayoutParams) textView1.getLayoutParams();
                top = layoutParams1.topMargin = layoutParams.topMargin = sizeBetweenNumbersY + (sizeBetweenNumbersY + Settings.dp(50)) * row;
                layoutParams1.leftMargin = layoutParams.leftMargin = sizeBetweenNumbersX + (sizeBetweenNumbersX + Settings.dp(50)) * col;
                layoutParams1.topMargin += Settings.dp(40);
                textView.setLayoutParams(layoutParams);
                textView1.setLayoutParams(layoutParams1);
            } else {
                layoutParams = (LayoutParams) eraseView.getLayoutParams();
                top = layoutParams.topMargin = sizeBetweenNumbersY + (sizeBetweenNumbersY + Settings.dp(50)) * row + Settings.dp(8);
                layoutParams.leftMargin = sizeBetweenNumbersX + (sizeBetweenNumbersX + Settings.dp(50)) * col;
                top -= Settings.dp(8);
                eraseView.setLayoutParams(layoutParams);
            }

            FrameLayout frameLayout = numberFrameLayouts.get(a);
            layoutParams1 = (LayoutParams) frameLayout.getLayoutParams();
            layoutParams1.topMargin = top - Settings.dp(17);
            layoutParams1.leftMargin = layoutParams.leftMargin - Settings.dp(25);
            frameLayout.setLayoutParams(layoutParams1);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View rootView = getRootView();
        int usableViewHeight = rootView.getHeight() - Settings.statusBarHeight - Settings.getViewInset(rootView);
        getWindowVisibleDisplayFrame(rect);
        keyboardHeight = usableViewHeight - (rect.bottom - rect.top);

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }
        if (backgroundDrawable != null) {
            if (window != null) {
                window.setBackgroundDrawable(backgroundDrawable);
            } else {
                if (backgroundDrawable instanceof ColorDrawable) {
                    backgroundDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                    backgroundDrawable.draw(canvas);
                } else {
                    float scaleX = (float) getMeasuredWidth() / (float) backgroundDrawable.getIntrinsicWidth();
                    float scaleY = (float) (getMeasuredHeight() + keyboardHeight) / (float) backgroundDrawable.getIntrinsicHeight();
                    float scale = scaleX < scaleY ? scaleY : scaleX;
                    int width = (int) Math.ceil(backgroundDrawable.getIntrinsicWidth() * scale);
                    int height = (int) Math.ceil(backgroundDrawable.getIntrinsicHeight() * scale);
                    int x = (getMeasuredWidth() - width) / 2;
                    int y = (getMeasuredHeight() - height + keyboardHeight) / 2;
                    backgroundDrawable.setBounds(x, y, x + width, y + height);
                    backgroundDrawable.draw(canvas);
                }
            }
        } else {
            super.onDraw(canvas);
        }
    }
}
