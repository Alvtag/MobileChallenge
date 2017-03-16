package currencyconverter.alvinc.com.paytmconverter.net;

import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import currencyconverter.alvinc.com.paytmconverter.application.BaseApplication;
import currencyconverter.alvinc.com.paytmconverter.model.ExchangeRates;

public class VolleyWrapper {
    private final static String URL = "http://api.fixer.io/latest?base=%%BASE%%";
    private final static String BASE = "%%BASE%%";
    private static final String INITIAL_CURRENCY = "CAD";

    private static final Gson gson = new Gson();

    public static void getRates(@Nullable String currency, final ExchangeRatesCallback exchangeRatesCallback) {
        if (currency == null || currency.isEmpty()) {
            currency = INITIAL_CURRENCY;
        }
        String url = replaceBase(URL, currency);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(BaseApplication.getContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String json) {
                        ExchangeRates exchangeRates = gson.fromJson(json, ExchangeRates.class);
                        exchangeRatesCallback.onFetchComplete(exchangeRates);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                exchangeRatesCallback.onError(error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private static String replaceBase(String url, String currency) {
        return url.replace(BASE, currency);
    }

    public interface ExchangeRatesCallback {
        void onFetchComplete(ExchangeRates exchangeRates);

        void onError(VolleyError error);
    }
}
