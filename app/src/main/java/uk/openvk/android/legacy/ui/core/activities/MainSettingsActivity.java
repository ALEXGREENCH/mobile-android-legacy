package uk.openvk.android.legacy.ui.core.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import uk.openvk.android.legacy.BuildConfig;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Ovk;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import uk.openvk.android.legacy.ui.core.fragments.app.MainSettingsFragment;
import uk.openvk.android.legacy.ui.view.layouts.ActionBarImitation;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

public class MainSettingsActivity extends FragmentActivity {
    private boolean isQuiting;
    private OvkApplication app;
    private Global global = new Global();
    public OvkAPIWrapper ovk_api;
    private SharedPreferences global_prefs;
    private SharedPreferences instance_prefs;
    private OvkAlertDialog about_instance_dlg;
    public Handler handler;
    private View about_instance_view;
    private Ovk ovk;
    private int danger_zone_multiple_tap;
    private String account_name;
    private MainSettingsFragment mainSettingsFragment;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isQuiting = false;
        global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        instance_prefs = getApplicationContext().getSharedPreferences("instance", 0);
        setContentView(R.layout.app_layout);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                account_name = "";
            } else {
                account_name = extras.getString("account_name");
            }
        } else {
            account_name = (String) savedInstanceState.getSerializable("account_name");
        }
        app = ((OvkApplication) getApplicationContext());
        ovk_api = new OvkAPIWrapper(this, global_prefs.getBoolean("useHTTPS", true));
        ovk_api.setServer(instance_prefs.getString("server", ""));
        ovk = new Ovk();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getActionBar().setDisplayShowHomeEnabled(true);
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setTitle(getResources().getString(R.string.menu_settings));
            } catch (Exception ex) {
                Log.e("OpenVK", "Cannot display home button.");
            }
        } else {
            final ActionBarImitation actionBarImitation = findViewById(R.id.actionbar_imitation);
            actionBarImitation.setHomeButtonVisibility(true);
            actionBarImitation.setTitle(getResources().getString(R.string.menu_settings));
            actionBarImitation.setOnBackClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Bundle data = message.getData();
                if(!BuildConfig.BUILD_TYPE.equals("release")) Log.d("OpenVK", String.format("Handling API message: %s", message.what));
                receiveState(message.what, data);
            }
        };
        installFragments();
    }

    private void installFragments() {
        mainSettingsFragment = new MainSettingsFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, mainSettingsFragment, "settings");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.show(mainSettingsFragment);
        ft.commit();
    }

    private void receiveState(int message, Bundle data) {
        try {
            if (message == HandlerMessages.OVK_VERSION) {
                ovk.parseVersion(data.getString("response"));
                mainSettingsFragment.setInstanceVersion(ovk);
            } else if(message == HandlerMessages.OVK_ABOUTINSTANCE) {
                ovk.parseAboutInstance(data.getString("response"));
                mainSettingsFragment.setAboutInstanceData(ovk);
            } else {
                mainSettingsFragment.setConnectionType(message);
            }
        } catch (Exception ex) {
            mainSettingsFragment.setConnectionType(message);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }
}