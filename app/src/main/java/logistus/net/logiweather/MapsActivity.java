package logistus.net.logiweather;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import logistus.net.logiweather.adapters.CityAdapter;
import logistus.net.logiweather.dialog_fragments.ErrorDialog;
import logistus.net.logiweather.services.NetworkService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private LatLng mLatLng;
    public static String cityName;
    private NetworkService mNetworkService;
    private AdView mAdView;

    @BindView(R.id.maps_progress_bar) ProgressBar mProgressBar;

    @OnClick(R.id.add_new_city)
    public void addCityToDb(View view) {
        if (!mNetworkService.isNetworkAvailable()) {
            getNoConnectionErrorDialog();
        } else if (mLatLng == null) {
            getCityNotSelectedErrorDialog();
        } else {
            String latitude = String.valueOf(mLatLng.latitude);
            String longitude = String.valueOf(mLatLng.longitude);
            CityAdapter.addCity(latitude, longitude, this);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        MobileAds.initialize(this, "ca-app-pub-1159392186617217~1943223016");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mNetworkService = new NetworkService(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get auto complete fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        // we want only cities
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mLatLng = place.getLatLng();
                cityName = getCityFullText(MapsActivity.this, mLatLng.latitude, mLatLng.longitude);
                Log.d(TAG, cityName);
                mMap.addMarker(new MarkerOptions().position(mLatLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 15));
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occured: " + status);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_new_city).setVisible(false);
        menu.findItem(R.id.action_delete_all_cities).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCityNotSelectedErrorDialog() {
        ErrorDialog dialogFragment = new ErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putString("message", getString(R.string.select_city_error));
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getFragmentManager(), "select_city_error_dialog");
    }

    private void getNoConnectionErrorDialog() {
        ErrorDialog dialogFragment = new ErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putString("message", getString(R.string.no_connection_error));
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getFragmentManager(), "no_connection_error_dialog");
    }

    public String getCityFullText(Context context, double latitude, double longitude) {
        String city = "", state = "", country = "";
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String finalText = "";
        if (!TextUtils.isEmpty(city)) finalText += city  + ", ";
        if (!TextUtils.isEmpty(state)) finalText += state  + ", ";
        finalText += country;
        return finalText;
    }
}
