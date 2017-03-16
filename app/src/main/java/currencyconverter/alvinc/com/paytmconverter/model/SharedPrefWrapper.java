package currencyconverter.alvinc.com.paytmconverter.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;

import currencyconverter.alvinc.com.paytmconverter.application.BaseApplication;

class SharedPrefWrapper {
    /**
     * @return true if a map of currencies was read from sharedPrefs
     */
    static HashMap<Pair<String, String>, Pair<Float, String>> retrieveMapFromSharedPrefs() {
        String serializedMap =
                BaseApplication.getContext().getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
                        .getString("currencyMap", null);
        if (serializedMap == null) {
            return null;
        }
        Gson gson = new Gson();

        HashMap<Pair<String, String>, Pair<Float, String>> map = new HashMap<>();
        HashMap<String, LinkedTreeMap<Object, Object>> regenMap = gson.fromJson(serializedMap, map.getClass());
        // unfortunately, Gson doesn't handle our Pairs key/values,
        // it mashes the key into one string "PAIR{CAD AUD}" and the values into LinkedTreeMap
        // so we have to copy each value over, and split the key
        for (String combinedKey : regenMap.keySet()) {
            //the format is not expected to change since we're always serializing the data the same way
            LinkedTreeMap<Object, Object> treeMap = regenMap.get(combinedKey);

            float rate = (float) (double) treeMap.get("first");
            String date = (String) treeMap.get("second");
            Pair<Float, String> valuePair = new Pair<>(rate, date);

            combinedKey = combinedKey.replace("Pair{", "").replace("}", "");
            String[] keysArray = combinedKey.split("\\s+");

            Pair<String, String> keyPair = new Pair<>(keysArray[0], keysArray[1]);
            map.put(keyPair, valuePair);
        }
        return map;
    }

    static void persistMapToSharedPrefs(HashMap<Pair<String, String>, Pair<Float, String>> map) {

        Gson gson = new Gson();
        String serializedMap = gson.toJson(map);
        SharedPreferences.Editor editor =
                BaseApplication.getContext().getSharedPreferences("shared_prefs", Context.MODE_PRIVATE).edit();
        editor.putString("currencyMap", serializedMap).apply();
    }
}
