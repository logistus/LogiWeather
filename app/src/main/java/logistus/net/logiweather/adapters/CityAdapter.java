package logistus.net.logiweather.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import logistus.net.logiweather.ApplicationContextProvider;
import logistus.net.logiweather.MainActivity;
import logistus.net.logiweather.MapsActivity;
import logistus.net.logiweather.R;
import logistus.net.logiweather.dialog_fragments.ErrorDialog;
import logistus.net.logiweather.models.City;
import logistus.net.logiweather.models.Forecast;
import logistus.net.logiweather.services.ClientService;
import logistus.net.logiweather.services.DataChangeListener;
import logistus.net.logiweather.services.DatabaseService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityHolder> {
    private static final String TAG = CityAdapter.class.getSimpleName();
    private Context mContext;
    private List<City> mCities;
    private static DisplayMetrics displayMetrics = ApplicationContextProvider.getmContext().getResources().getDisplayMetrics();
    private static float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
    private static int numColumns = (int) (dpWidth / 100);

    public CityAdapter(Context context, List<City> cities) {
        mContext = context;
        mCities = cities;
    }

    @Override
    public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.city_list_item, parent, false);
        return new CityHolder(view);
    }

    public static void addCity(final String latitude, final String longitude, final Activity activity) {
        final DatabaseService databaseService = new DatabaseService(ApplicationContextProvider.getmContext());
        String geolookupURL = ClientService.WEATHER_API + ClientService.API_KEY +
                "/geolookup/q/" + latitude + "," + longitude + ".json";
        Call geoCall = ClientService.connectApi(geolookupURL);
        geoCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String queryURL;
                    String jsonData = response.body().string();
                    try {
                        JSONObject resultObject = new JSONObject(jsonData);
                        JSONObject location = resultObject.getJSONObject("location");
                        queryURL = location.getString("l");
                        String condUrl = ClientService.WEATHER_API + ClientService.API_KEY +
                                "/forecast10day";
                        for (String language : ClientService.supportedLanguages) {
                            if (language.equals(MainActivity.systemLanguage)) {
                                condUrl += "/lang:" + language;
                                break;
                            }
                        }
                        condUrl += queryURL + ".json";

                        City newCity = new City();
                        newCity.setLastUpdate(String.valueOf(System.currentTimeMillis() / 1000L));
                        newCity.setCityText(MapsActivity.cityName);
                        newCity.setTimezone(location.getString("tz_long"));
                        newCity.setQueryUrl(queryURL);
                        if (!databaseService.checkCity(MapsActivity.cityName)) {
                            final long last_city_id = databaseService.addCity(newCity);
                            // get 10 days forecasts
                            Call conditionsCall = ClientService.connectApi(condUrl);
                            conditionsCall.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String jsonData = response.body().string();
                                        try {
                                            JSONObject resultObject = new JSONObject(jsonData);
                                            JSONObject forecast = resultObject.getJSONObject("forecast");
                                            JSONObject simple_forecast = forecast.getJSONObject("simpleforecast");

                                            JSONArray forecast_days = simple_forecast.getJSONArray("forecastday");

                                            for (int i = 0; i < numColumns; i++) {
                                                JSONObject day = forecast_days.getJSONObject(i);
                                                Forecast new_forecast = new Forecast();
                                                new_forecast.setCity_id(last_city_id);
                                                new_forecast.setDay(
                                                        day.getJSONObject("date").getString("monthname_short") + " " +
                                                                day.getJSONObject("date").getInt("day"));
                                                new_forecast.setCondition(day.getString("conditions"));
                                                new_forecast.setLow_temp_c(day.getJSONObject("low").getString("celsius"));
                                                new_forecast.setLow_temp_f(day.getJSONObject("low").getString("fahrenheit"));
                                                new_forecast.setHigh_temp_c(day.getJSONObject("high").getString("celsius"));
                                                new_forecast.setHigh_temp_f(day.getJSONObject("high").getString("fahrenheit"));
                                                new_forecast.setIcon_url(day.getString("icon_url"));
                                                databaseService.addForecast(new_forecast);
                                            }

                                            // go back to main activity
                                            Intent intent = new Intent(ApplicationContextProvider.getmContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            ApplicationContextProvider.getmContext().startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else {
                            ErrorDialog dialogFragment = new ErrorDialog();
                            Bundle bundle = new Bundle();
                            bundle.putString("message", ApplicationContextProvider.getmContext().getString(R.string.already_have));
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "select_city_error_dialog");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteAllCities(DataChangeListener listener) {
        DatabaseService databaseService = new DatabaseService(ApplicationContextProvider.getmContext());
        databaseService.deleteAllCities();
        databaseService.deleteAllForecasts();
        listener.onDataChanged(true);
    }

    public static void updateCity(final City city, final DataChangeListener listener) {
        String condUrl = ClientService.WEATHER_API + ClientService.API_KEY +
                "/forecast10day";
        for (String language : ClientService.supportedLanguages) {
            if (language.equals(MainActivity.systemLanguage)) {
                condUrl += "/lang:" + language;
                break;
            }
        }
        condUrl += city.getQueryUrl() + ".json";
        final Call updateCall = ClientService.connectApi(condUrl);
        final DatabaseService databaseService = new DatabaseService(ApplicationContextProvider.getmContext());
        updateCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject resultObject = new JSONObject(jsonData);

                        JSONObject forecast = resultObject.getJSONObject("forecast");
                        JSONObject simple_forecast = forecast.getJSONObject("simpleforecast");

                        databaseService.deleteForecasts(city.getCityId());
                        databaseService.updateCity(city.getCityId());

                        JSONArray forecast_days = simple_forecast.getJSONArray("forecastday");

                        for (int i = 0; i < numColumns; i++) {
                            JSONObject day = forecast_days.getJSONObject(i);
                            Forecast new_forecast = new Forecast();
                            new_forecast.setCity_id(city.getCityId());
                            new_forecast.setDay(
                                    day.getJSONObject("date").getString("monthname_short") + " " +
                                            day.getJSONObject("date").getInt("day"));
                            new_forecast.setCondition(day.getString("conditions"));
                            new_forecast.setLow_temp_c(day.getJSONObject("low").getString("celsius"));
                            new_forecast.setLow_temp_f(day.getJSONObject("low").getString("fahrenheit"));
                            new_forecast.setHigh_temp_c(day.getJSONObject("high").getString("celsius"));
                            new_forecast.setHigh_temp_f(day.getJSONObject("high").getString("fahrenheit"));
                            new_forecast.setIcon_url(day.getString("icon_url"));
                            databaseService.addForecast(new_forecast);
                        }
                        listener.onDataChanged(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(final CityHolder holder, int position) {
        DatabaseService databaseService = new DatabaseService(mContext);
        City city = mCities.get(holder.getAdapterPosition());
        List<Forecast> forecasts = databaseService.getAllForecasts(city.getCityId());
        holder.mCityFullText.setText(city.getCityText());
        holder.mLastUpdate.setText(city.getFormattedTime(city.getLastUpdate(), "updated"));
        holder.mLocalTime.setText(city.getFormattedTime(String.valueOf(System.currentTimeMillis() / 1000L), "local"));
        final float scale = mContext.getResources().getDisplayMetrics().density;
        holder.mDailyConditions.removeAllViews();
        for (Forecast forecast : forecasts) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int margins_condition = (int) (scale * 8);
            params.setMargins(margins_condition, margins_condition, margins_condition, margins_condition);
            params.weight = 1;
            LinearLayout condition = new LinearLayout(mContext);
            condition.setLayoutParams(params);
            condition.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams dayValueParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView dayValue = new TextView(mContext);
            dayValue.setLayoutParams(dayValueParams);
            dayValue.setGravity(Gravity.CENTER);
            dayValue.setText(forecast.getDay());
            dayValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            condition.addView(dayValue);

            ImageView condition_icon = new ImageView(mContext);
            int icon_dimen = (int) (scale * 50); // 50dp
            int icon_margins = (int) (scale * 10); // 10dp
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(icon_dimen, icon_dimen);
            imageParams.gravity = Gravity.CENTER_HORIZONTAL;
            imageParams.setMargins(icon_margins, icon_margins, icon_margins, icon_margins);
            condition_icon.setLayoutParams(imageParams);
            Picasso.with(mContext).load(forecast.getIcon_url()).into(condition_icon);
            condition.addView(condition_icon);

            LinearLayout temps = new LinearLayout(mContext);
            temps.setLayoutParams(params);
            temps.setGravity(Gravity.CENTER);
            temps.setOrientation(LinearLayout.HORIZONTAL);
            TextView min_temp = new TextView(mContext);
            min_temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            TextView slash = new TextView(mContext);
            slash.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            slash.setText(R.string.slash);
            TextView max_temp = new TextView(mContext);
            max_temp.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            TextView temp_type = new TextView(mContext);
            temp_type.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            temp_type.setText(MainActivity.TEMP_TYPE);
            ImageView degree = new ImageView(mContext);
            degree.setImageResource(R.drawable.degree_symbol);
            int degree_dimen = (int) (10 * scale);
            LinearLayout.LayoutParams degreeParams = new LinearLayout.LayoutParams(degree_dimen, degree_dimen);
            degree.setLayoutParams(degreeParams);
            if ("C".equals(MainActivity.TEMP_TYPE)) {
                min_temp.setText(forecast.getLow_temp_c());
                max_temp.setText(forecast.getHigh_temp_c());
            } else {
                min_temp.setText(forecast.getLow_temp_f());
                max_temp.setText(forecast.getHigh_temp_f());
            }
            temps.addView(min_temp);
            temps.addView(slash);
            temps.addView(max_temp);
            temps.addView(degree);
            temps.addView(temp_type);
            condition.addView(temps);

            TextView conditionDay = new TextView(mContext);
            conditionDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            conditionDay.setText(forecast.getCondition());
            conditionDay.setLayoutParams(dayValueParams);
            conditionDay.setGravity(Gravity.CENTER);
            conditionDay.setLines(2);
            conditionDay.setSingleLine(false);
            condition.addView(conditionDay);

            holder.mDailyConditions.addView(condition);
        }
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    public class CityHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.city_full_text)
        TextView mCityFullText;
        @BindView(R.id.last_update_value)
        TextView mLastUpdate;
        @BindView(R.id.local_time_value)
        TextView mLocalTime;
        @BindView(R.id.cardview)
        public RelativeLayout mCardView;
        @BindView(R.id.daily_conditions)
        LinearLayout mDailyConditions;

        private CityHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
