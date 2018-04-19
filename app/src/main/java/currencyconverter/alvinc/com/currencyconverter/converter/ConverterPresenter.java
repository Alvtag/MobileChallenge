package currencyconverter.alvinc.com.currencyconverter.converter;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.android.volley.VolleyError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.currencyconverter.model.RateStorage;
import currencyconverter.alvinc.com.currencyconverter.net.VolleyWrapper;

class ConverterPresenter {
    private static final int MAX_DIGITS = 14;
    StringBuilder inputValueInCentsStringBuilder;
    int outputCurrencyChoice = 0;
    int inputCurrencyChoice = 0;
    List<String> currenciesList;

    private static DecimalFormat formatter;
    private ConverterActivityView converterActivityView;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(',');
        formatter = new DecimalFormat("###,###,##0.00", symbols);
        // for an Indian counting version:
        // DecimalFormat formatter2 = new DecimalFormat("##,##,##,###.##", symbols2);
    }

    ConverterPresenter(ConverterActivityView converterActivityView) {
        this.converterActivityView = converterActivityView;
        RateStorage rateStorage = RateStorage.getInstance();
        boolean hasPrevData = rateStorage.retrieveMapFromSharedPrefs();
        if (hasPrevData) {
            currenciesList = rateStorage.getCurrenciesList();
            converterActivityView.setCurrencies(currenciesList);
        } else {
            loadCurrencies(null, false);
        }
    }

    void appendChar(char c) {
        if (inputValueInCentsStringBuilder == null) {
            inputValueInCentsStringBuilder = new StringBuilder();
        }
        if (inputValueInCentsStringBuilder.length() >= MAX_DIGITS) {
            converterActivityView.setInfoText("sorry! we only support numbers up to 999,999,999,999.99");
        } else {
            inputValueInCentsStringBuilder.append(c);
            long cents = translateInputToCents();
            String inputValueToDisplay = formatNumber(cents);
            converterActivityView.setInputValue(inputValueToDisplay);
            converterActivityView.setOutputValue("");
            converterActivityView.setInfoText("");
        }
    }

    void deleteChar() {
        if (inputValueInCentsStringBuilder == null || inputValueInCentsStringBuilder.length() < 1) {
            return;
        } else if (inputValueInCentsStringBuilder.length() == 1) {
            deleteAllChars();
            return;
        }
        inputValueInCentsStringBuilder.deleteCharAt(inputValueInCentsStringBuilder.length() - 1);
        converterActivityView.setInputValue(formatNumber(translateInputToCents()));
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");
    }

    void deleteAllChars() {
        inputValueInCentsStringBuilder = null;
        converterActivityView.setInputValue("");
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");
    }

    void setInputCurrencyChoice(int position) {
        if (position >= currenciesList.size()) return;
        inputCurrencyChoice = position;
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");
    }

    void setOutputCurrencyChoice(int position) {
        if (position >= currenciesList.size()) return;
        outputCurrencyChoice = position;
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");
    }

    long translateInputToCents() {
        if (inputValueInCentsStringBuilder == null || inputValueInCentsStringBuilder.length() == 0) {
            return 0;
        }
        String inputValue = inputValueInCentsStringBuilder.toString();
        return Long.valueOf(inputValue);
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

    void convert() {
        String inputCurrency = currenciesList.get(inputCurrencyChoice);
        String outputCurrency = currenciesList.get(outputCurrencyChoice);
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");

        try {
            Pair<Float, String> rateTimeStamp = RateStorage.getInstance().getRate(inputCurrency, outputCurrency);
            float rate = rateTimeStamp.first;
            BigDecimal rateBD = new BigDecimal(String.valueOf(rate));
            BigDecimal centsBD = new BigDecimal(String.valueOf(translateInputToCents()));
            BigDecimal productBD = rateBD.multiply(centsBD);

            long result = productBD.longValue();

            converterActivityView.setOutputValue(formatNumber(result));
            converterActivityView.setInfoText("1 " + inputCurrency + " = " + rate + " " + outputCurrency + ", as of " + rateTimeStamp.second);
        } catch (RateStorage.RateNotFoundException e) {
            converterActivityView.setLoadingSpinnerVisible();
            loadCurrencies(inputCurrency, true);
        }
    }

    void persistData() {
        RateStorage.getInstance().persistData();
    }
    
    void clearData(){
        RateStorage.getInstance().clearData();
    }

    static String formatNumber(long cents) {
        BigDecimal bigDecimalTest = (new BigDecimal(String.valueOf(cents))).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return formatter.format(bigDecimalTest);
    }
}
