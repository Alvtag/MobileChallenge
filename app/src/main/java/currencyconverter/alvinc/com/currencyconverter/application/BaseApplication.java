package currencyconverter.alvinc.com.currencyconverter.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
    }
    public static Context getContext() {
        return appContext;
    }
}
