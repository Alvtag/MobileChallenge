package currencyconverter.alvinc.com.currencyconverter.model;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RealmStorage {
    private static RealmStorage instance = new RealmStorage();
    private Realm realm;

    public static RealmStorage getInstance() {
        if (instance == null) {
            instance = new RealmStorage();
        }
        return instance;
    }

    private RealmStorage() {
        realm = Realm.getDefaultInstance();
    }

    public void insertRateAndInverse(final String baseCurrencySymbol,
                                     final String targetCurrencySymbol,
                                     final float exchangeRate, final String date) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Currency currency = realm.where(Currency.class)
                        .equalTo("currencySymbol", baseCurrencySymbol).findFirst();
                if (currency == null) {
                    currency = realm.createObject(Currency.class);
                    currency.currencySymbol = baseCurrencySymbol;
                    currency.rates = new RealmList<>();
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

    //SYNCHRONOUS CALL - possible ANR
    public List<String> getCurrenciesList() {
        RealmResults<Currency> realmResults = realm.where(Currency.class).findAll();
        List<String> result = new ArrayList<>(realmResults.size());

        for (Currency currency : realmResults) {
            result.add(currency.currencySymbol);
        }
        return result;
    }

    public void clearData() {
        RealmConfiguration config = Realm.getDefaultConfiguration();
        if (config != null) {
            Realm.deleteRealm(config);
        }
    }

    // in the realm world we generally use Currency.rates.exchangeRate, but since we've inserted an inverse...
    public Pair<Float, String> getRate(final String baseCurrency, final String targetCurrency, final GetRateListener listener) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                Currency currency = realm.where(Currency.class)
                        .equalTo("currencySymbol", baseCurrency).findFirst();
                Log.d("ALVTAG", " getRate 1 :currency: " + currency);
                if (currency != null) {
                    Log.d("ALVTAG", " getRate 2 :currency.rates.size: " + currency.rates.size());
                    Rate rate = currency.rates.where()
                            .equalTo("currencySymbol", targetCurrency).findFirst();
                    if (rate != null) {
                        Log.d("ALVTAG", " getRate 3 :currency: " + currency);
                        listener.onRateRetrieved(rate);
                        return;
                    }
                }
                Log.d("ALVTAG", " getRate 4 :onRateNotAvailable: ");
                listener.onRateNotAvailable();
            }
        });
        return null;
    }

    public interface GetRateListener {
        void onRateRetrieved(Rate rate);

        void onRateNotAvailable();
    }
}
