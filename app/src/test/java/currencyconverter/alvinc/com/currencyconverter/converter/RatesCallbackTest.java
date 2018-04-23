package currencyconverter.alvinc.com.currencyconverter.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;

@RunWith(PowerMockRunner.class)
public class RatesCallbackTest {

    private RatesCallback ratesCallbackUnderTest;

    @Mock
    private ConverterPresenter converterPresenter;

    @Mock
    private ExchangeRates exchangeRates;

    @Test
    public void onFetchCompleteWithPending() {
        ratesCallbackUnderTest = new RatesCallback(converterPresenter, true, null);

        ratesCallbackUnderTest.onFetchComplete(exchangeRates);

        Mockito.verify(converterPresenter).onNewRatesData(exchangeRates);
        Mockito.verify(converterPresenter).convertAndDisplay();
    }


    @Test
    public void onFetchCompleteNoPending() {
        ratesCallbackUnderTest = new RatesCallback(converterPresenter, false, null);

        ratesCallbackUnderTest.onFetchComplete(exchangeRates);

        Mockito.verify(converterPresenter).onNewRatesData(exchangeRates);
        Mockito.verify(converterPresenter, Mockito.times(0)).convertAndDisplay();
    }
}
