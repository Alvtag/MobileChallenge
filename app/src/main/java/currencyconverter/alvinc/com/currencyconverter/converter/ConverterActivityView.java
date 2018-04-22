package currencyconverter.alvinc.com.currencyconverter.converter;

import java.util.List;

interface ConverterActivityView {
    void setInputValue(final String inputValue);
    void setOutputValue(final String outputValue);

    void setInfoText(String conversionRatio);
    void setCurrencies(List<String> currenciesList);

    void setLoadingSpinnerVisible();
    void setLoadingSpinnerGone();

    void setButtonsEnabled();
    void setButtonsDisabled();

    void showError(String error);

}

