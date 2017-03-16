package currencyconverter.alvinc.com.paytmconverter.model;

import java.util.Map;

/**
 * Created by alvin on 3/14/17.
 */

public class ExchangeRates {
    private String base;
    private String date;
    private Map<String, Float> rates;

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Float> getRates() {
        return rates;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ExchangeRates{");
        stringBuilder.append("base='");
        stringBuilder.append(base);
        stringBuilder.append('\'');
        stringBuilder.append(", date='");
        stringBuilder.append(date);
        stringBuilder.append('\'');
        stringBuilder.append(", rates={");
        for(String key: rates.keySet()){
            stringBuilder.append('(');
            stringBuilder.append(key);
            stringBuilder.append('=');
            stringBuilder.append(rates.get(key));
            stringBuilder.append("),");
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}