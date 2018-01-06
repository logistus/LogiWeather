package logistus.net.logiweather;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import logistus.net.logiweather.adapters.CityAdapter;
import logistus.net.logiweather.helpers.RecyclerItemTouchHepler;
import logistus.net.logiweather.models.City;
import logistus.net.logiweather.services.DataChangeListener;
import logistus.net.logiweather.services.DatabaseService;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements DataChangeListener, RecyclerItemTouchHepler.RecyclerItemTouchHelperListener {
    public static String TEMP_TYPE;
    public static int TIME_TYPE;
    public static final String PREFS_FILE = "logistus.net.logiweather.prefs";
    public static FragmentManager fragmentManager;
    public static String systemLanguage;
    private DatabaseService databaseService;
    private CityAdapter mCityAdapter;
    private List<City> addedCities;
    private int deleteIndex;
    private AdView mAdView;

    @BindView(R.id.no_city_alert)
    TextView noCityAlert;
    @BindView(R.id.city_list)
    RecyclerView cityList;
    @BindView(R.id.relativeLayout)
    RelativeLayout mRelativeLayout;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MobileAds.initialize(this, "ca-app-pub-1159392186617217~1943223016");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        fragmentManager = getFragmentManager();
        systemLanguage = Resources.getSystem().getConfiguration().locale.getLanguage().toUpperCase();
        databaseService = new DatabaseService(MainActivity.this);
        SharedPreferences mSharedPreferences = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        TEMP_TYPE = mSharedPreferences.getString("temp_type", "C");
        TIME_TYPE = mSharedPreferences.getInt("time_type", 12);

        addedCities = databaseService.getAllCities();
        mCityAdapter = new CityAdapter(MainActivity.this, addedCities);
        cityList.setAdapter(mCityAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        cityList.setLayoutManager(layoutManager);
        cityList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        updateUi();

        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHepler(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(cityList);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                for (City city : databaseService.getAllCities()) {
                    CityAdapter.updateCity(city, MainActivity.this);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all_cities) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_all_cities)
                    .setMessage(R.string.delete_cities_warn)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CityAdapter.deleteAllCities(MainActivity.this);
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            builder.create();
            builder.show();
        } else if (id == R.id.action_add_new_city) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onDataChanged(boolean success) {
        if (success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addedCities.clear();
                    mCityAdapter.notifyDataSetChanged();
                    addedCities.addAll(databaseService.getAllCities());
                    mCityAdapter.notifyDataSetChanged();
                    if (databaseService.getCityCount() == 0) {
                        noCityAlert.setVisibility(View.VISIBLE);
                    } else {
                        noCityAlert.setVisibility(View.INVISIBLE);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    public void updateUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (databaseService.getCityCount() == 0) {
                    noCityAlert.setVisibility(View.VISIBLE);
                } else {
                    noCityAlert.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CityAdapter.CityHolder) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.confirm_delete_city)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteIndex = viewHolder.getAdapterPosition();
                            int city_id = addedCities.get(deleteIndex).getCityId();
                            databaseService.deleteCity(city_id);
                            databaseService.deleteForecasts(city_id);
                            String city = addedCities.get(deleteIndex).getCityText();
                            addedCities.remove(deleteIndex);
                            mCityAdapter.notifyItemRemoved(deleteIndex);
                            mCityAdapter.notifyItemRangeChanged(0, addedCities.size());
                            updateUi();
                            Snackbar snackbar = Snackbar.make(mRelativeLayout, city + " " + getString(R.string.removed), Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCityAdapter.notifyDataSetChanged();
                        }
                    });
            builder.create();
            builder.show();
        }
    }
}
