package logistus.net.logiweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.settings_temp_celcius) RadioButton tempCelcius;
    @BindView(R.id.settings_temp_fahrenheit) RadioButton tempFahrenheit;
    @BindView(R.id.time_choices) RadioGroup timeChoices;
    @BindView(R.id.settings_time_12) RadioButton time12;
    @BindView(R.id.settings_time_24) RadioButton time24;
    private SharedPreferences mSharedPreferences;
    private AdView mAdView;

    @OnClick(R.id.save_settings)
    public void saveSettings(View view) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (tempCelcius.isChecked()) {
            editor.putString("temp_type", "C");
        } else {
            editor.putString("temp_type", "F");
        }
        if (time12.isChecked()) {
            editor.putInt("time_type", 12);
        } else {
            editor.putInt("time_type", 24);
        }
        editor.apply();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mSharedPreferences = getSharedPreferences(MainActivity.PREFS_FILE, Context.MODE_PRIVATE);

        MobileAds.initialize(this, "ca-app-pub-1159392186617217~1943223016");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if ("C".equals(MainActivity.TEMP_TYPE)) {
            tempCelcius.setChecked(true);
            tempFahrenheit.setChecked(false);
        } else {
            tempCelcius.setChecked(false);
            tempFahrenheit.setChecked(true);
        }

        if (MainActivity.TIME_TYPE == 24) {
            timeChoices.check(R.id.settings_time_24);
            time24.setChecked(true);
            time12.setChecked(false);
        } else {
            time24.setChecked(false);
            time12.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_delete_all_cities).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_new_city) {
            finish();
            Intent intent = new Intent(SettingsActivity.this, MapsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}