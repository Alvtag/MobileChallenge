package currencyconverter.alvinc.com.paytmconverter.converter;

import java.util.List;

interface ConverterActivityView {
    void setInputValue(String inputValue);
    void setOutputValue(String outputValue);

    void setConversionRateInfo(String conversionRatio);
    void setCurrencies(List<String> currenciesList);

    void setLoadingSpinnerVisible();
    void setLoadingSpinnerGone();
    void showError(String error);

}
