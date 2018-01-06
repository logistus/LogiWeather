package logistus.net.logiweather;

import android.app.Application;
import android.content.Context;

public class ApplicationContextProvider extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getmContext() {
        return mContext;
    }
}
