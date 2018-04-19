package currencyconverter.alvinc.com.currencyconverter.model;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
        ArrayList<String> currenciesList = new ArrayList<>(currenciesSet);
        Collections.sort(currenciesList);
        return currenciesList;
    }

    public void persistData() {
        SharedPrefWrapper.persistMapToSharedPrefs(map);
    }

    public void clearData() {
        this.map = new HashMap<>();
        SharedPrefWrapper.clear();
    }

    /**
     * @return a pair, first = exchange rate, second = time stamp that this rate was inserted
     * if the currency is not in the local DB this returns -1 for both
     */
    public Pair<Float, String> getRate(String baseCurrency, String targetCurrency) throws RateNotFoundException {
        if (baseCurrency.equals(targetCurrency)){
            return (new Pair<>(1.0F, "today and forever"));
        }
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

