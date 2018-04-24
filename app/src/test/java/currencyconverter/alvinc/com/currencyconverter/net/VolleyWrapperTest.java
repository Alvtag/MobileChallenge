package currencyconverter.alvinc.com.currencyconverter.net;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import currencyconverter.alvinc.com.currencyconverter.application.BaseApplication;
import currencyconverter.alvinc.com.currencyconverter.model.ExchangeRates;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static com.android.volley.Request.Method.GET;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseApplication.class, Gson.class, Volley.class,
        VolleyWrapper.ResponseListener.class, VolleyWrapper.class})
public class VolleyWrapperTest {

    @Mock
    private Context context;

    private Gson gson;
    private VolleyWrapper wrapperUnderTest;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BaseApplication.class);
        PowerMockito.mockStatic(Volley.class);
        PowerMockito.mockStatic(VolleyWrapper.ResponseListener.class);
        PowerMockito.mockStatic(VolleyWrapper.class);
        gson = PowerMockito.mock(Gson.class);
        PowerMockito.whenNew(Gson.class).withNoArguments().thenReturn(gson);
        wrapperUnderTest = new VolleyWrapper();
    }

    @Test
    public void replace() {
        String result = wrapperUnderTest.replaceBase("http://something.net/&Meow=%%BASE%%", "cat");

        Assert.assertEquals("http://something.net/&Meow=cat", result);
    }

    @Test
    public void getRates() throws Exception {
        PowerMockito.when(BaseApplication.getContext()).thenReturn(context);
        VolleyWrapper.ExchangeRatesCallback exchangeRatesCallback = mock(VolleyWrapper.ExchangeRatesCallback.class);

        StringRequest stringRequest = mock(StringRequest.class);
        PowerMockito.whenNew(StringRequest.class)
                .withArguments(eq(GET), anyString(),
                        any(Response.Listener.class),
                        any(Response.ErrorListener.class))
                .thenReturn(stringRequest);
        RequestQueue requestQueue = mock(RequestQueue.class);
        PowerMockito.when(Volley.newRequestQueue(context)).thenReturn(requestQueue);

        wrapperUnderTest.getRates("TWD", exchangeRatesCallback);

        PowerMockito.verifyStatic();
        Volley.newRequestQueue(context);

        PowerMockito.verifyNew(StringRequest.class, times(1)).withArguments(
                eq(GET), eq("https://exchangeratesapi.io/api/latest?base=TWD"),
                any(VolleyWrapper.ResponseListener.class),
                any(VolleyWrapper.ResponseListener.class));

        verify(requestQueue).add(any(StringRequest.class));
    }

    @Test
    public void responseListener() {
        VolleyWrapper.ExchangeRatesCallback exchangeRatesCallback =
                mock (VolleyWrapper.ExchangeRatesCallback.class);
        VolleyWrapper.ResponseListener listenerUnderTest = new VolleyWrapper.ResponseListener(exchangeRatesCallback);
        String json = "json";
        ExchangeRates exchangeRates = mock(ExchangeRates.class);
        when(gson.fromJson(json, ExchangeRates.class)).thenReturn(exchangeRates);
        listenerUnderTest.onResponse(json);
        verify(exchangeRatesCallback, times(1)).onFetchComplete(exchangeRates);
        verify(exchangeRatesCallback, times(0)).onError(any(VolleyError.class));

        VolleyError error = mock (VolleyError.class);
        listenerUnderTest.onErrorResponse(error);
        verify(exchangeRatesCallback, times(1)).onFetchComplete(exchangeRates);
        verify(exchangeRatesCallback, times(1)).onError(error);
    }
}
