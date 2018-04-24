package currencyconverter.alvinc.com.currencyconverter.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import java.util.HashSet;

import java.util.Set;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RealmStorage {
    private static RealmStorage instance;
    private static final String INIT_NOT_CALLED = "Run init() before this method!";

    @Nullable
    private Realm realm;

    private RealmConfiguration config;

    public static RealmStorage getInstance() {
        if (instance == null) {
            instance = new RealmStorage();
        }
        return instance;
    }

    private RealmStorage() {
    }

    public void init(Context context) {
        Realm.init(context);
        config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
    }

    public void close() {
        if (realm == null) throw new RuntimeException(INIT_NOT_CALLED);
        realm.close();
    }

    @SuppressWarnings("unused")
    public void printData() {
        if (realm == null) throw new RuntimeException(INIT_NOT_CALLED);

        Log.v("Realm", "==========BEGIN DUMP===========");
        RealmResults<Currency> currencies = realm.where(Currency.class).findAll();
        for (Currency currency : currencies) {

            Log.v("Realm", "-----------currency:" + currency.currencySymbol);
            for (Rate rate : currency.rates) {
                Log.d("ALVTAG", "to:" + rate.currencySymbol + " @ " + rate.exchangeRate);
            }
        }
        Log.v("Realm", "==========END DUMP===========");
    }

    public void insertRate(final String baseCurrencySymbol,
                           final String targetCurrencySymbol,
                           final Float exchangeRate, final String date) {
        if (realm == null) throw new RuntimeException(INIT_NOT_CALLED);

        //create object out here- powerMockito cannot touch anon inner
        final RealmList<Rate> newRealmList = new RealmList<>();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {

                Currency currency = realm.where(Currency.class)
                        .equalTo("currencySymbol", baseCurrencySymbol).findFirst();
                if (currency == null) {
                    currency = realm.createObject(Currency.class);
                    currency.currencySymbol = baseCurrencySymbol;
                    currency.rates = newRealmList;
                }

                //currency is now ready to go
                Rate rate = currency.rates.where()
                        .equalTo("currencySymbol", targetCurrencySymbol).findFirst();

                if (rate == null) {
                    rate = realm.createObject(Rate.class);
                    currency.rates.add(rate);
                }

                rate.currencySymbol = targetCurrencySymbol;
                rate.parent = currency;
                rate.date = date;
                rate.exchangeRate = exchangeRate;
            }
        });
    }

    //SYNCHRONOUS CALL - but we won't have very many currencies, this should be safe
    public Set<String> getCurrenciesSet() {
        if (realm == null) throw new RuntimeException(INIT_NOT_CALLED);
        RealmResults<Currency> currencyResults = realm.where(Currency.class).findAll();

        Set<String> result = new HashSet<>(currencyResults.size());

        // I can't guarantee that one currency would have every other conversion attached
        // If that was the case, just taking the first currency and its attached conversions 
        // would suffice
        for (Currency currency : currencyResults) {
            result.add(currency.currencySymbol);

            for (Rate rate : currency.rates) {
                result.add(rate.currencySymbol);
            }
        }
        return result;
    }

    public void clearData(Context applicationContext) {
        close();
        if (config != null) {
            Realm.deleteRealm(config);
        }
        init(applicationContext);
    }

    public void getRate(final String baseCurrency, final String targetCurrency, final GetRateListener listener) {
        if (realm == null) throw new RuntimeException();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Currency currency = realm.where(Currency.class)
                        .equalTo("currencySymbol", baseCurrency).findFirst();
                if (currency != null) {
                    Rate rate = currency.rates.where()
                            .equalTo("currencySymbol", targetCurrency).findFirst();
                    if (rate != null) {
                        listener.onRateRetrieved(rate);
                        return;
                    }
                }
                listener.onRateNotAvailable();
            }
        });
    }

    public interface GetRateListener {
        void onRateRetrieved(Rate rate);

        void onRateNotAvailable();
    }
}
