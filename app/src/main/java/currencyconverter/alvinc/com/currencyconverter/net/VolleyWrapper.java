package currencyconverter.alvinc.com.currencyconverter.net;

import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import currencyconverter.alvinc.com.currencyconverter.application.BaseApplication;
import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;

public class VolleyWrapper {
    private final static String URL = "https://exchangeratesapi.io/api/latest?base=%%BASE%%";

    private final static String BASE = "%%BASE%%";
    private static final String INITIAL_CURRENCY = "CAD";

    public void getRates(@Nullable String currency, ExchangeRatesCallback exchangeRatesCallback) {
        if (currency == null || currency.isEmpty()) {
            currency = INITIAL_CURRENCY;
        }
        String url = replaceBase(URL, currency);
        RequestQueue queue = Volley.newRequestQueue(BaseApplication.getContext());
        ResponseListener responseListener = new ResponseListener(exchangeRatesCallback);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                responseListener, responseListener);
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    public String replaceBase(String url, String currency) {
        return url.replace(BASE, currency);
    }

    // a named class to wrap ExchangeRatesCallback, while
    // implementing Response.Listener and Response.ErrorListener, to ease testing
    public static class ResponseListener implements Response.Listener<String>, Response.ErrorListener {
        ExchangeRatesCallback exchangeRatesCallback;

        ResponseListener(ExchangeRatesCallback exchangeRatesCallback) {
            this.exchangeRatesCallback = exchangeRatesCallback;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            exchangeRatesCallback.onError(error);
        }

        @Override
        public void onResponse(String json) {
            Gson gson = new Gson();
            ExchangeRates exchangeRates = gson.fromJson(json, ExchangeRates.class);
            exchangeRatesCallback.onFetchComplete(exchangeRates);
        }
    }

    public interface ExchangeRatesCallback {
        void onFetchComplete(ExchangeRates exchangeRates);

        void onError(VolleyError error);
    }
}
