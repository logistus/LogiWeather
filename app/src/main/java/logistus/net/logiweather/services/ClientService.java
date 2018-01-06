package logistus.net.logiweather.services;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ClientService {
    public static final String WEATHER_API = "http://api.wunderground.com/api/";
    public static final String API_KEY = "YOUR_KEY";
    public static final String[] supportedLanguages = {"AF", "AL", "AR", "HY", "AZ", "EU", "BY", "BU",
            "LI", "MY", "CA", "CN", "TW", "CR", "CZ", "DK", "DV", "NL", "EN", "EO", "ET", "FA", "FI",
            "FR", "FC", "GZ", "DL", "KA", "GR", "GU", "HT", "IL", "HI", "HU", "IS", "IO", "ID", "IR",
            "IT", "JP", "JW", "KM", "KR", "KU", "LA", "LV", "LT", "ND", "MK", "MT", "GM", "MI", "MR",
            "MN", "NO", "OC", "PS", "GN", "PL", "BR", "PA", "RO", "RU", "SR", "SK", "SL", "SP", "SI",
            "SW", "CH", "TL", "TT", "TH", "TR", "TK", "UA", "UZ", "VU", "CY", "SN", "JI", "YI"
    };

    public static Call connectApi(String API_URL) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();
        return client.newCall(request);
    }
}
