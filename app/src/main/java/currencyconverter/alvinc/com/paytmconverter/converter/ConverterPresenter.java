package currencyconverter.alvinc.com.paytmconverter.converter;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import currencyconverter.alvinc.com.paytmconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.paytmconverter.model.RateStorage;
import currencyconverter.alvinc.com.paytmconverter.net.VolleyWrapper;

class ConverterPresenter {
    StringBuilder inputValueInCentsStringBuilder;
    private ConverterActivityView converterActivityView;

    int outputCurrencyChoice = 0;
    int inputCurrencyChoice = 0;
    List<String> currenciesList;

    ConverterPresenter(ConverterActivityView converterActivityView) {
        this.converterActivityView = converterActivityView;
        loadCurrencies(null, false);
    }

    void appendChar(char c) {
        if (inputValueInCentsStringBuilder == null) {
            inputValueInCentsStringBuilder = new StringBuilder();
        }
        inputValueInCentsStringBuilder.append(c);
        converterActivityView.setInputValue(convertToTwoDecimals(translateInputToFloat()));
    }

    void deleteChar() {
        if (inputValueInCentsStringBuilder == null || inputValueInCentsStringBuilder.length() < 1) {
            return;
        } else if (inputValueInCentsStringBuilder.length() == 1) {
            deleteAllChars();
            return;
        }
        inputValueInCentsStringBuilder.deleteCharAt(inputValueInCentsStringBuilder.length() - 1);
        converterActivityView.setInputValue(convertToTwoDecimals(translateInputToFloat()));
    }

    void deleteAllChars() {
        inputValueInCentsStringBuilder = null;
        converterActivityView.setInputValue("");
        converterActivityView.setOutputValue("");
        converterActivityView.setConversionRateInfo("");
    }

    void setInputCurrencyChoice(int position) {
        if (position >= currenciesList.size()) return;
        inputCurrencyChoice = position;
        converterActivityView.setOutputValue("");
        converterActivityView.setConversionRateInfo("");
    }

    void setOutputCurrencyChoice(int position) {
        if (position >= currenciesList.size()) return;
        outputCurrencyChoice = position;
        converterActivityView.setOutputValue("");
        converterActivityView.setConversionRateInfo("");
    }

    void convert() {
        String inputCurrency = currenciesList.get(inputCurrencyChoice);
        String outputCurrency = currenciesList.get(outputCurrencyChoice);
        converterActivityView.setOutputValue("");
        converterActivityView.setConversionRateInfo("");

        try {
            Pair<Float, String> rateTimeStamp = RateStorage.getInstance().getRate(inputCurrency, outputCurrency);
            float rate = rateTimeStamp.first;
            float result = rate * translateInputToFloat();

            converterActivityView.setOutputValue(convertToTwoDecimals(result));
            converterActivityView.setConversionRateInfo("1 " + inputCurrency + " = " + rate + " " + outputCurrency + ", as of " + rateTimeStamp.second);
        } catch (RateStorage.RateNotFoundException e) {
            converterActivityView.setLoadingSpinnerVisible();
            //todo disable UI elements until network call returns
            loadCurrencies(inputCurrency, true);
        }
    }

    float translateInputToFloat() {
        if (inputValueInCentsStringBuilder == null || inputValueInCentsStringBuilder.length() == 0){
            return 0F;
        }
        String inputValue = inputValueInCentsStringBuilder.toString();
        float valueInCents = Float.valueOf(inputValue);
        return valueInCents / 100;
    }

    void loadCurrencies(@Nullable String currency, boolean pendingConversion) {
        VolleyWrapper.getRates(currency, new RatesCallback(this, pendingConversion));
    }

    void onNewRatesData(ExchangeRates exchangeRates) {
        Map<String, Float> ratesMap = exchangeRates.getRates();
        String base = exchangeRates.getBase();
        String date = exchangeRates.getDate();
        if (currenciesList == null) {
            currenciesList = new ArrayList<>();
            currenciesList.add(base);
            for (String key : ratesMap.keySet()) {
                currenciesList.add(key);
            }
            // currenciesList is now valid.
            converterActivityView.setCurrencies(currenciesList);
        }

        for (String key : ratesMap.keySet()) {
            RateStorage rateStorage = RateStorage.getInstance();
            rateStorage.insertRateAndInverse(base, key, ratesMap.get(key), date);
            rateStorage.insertRateAndInverse(base, base, 1.0f, date); //safety thing, not really necessary.
        }
        converterActivityView.setLoadingSpinnerGone();
    }

    void onRatesFetchError(VolleyError error) {
        converterActivityView.setLoadingSpinnerGone();
        converterActivityView.showError(error.toString());
    }

    static String convertToTwoDecimals(float input) {
        return String.format(Locale.ENGLISH, "%.2f", input);
    }
}
