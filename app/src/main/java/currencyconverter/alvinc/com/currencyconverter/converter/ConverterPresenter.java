package currencyconverter.alvinc.com.currencyconverter.converter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import currencyconverter.alvinc.com.currencyconverter.application.BaseApplication;
import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.currencyconverter.model.Rate;
import currencyconverter.alvinc.com.currencyconverter.model.RealmStorage;
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
        // Aside: for an Indian counting version:     crore,lakh,thousand
        // DecimalFormat formatter2 = new DecimalFormat("##,##,##,###.##", symbols2);
    }

    ConverterPresenter(ConverterActivityView converterActivityView) {
        this.converterActivityView = converterActivityView;
        RealmStorage storage = RealmStorage.getInstance();

        currenciesList = new ArrayList<>(storage.getCurrenciesSet());

        if (currenciesList.isEmpty()) {
            loadCurrenciesFromNetwork(null, false);
        } else {
            converterActivityView.setCurrencies(currenciesList);
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

    void loadCurrenciesFromNetwork(@Nullable String currency, boolean pendingConversion) {
        converterActivityView.setLoadingSpinnerVisible();
        VolleyWrapper.getRates(currency,
                new RatesCallback(this, pendingConversion, new Handler(Looper.getMainLooper())));
    }

    void onNewRatesData(ExchangeRates exchangeRates) {
        Map<String, Float> ratesMap = exchangeRates.getRates();
        String base = exchangeRates.getBase();
        String date = exchangeRates.getDate();

        if (currenciesList == null || currenciesList.isEmpty()) {
            currenciesList = new ArrayList<>();
            currenciesList.add(base);
            currenciesList.addAll(ratesMap.keySet());
            Collections.sort(currenciesList);
            converterActivityView.setCurrencies(currenciesList);
        }
        RealmStorage storage = RealmStorage.getInstance();
        storage.insertRate(base, base, 1f, date);
        for (String key : ratesMap.keySet()) {
            storage.insertRate(base, key, ratesMap.get(key), date);
            storage.insertRate(key, base, (1f / ratesMap.get(key)), date);
        }
        converterActivityView.setLoadingSpinnerGone();
    }

    void onRatesFetchError(VolleyError error) {
        converterActivityView.setLoadingSpinnerGone();
        converterActivityView.showError(error.toString());
    }

    void convertAndDisplay() {
        final String inputCurrency = currenciesList.get(inputCurrencyChoice);
        final String outputCurrency = currenciesList.get(outputCurrencyChoice);
        converterActivityView.setOutputValue("");
        converterActivityView.setInfoText("");
        final Handler uiHandler = new Handler(Looper.getMainLooper());

        RealmStorage.getInstance().getRate(inputCurrency, outputCurrency, new RealmStorage.GetRateListener() {
            @Override
            public void onRateRetrieved(final Rate rate) {


                BigDecimal rateBD = new BigDecimal(String.valueOf(rate.exchangeRate));
                BigDecimal centsBD = new BigDecimal(String.valueOf(translateInputToCents()));
                BigDecimal productBD = rateBD.multiply(centsBD);

                //realm objects can only be touched on its background thread, but ui objects from user thread
                final float exchangeRate = rate.exchangeRate;
                final long result = productBD.longValue();
                final String date = rate.date;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        converterActivityView.setOutputValue(formatNumber(result));
                        converterActivityView.setInfoText("1 " + inputCurrency + " = " + exchangeRate+
                                " " + outputCurrency + ", as of " + date);
                    }
                };
                uiHandler.post(runnable);
            }

            @Override
            public void onRateNotAvailable() {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ConverterPresenter.this.loadCurrenciesFromNetwork(inputCurrency, true);
                    }
                };
                uiHandler.post(runnable);
            }
        });

    }

    void clearData() {
        RealmStorage.getInstance().clearData(BaseApplication.getContext());
    }

    static String formatNumber(long cents) {
        BigDecimal bigDecimal = (new BigDecimal(String.valueOf(cents)))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return formatter.format(bigDecimal);
    }
}
