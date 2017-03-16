package currencyconverter.alvinc.com.paytmconverter.converter;

import com.android.volley.VolleyError;

import java.lang.ref.WeakReference;

import currencyconverter.alvinc.com.paytmconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.paytmconverter.net.VolleyWrapper;

public class RatesCallback implements VolleyWrapper.ExchangeRatesCallback {
    WeakReference<ConverterPresenter> converterPresenterWeakReference;
    private boolean pendingConversion;

    /**
     * @param pendingConversion if set to true, when new data is availble a convert() operation is called
     */
    RatesCallback(ConverterPresenter converterPresenter, boolean pendingConversion) {
        this.converterPresenterWeakReference = new WeakReference<>(converterPresenter);
        this.pendingConversion = pendingConversion;
    }

    @Override
    public void onFetchComplete(ExchangeRates exchangeRates) {
        ConverterPresenter converterPresenter = converterPresenterWeakReference.get();
        if (converterPresenter == null) return;
        converterPresenter.onNewRatesData(exchangeRates);
        if (pendingConversion) {
            converterPresenter.convert();
        }
    }

    @Override
    public void onError(VolleyError error) {
        ConverterPresenter converterPresenter = converterPresenterWeakReference.get();
        if (converterPresenter == null) return;
        converterPresenter.onRatesFetchError(error);
    }
}