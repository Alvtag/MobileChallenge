package currencyconverter.alvinc.com.currencyconverter.converter;

import android.os.Handler;

import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;

import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.currencyconverter.net.VolleyWrapper;

class RatesCallback implements VolleyWrapper.ExchangeRatesCallback {
    private WeakReference<ConverterPresenter> converterPresenterWeakReference;
    private boolean pendingConversion;
    private Handler uiHandler;

    /**
     * @param pendingConversion if set to true, when new data is available a convertAndDisplay()
     *                          operation will be called
     */
    RatesCallback(ConverterPresenter converterPresenter, boolean pendingConversion,
                  Handler uiHandler) {
        this.converterPresenterWeakReference = new WeakReference<>(converterPresenter);
        this.pendingConversion = pendingConversion;
        this.uiHandler = uiHandler;
    }

    @Override
    public void onFetchComplete(final ExchangeRates exchangeRates) {
        final ConverterPresenter converterPresenter = converterPresenterWeakReference.get();
        if (converterPresenter == null) return;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                converterPresenter.onNewRatesData(exchangeRates);
                if (pendingConversion) {
                    converterPresenter.convertAndDisplay();
                }
            }
        };
        uiHandler.post(runnable);
    }

    @Override
    public void onError(final VolleyError error) {
        final ConverterPresenter converterPresenter = converterPresenterWeakReference.get();
        if (converterPresenter == null) return;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                converterPresenter.onRatesFetchError(error);
            }
        };
        uiHandler.post(runnable);
    }
}