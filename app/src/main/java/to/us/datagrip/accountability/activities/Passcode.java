package to.us.datagrip.accountability.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.adapters.MenuAdapter;
import to.us.datagrip.accountability.models.MenuItem;
import to.us.datagrip.accountability.utils.PayloadRunnable;
import to.us.datagrip.accountability.utils.Settings;

public class Passcode extends BaseActivity {
    private int step = 0;
    private String pin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nextStep();
        if (Settings.getInstance().pinSet()) {
            Intent intent = new Intent(this, PinChecker.class);
            startActivityForResult(intent, PinChecker.CHECK_PASSCODE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        nextStep();
    }

    private void nextStep() {
        switch (step) {
            case 1:
                stepOne();
                break;
            case 2:
                stepTwo();
                break;
            default:
                stepZero();
                break;
        }
    }

    private void stepTwo() {
        setContentView(R.layout.activity_passcode);
        TextView hint = (TextView) findViewById(R.id.passcodeView);
        hint.setText(R.string.repeat_passcode);
        final EditText passcode = (EditText) findViewById(R.id.editTextPasscode);
        passcode.requestFocus();
        passcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 4) {
                    String pin2 = editable.toString();
                    if (pin2.contentEquals(pin)) {
                        Settings.getInstance().setPin(pin);
                        Settings.getInstance().putLong(Settings.Constants.lockTime, (long) Settings.getInstance().getLong(Settings.Constants.lockTime, 6000));
                        Settings.getInstance().putString(Settings.Constants.lockTimeString, Settings.getInstance().getString(Settings.Constants.lockTimeString, getString(R.string.default_6_seconds)));
                        Toast.makeText(Passcode.this, R.string.passcode_set_done, Toast.LENGTH_SHORT).show();
                        step = 0;
                        nextStep();
                    } else {
                        editable.clear();
                        AnimatorSet animatorSetProxy = new AnimatorSet();
                        animatorSetProxy.playTogether(ObjectAnimator.ofFloat(passcode, "translationX", Settings.dp(16)));
                        animatorSetProxy.setDuration(50);
                        animatorSetProxy.start();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(200);
                        }
                    }
                }
            }
        });
    }

    private void stepZero() {
        setContentView(R.layout.activity_passcode_menu);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.menuList);
        final MenuAdapter adapter = new MenuAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        MenuItem menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_CHECK);
        menuItem.setPrimaryText(getString(R.string.passcode_lock));
        menuItem.setSecondaryText("");
        menuItem.setValue(Settings.getInstance().pinSet());
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                MenuItem menu = (MenuItem) result;
                if (menu.getBoolean()) {
                    step = 1;
                } else {
                    step = 0;
                    Settings.getInstance().removePin();
                }
                nextStep();
                return null;
            }
        });

        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_ONE_LINE);
        menuItem.setPrimaryText(getString(R.string.change_passcode));
        menuItem.setSecondaryText("");
        menuItem.setValue(Settings.getInstance().pinSet());
        menuItem.setEnabled(Settings.getInstance().pinSet());
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                step = 1;
                nextStep();
                return null;
            }
        });

        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_DESCRIPTION);
        menuItem.setPrimaryText(getString(R.string.passcode_desc));
        menuItem.setSecondaryText("");

        adapter.add(menuItem);

        menuItem = new MenuItem();
        menuItem.setType(MenuItem.TYPES.SECTION_ONE_LINE);
        menuItem.setPrimaryText(getString(R.string.timeout));
        menuItem.setSecondaryText(Settings.getInstance().getString(Settings.Constants.lockTimeString, getString(R.string.default_6_seconds)));
        menuItem.setAction(new PayloadRunnable() {
            @Override
            public Object run(Object result) {
                final MenuItem menu = (MenuItem) result;
                AlertDialog.Builder builder = new AlertDialog.Builder(Passcode.this);
                builder.setTitle(R.string.select_duration);
                builder.setCancelable(false);
                View text = getLayoutInflater().inflate(R.layout.spinner, null);
                builder.setView(text);
                final Spinner timing = (Spinner) text.findViewById(R.id.duration);
                String list[] = {getString(R.string.default_6_seconds), getString(R.string.time_30_seconds),
                        getString(R.string.time_one_minute), getString(R.string.time_five_minutes), getString(R.string.time_10_minutes),
                        getString(R.string.time_30_minutes), getString(R.string.time_one_hour)};

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(Passcode.this,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timing.setAdapter(dataAdapter);
                timing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int list[] = {6000, 30000, 60000, 300000, 600000, 1800000, 3600000};
                        int currentTiming = list[i];

                        menu.setSecondaryText((String) adapterView.getItemAtPosition(i));
                        Settings.getInstance().putLong(Settings.Constants.lockTime, currentTiming);
                        Settings.getInstance().putString(Settings.Constants.lockTimeString, menu.getSecondaryText());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
                return null;
            }
        });
        adapter.add(menuItem);

    }

    @Override
    public void onBackPressed() {
        if (step != 0) {
            step--;
            nextStep();
        } else {
            super.onBackPressed();
        }
    }

    private void stepOne() {
        setContentView(R.layout.activity_passcode);
        TextView hint = (TextView) findViewById(R.id.passcodeView);
        hint.setText(R.string.enter_new_passcode);
        EditText passcode = (EditText) findViewById(R.id.editTextPasscode);
        passcode.requestFocus();
        passcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 4) {
                    pin = editable.toString();
                    step++;
                    nextStep();
                }
            }
        });
    }

}
