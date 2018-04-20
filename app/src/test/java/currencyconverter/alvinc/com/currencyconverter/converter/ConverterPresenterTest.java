package currencyconverter.alvinc.com.currencyconverter.converter;


import android.support.v4.util.Pair;

import com.android.volley.VolleyError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.currencyconverter.net.VolleyWrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VolleyWrapper.class, SharedPrefWrapper.class, ConverterPresenter.class, RateStorage.class})
public class ConverterPresenterTest {
    @Mock
    private ConverterActivityView converterActivityView;

    private ConverterPresenter converterPresenterUnderTest;

    @Before
    public void setup() {
        PowerMockito.mockStatic(VolleyWrapper.class);
        PowerMockito.mockStatic(SharedPrefWrapper.class);
        converterPresenterUnderTest = new ConverterPresenter(converterActivityView);
    }

    @Test
    public void appendAndDeleteChar() {
        assertNull(converterPresenterUnderTest.inputValueInCentsStringBuilder);

        converterPresenterUnderTest.appendChar('1');

        assertNotNull(converterPresenterUnderTest.inputValueInCentsStringBuilder);
        verify(converterActivityView).setInputValue("0.01");
        verify(converterActivityView).setOutputValue("");
        verify(converterActivityView).setInfoText("");

        converterPresenterUnderTest.appendChar('2');
        verify(converterActivityView).setInputValue("0.12");
        verify(converterActivityView, times(2)).setOutputValue("");
        verify(converterActivityView, times(2)).setInfoText("");

        converterPresenterUnderTest.appendChar('3');
        verify(converterActivityView).setInputValue("1.23");
        verify(converterActivityView, times(3)).setOutputValue("");
        verify(converterActivityView, times(3)).setInfoText("");

        converterPresenterUnderTest.deleteChar();
        verify(converterActivityView, Mockito.times(2)).setInputValue("0.12");
        verify(converterActivityView, times(4)).setOutputValue("");
        verify(converterActivityView, times(4)).setInfoText("");

        converterPresenterUnderTest.deleteChar();
        verify(converterActivityView, times(2)).setInputValue("0.01");
        verify(converterActivityView, times(5)).setOutputValue("");
        verify(converterActivityView, times(5)).setInfoText("");

        converterPresenterUnderTest.deleteChar();
        verify(converterActivityView).setInputValue("");
        verify(converterActivityView, times(6)).setOutputValue("");
        verify(converterActivityView, times(6)).setInfoText("");

        converterPresenterUnderTest.deleteChar();
        //just to make sure we don't crash
    }

    @Test
    public void appendAndDeleteAllChars() {
        assertNull(converterPresenterUnderTest.inputValueInCentsStringBuilder);

        converterPresenterUnderTest.appendChar('1');
        verify(converterActivityView).setOutputValue("");
        verify(converterActivityView).setInfoText("");
        assertNotNull(converterPresenterUnderTest.inputValueInCentsStringBuilder);
        verify(converterActivityView).setInputValue("0.01");

        converterPresenterUnderTest.appendChar('2');
        verify(converterActivityView).setInputValue("0.12");
        verify(converterActivityView, times(2)).setOutputValue("");
        verify(converterActivityView, times(2)).setInfoText("");


        converterPresenterUnderTest.deleteAllChars();
        verify(converterActivityView).setInputValue("");
        verify(converterActivityView, times(3)).setOutputValue("");
        verify(converterActivityView, times(3)).setInfoText("");

        converterPresenterUnderTest.deleteChar();
        //just to make sure we don't crash
    }

    @Test
    public void translateInputToFloat() {

        converterPresenterUnderTest.appendChar('1');
        converterPresenterUnderTest.appendChar('4');
        converterPresenterUnderTest.appendChar('5');

        assertEquals(145, converterPresenterUnderTest.translateInputToCents(), 0.01);
    }

    @Test
    public void translateLargeInputToFloat() {

        converterPresenterUnderTest.appendChar('1');
        converterPresenterUnderTest.appendChar('1');
        converterPresenterUnderTest.appendChar('0');

        converterPresenterUnderTest.appendChar('2');
        converterPresenterUnderTest.appendChar('3');
        converterPresenterUnderTest.appendChar('4');

        converterPresenterUnderTest.appendChar('7');
        converterPresenterUnderTest.appendChar('8');
        converterPresenterUnderTest.appendChar('9');

        converterPresenterUnderTest.appendChar('8');
        converterPresenterUnderTest.appendChar('7');

        assertEquals(11023478987L, converterPresenterUnderTest.translateInputToCents(), 0.01F);
    }

    @Test
    public void setInputCurrencyChoice() {
        @SuppressWarnings("unchecked")
        List<String> mockCurrencies = mock(List.class);
        converterPresenterUnderTest.currenciesList = mockCurrencies;
        when(mockCurrencies.size()).thenReturn(4);

        converterPresenterUnderTest.setInputCurrencyChoice(1);

        assertEquals(1, converterPresenterUnderTest.inputCurrencyChoice);
        verify(converterActivityView).setOutputValue("");
        verify(converterActivityView).setInfoText("");
    }

    @Test
    public void setOutputCurrencyChoice() {
        @SuppressWarnings("unchecked")
        List<String> mockCurrencies = mock(List.class);
        converterPresenterUnderTest.currenciesList = mockCurrencies;
        when(mockCurrencies.size()).thenReturn(4);

        converterPresenterUnderTest.setOutputCurrencyChoice(1);

        assertEquals(1, converterPresenterUnderTest.outputCurrencyChoice);
        verify(converterActivityView).setOutputValue("");
        verify(converterActivityView).setInfoText("");
    }

    @Test
    public void loadCurrencies() throws Exception {
        RatesCallback ratesCallback = mock(RatesCallback.class);
        // very strange mockito "feature" that you're to mockStatic the class that calls the constructor,
        // rather then mockStatic(RatesCallback.class)!
        PowerMockito.mockStatic(ConverterPresenter.class);
        PowerMockito.whenNew(RatesCallback.class)
                .withArguments(converterPresenterUnderTest, true)
                .thenReturn(ratesCallback);

        converterPresenterUnderTest.loadCurrencies("EUR", true);

        PowerMockito.verifyStatic();
        VolleyWrapper.getRates("EUR", ratesCallback);
    }

    @Test
    public void onNewRatesData() throws Exception {
        ExchangeRates exchangeRates = mock(ExchangeRates.class);
        Map<String, Float> ratesMap = new HashMap<>();
        ratesMap.put("AUD", 1.32F);
        ratesMap.put("CAD", 1.78F);
        when(exchangeRates.getRates()).thenReturn(ratesMap);
        when(exchangeRates.getBase()).thenReturn("EUR");
        when(exchangeRates.getDate()).thenReturn("10-12-18");
        PowerMockito.mockStatic(ConverterPresenter.class);
        PowerMockito.mockStatic(RateStorage.class);
        RateStorage mockRateStorage = mock(RateStorage.class);
        PowerMockito.when(RateStorage.getInstance()).thenReturn(mockRateStorage);

        converterPresenterUnderTest = new ConverterPresenter(converterActivityView);
        verify(converterActivityView, times(1)).setCurrencies(Matchers.anyListOf(String.class));

        converterPresenterUnderTest.onNewRatesData(exchangeRates);
        verify(converterActivityView, times(2)).setCurrencies(Matchers.anyListOf(String.class));

        assertEquals(3, converterPresenterUnderTest.currenciesList.size());
        assertEquals("EUR", converterPresenterUnderTest.currenciesList.get(0));
        assertEquals("AUD", converterPresenterUnderTest.currenciesList.get(1));
        assertEquals("CAD", converterPresenterUnderTest.currenciesList.get(2));
        verify(mockRateStorage).insertRateAndInverse("EUR", "AUD", 1.32F, "10-12-18");
        verify(mockRateStorage).insertRateAndInverse("EUR", "CAD", 1.78F, "10-12-18");
        verify(mockRateStorage, times(2)).insertRateAndInverse("EUR", "EUR", 1.0f, "10-12-18");
        verify(converterActivityView).setLoadingSpinnerGone();
    }

    @Test
    public void onRatesFetchError() {
        VolleyError mockVolleyError = mock(VolleyError.class);
        when(mockVolleyError.toString()).thenReturn("hi");

        converterPresenterUnderTest.onRatesFetchError(mockVolleyError);

        verify(converterActivityView).setLoadingSpinnerGone();
        verify(converterActivityView).showError("hi");
    }

    @Test
    public void convertForFoundItem() throws RateStorage.RateNotFoundException {
        converterPresenterUnderTest.inputCurrencyChoice = 1;
        converterPresenterUnderTest.outputCurrencyChoice = 0;
        converterPresenterUnderTest.currenciesList = new ArrayList<>();
        converterPresenterUnderTest.currenciesList.add("CAD");
        converterPresenterUnderTest.currenciesList.add("USD");
        PowerMockito.mockStatic(RateStorage.class);
        RateStorage mockRateStorage = mock(RateStorage.class);
        PowerMockito.when(RateStorage.getInstance()).thenReturn(mockRateStorage);
        when(mockRateStorage.getRate("USD", "CAD"))
                .thenReturn(new Pair<>(0.78F, "01-12-12"));
        assertNull(converterPresenterUnderTest.inputValueInCentsStringBuilder);

        converterPresenterUnderTest.appendChar('1');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.convert();

        verify(converterActivityView, times(6)).setOutputValue("");
        verify(converterActivityView, times(6)).setInfoText("");
        verify(converterActivityView).setOutputValue("78.00");
        verify(converterActivityView).setInfoText("1 USD = 0.78 CAD, as of 01-12-12");
    }

    @Test
    public void convertForMissingItem() throws Exception {
        converterPresenterUnderTest.inputCurrencyChoice = 1;
        converterPresenterUnderTest.outputCurrencyChoice = 0;
        converterPresenterUnderTest.currenciesList = new ArrayList<>();
        converterPresenterUnderTest.currenciesList.add("CAD");
        converterPresenterUnderTest.currenciesList.add("USD");
        PowerMockito.mockStatic(RateStorage.class);
        RateStorage mockRateStorage = mock(RateStorage.class);
        PowerMockito.when(RateStorage.getInstance()).thenReturn(mockRateStorage);
        when(mockRateStorage.getRate("USD", "CAD"))
                .thenThrow(new RateStorage.RateNotFoundException());
        RatesCallback ratesCallback = mock(RatesCallback.class);
        PowerMockito.mockStatic(ConverterPresenter.class);
        PowerMockito.whenNew(RatesCallback.class)
                .withArguments(converterPresenterUnderTest, true)
                .thenReturn(ratesCallback);

        converterPresenterUnderTest.appendChar('1');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.appendChar('0');
        converterPresenterUnderTest.convert();

        verify(converterActivityView, times(6)).setOutputValue("");
        verify(converterActivityView, times(6)).setInfoText("");
        PowerMockito.verifyStatic();
        VolleyWrapper.getRates("USD", ratesCallback);
    }

    @Test
    public void formatTinyNumber() {
        assertEquals("0.02", ConverterPresenter.formatNumber(2L));
    }

    @Test
    public void formatSmallNumber() {
        assertEquals("121.31", ConverterPresenter.formatNumber(12131L));
    }

    @Test
    public void formatBigNumber() {
        assertEquals("841,212,121,567.12", ConverterPresenter.formatNumber(84121212156712L));
    }
}
