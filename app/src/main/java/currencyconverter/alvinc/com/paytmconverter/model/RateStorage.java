package currencyconverter.alvinc.com.paytmconverter.model;

import android.content.Context;
import android.support.v4.util.Pair;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import currencyconverter.alvinc.com.paytmconverter.application.BaseApplication;

public class RateStorage {
    private static RateStorage instance = new RateStorage();
    // a complicated object, the key is <BaseCurrency, TargetCurrency
    //                       the value is <Rate, timestamp>
    HashMap<Pair<String, String>, Pair<Float, String>> map = new HashMap<>();

    public static RateStorage getInstance() {
        if (instance == null) {
            instance = new RateStorage();
        }
        return instance;
    }

    private RateStorage() {
    }

    /**
     * For convenience also inserts the inverse.
     * i.e. calling USD, CAD, 1.33 would insert that plus
     * CAD, USD, (1/1.33 = .75)
     * this saves the user a bit of data when he reverses.
     */
    public void insertRateAndInverse(String baseCurrency, String targetCurrency, float rate, String date) {
        insertRateInternal(baseCurrency, targetCurrency, rate, date);
        insertRateInternal(targetCurrency, baseCurrency, (1f / rate), date);
    }

    private void insertRateInternal(String baseCurrency, String targetCurrency, float rate, String date) {
        map.put(new Pair<>(baseCurrency, targetCurrency), new Pair<>(rate, date));
    }

    /**
     * @return true if a map of currencies was read from sharedPrefs
     */
    public boolean retrieveMapFromSharedPrefs() {
        HashMap<Pair<String, String>, Pair<Float, String>> regenMap = SharedPrefWrapper.retrieveMapFromSharedPrefs();
        if(regenMap != null){
            map = regenMap;
            return true;
        }
        return false;
    }

    public List<String> getCurrenciesList() {
        HashSet<String> currenciesSet = new HashSet<>();
        for (Pair<String, String> keyPair : map.keySet()) {
            currenciesSet.add(keyPair.first);
            currenciesSet.add(keyPair.second);
        }

        return new ArrayList<>(currenciesSet);
    }

    public void persistMap() {
        SharedPrefWrapper.persistMapToSharedPrefs(map);
    }

    public void clearData() {
        BaseApplication.getContext().getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    /**
     * @return a pair, first = exchange rate, second = time stamp that this rate was inserted
     * if the currency is not in the local DB this returns -1 for both
     */
    public Pair<Float, String> getRate(String baseCurrency, String targetCurrency) throws RateNotFoundException {
        Pair pair = new Pair<>(baseCurrency, targetCurrency);
        if (map.containsKey(pair)) {
            return map.get(pair);
        } else {
            //here I could return some arbitrary value (-1), but I think this allows
            //the caller to properly care for the case, catching an exception is cleaner
            //then doing some weird comparison for an arbitrary value
            throw new RateNotFoundException();
        }
    }

    public static class RateNotFoundException extends Exception {
    }
}

