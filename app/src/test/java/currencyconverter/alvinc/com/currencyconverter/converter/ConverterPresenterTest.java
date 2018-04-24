package currencyconverter.alvinc.com.currencyconverter.converter;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;
import currencyconverter.alvinc.com.currencyconverter.model.RealmStorage;
import currencyconverter.alvinc.com.currencyconverter.net.VolleyWrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({VolleyWrapper.class, Gson.class, ConverterPresenter.class, RealmStorage.class, Looper.class})
public class ConverterPresenterTest {
    @Mock
    private ConverterActivityView converterActivityView;
    @Mock
    private RealmStorage realmStorage;
    private ConverterPresenter converterPresenterUnderTest;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Looper.class);
        PowerMockito.mockStatic(VolleyWrapper.class);
        PowerMockito.mockStatic(RealmStorage.class);
        PowerMockito.when(RealmStorage.getInstance()).thenReturn(realmStorage);
        HashSet<String> currencySet = new HashSet<>();
        currencySet.add("HKD");
        currencySet.add("CAD");
        when(realmStorage.getCurrenciesSet()).thenReturn(currencySet);
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
        Gson gson = PowerMockito.mock(Gson.class);
        RatesCallback ratesCallback = mock(RatesCallback.class);
        // very strange mockito "feature" that you're to mockStatic the class that calls the constructor,
        // rather then mockStatic(RatesCallback.class)!
        PowerMockito.mockStatic(ConverterPresenter.class);
        VolleyWrapper wrapper = mock(VolleyWrapper.class);
        PowerMockito.whenNew(RatesCallback.class)
                .withArguments(eq(converterPresenterUnderTest), eq(true), any(Handler.class))
                .thenReturn(ratesCallback);
        PowerMockito.whenNew(Gson.class).withNoArguments().thenReturn(gson);
        PowerMockito.whenNew(VolleyWrapper.class)
                .withNoArguments()
                .thenReturn(wrapper);

        converterPresenterUnderTest.loadCurrenciesFromNetwork("EUR", true);

        verify(wrapper).getRates("EUR", ratesCallback);
    }

    @Test
    public void onNewRatesData() {
        converterPresenterUnderTest.currenciesList = null;

        ExchangeRates exchangeRates = mock(ExchangeRates.class);
        Map<String, Float> ratesMap = new HashMap<>();
        ratesMap.put("AUD", 1.32F);
        ratesMap.put("CAD", 1.78F);
        when(exchangeRates.getRates()).thenReturn(ratesMap);
        when(exchangeRates.getBase()).thenReturn("EUR");
        when(exchangeRates.getDate()).thenReturn("10-12-18");
        PowerMockito.mockStatic(ConverterPresenter.class);

        verify(converterActivityView, times(1)).setCurrencies(Matchers.anyListOf(String.class));
        converterPresenterUnderTest.onNewRatesData(exchangeRates);
        verify(converterActivityView, times(2)).setCurrencies(Matchers.anyListOf(String.class));

        assertEquals(3, converterPresenterUnderTest.currenciesList.size());
        assertTrue(converterPresenterUnderTest.currenciesList.contains("EUR"));
        assertTrue(converterPresenterUnderTest.currenciesList.contains("AUD"));
        assertTrue(converterPresenterUnderTest.currenciesList.contains("CAD"));
        verify(realmStorage).insertRate("EUR", "AUD", 1.32F, "10-12-18");
        verify(realmStorage).insertRate(eq("AUD"), eq("EUR"), anyFloat(), eq("10-12-18"));
        verify(realmStorage).insertRate("EUR", "CAD", 1.78F, "10-12-18");
        verify(realmStorage).insertRate(eq("CAD"), eq("EUR"), anyFloat(), eq("10-12-18"));
        verify(realmStorage, times(1)).insertRate("EUR", "EUR", 1.0f, "10-12-18");
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
