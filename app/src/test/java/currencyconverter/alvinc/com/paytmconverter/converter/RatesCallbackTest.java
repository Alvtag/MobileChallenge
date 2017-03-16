package currencyconverter.alvinc.com.paytmconverter.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import currencyconverter.alvinc.com.paytmconverter.model.ExchangeRates;

@RunWith(PowerMockRunner.class)
public class RatesCallbackTest {

    private RatesCallback ratesCallbackUnderTest;

    @Mock
    private ConverterPresenter converterPresenter;

    @Mock
    private ExchangeRates exchangeRates;

    @Test
    public void onFetchCompleteWithPending() {
        ratesCallbackUnderTest = new RatesCallback(converterPresenter, true);

        ratesCallbackUnderTest.onFetchComplete(exchangeRates);

        Mockito.verify(converterPresenter).onNewRatesData(exchangeRates);
        Mockito.verify(converterPresenter).convert();
    }


    @Test
    public void onFetchCompleteNoPending() {
        ratesCallbackUnderTest = new RatesCallback(converterPresenter, false);

        ratesCallbackUnderTest.onFetchComplete(exchangeRates);

        Mockito.verify(converterPresenter).onNewRatesData(exchangeRates);
        Mockito.verify(converterPresenter, Mockito.times(0)).convert();
    }
}
