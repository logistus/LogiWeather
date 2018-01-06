package logistus.net.logiweather.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import logistus.net.logiweather.MainActivity;

public class City {
    private String mCityText;
    private String mLastUpdate;
    private String mTimezone;
    private String mQueryUrl;
    private int cityId;

    public City() {}

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public String getLastUpdate() {
        return mLastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    public String getQueryUrl() {
        return mQueryUrl;
    }

    public void setQueryUrl(String queryUrl) {
        mQueryUrl = queryUrl;
    }

    public String getCityText() {
        return mCityText;
    }

    public void setCityText(String cityText) {
        mCityText = cityText;
    }

    public String getFormattedTime(String unix_time, String type) {
        int epoch = Integer.parseInt(unix_time);
        SimpleDateFormat formatter;
        if (MainActivity.TIME_TYPE == 24)
            formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        else
            formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm aaa", Locale.getDefault());
        if ("updated".equals(type))
            formatter.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)));
        else {
            if ("Europe/Istanbul".equals(getTimezone()))
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            else
                formatter.setTimeZone(TimeZone.getTimeZone(getTimezone()));
        }
        return formatter.format(new Date(epoch * 1000L));
    }
}
