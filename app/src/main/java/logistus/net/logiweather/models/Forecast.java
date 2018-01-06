package logistus.net.logiweather.models;

public class Forecast {
    private long city_id;
    private String day;
    private String low_temp_c;
    private String low_temp_f;
    private String high_temp_c;
    private String high_temp_f;
    private String condition;
    private String icon_url;

    public long getCity_id() {
        return city_id;
    }

    public void setCity_id(long city_id) {
        this.city_id = city_id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getLow_temp_c() {
        return low_temp_c;
    }

    public void setLow_temp_c(String low_temp_c) {
        this.low_temp_c = low_temp_c;
    }

    public String getLow_temp_f() {
        return low_temp_f;
    }

    public void setLow_temp_f(String low_temp_f) {
        this.low_temp_f = low_temp_f;
    }

    public String getHigh_temp_c() {
        return high_temp_c;
    }

    public void setHigh_temp_c(String high_temp_c) {
        this.high_temp_c = high_temp_c;
    }

    public String getHigh_temp_f() {
        return high_temp_f;
    }

    public void setHigh_temp_f(String high_temp_f) {
        this.high_temp_f = high_temp_f;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }
}
