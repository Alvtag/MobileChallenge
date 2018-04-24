package currencyconverter.alvinc.com.currencyconverter.net;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
import static org.mockito.Mockito.verify;
import static com.android.volley.Request.Method.GET;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseApplication.class,Gson.class, Volley.class, VolleyWrapper.class})
public class VolleyWrapperTest {

    @Mock
    private Context context;

    private Gson gson;
    private VolleyWrapper wrapperUnderTest;

    @Before
    public void setUp() {
        gson = PowerMockito.mock(Gson.class);
        wrapperUnderTest = new VolleyWrapper(gson);
    }

    @Test
    public void replace() {
        String result = wrapperUnderTest.replaceBase("http://something.net/&Meow=%%BASE%%", "cat");

        Assert.assertEquals("http://something.net/&Meow=cat", result);
    }

    @Test
    public void getRates() throws Exception {
        PowerMockito.mockStatic(BaseApplication.class);
        PowerMockito.mockStatic(Volley.class);
        PowerMockito.mockStatic(VolleyWrapper.class);
        PowerMockito.when(BaseApplication.getContext()).thenReturn(context);
        StringRequest stringRequest = mock (StringRequest.class);



        ArgumentCaptor<Response.Listener> listenerArgumentCaptor =
                ArgumentCaptor.forClass(Response.Listener.class);
        ArgumentCaptor<Response.ErrorListener> errorListenerArgumentCaptor =
                ArgumentCaptor.forClass(Response.ErrorListener.class);

        PowerMockito.whenNew(StringRequest.class)
                .withArguments(eq(GET), anyString(),
                        any(Response.Listener.class),
                        any(Response.ErrorListener.class))
                .thenReturn(stringRequest);
        RequestQueue requestQueue = mock(RequestQueue.class);
        PowerMockito.when(Volley.newRequestQueue(context)).thenReturn(requestQueue);

        VolleyWrapper.ExchangeRatesCallback callback = mock(VolleyWrapper.ExchangeRatesCallback.class);
        wrapperUnderTest.getRates("TWD", callback);

        PowerMockito.verifyStatic();
        Volley.newRequestQueue(context);

        verify(requestQueue).add(stringRequest);

        PowerMockito.verifyNew(StringRequest.class).withArguments(
                eq(GET),
                eq("https://exchangeratesapi.io/api/latest?base=TWD"),
                listenerArgumentCaptor.capture(),
                errorListenerArgumentCaptor.capture());

        Response.Listener listener = listenerArgumentCaptor.getValue();
        Response.ErrorListener errorListener = errorListenerArgumentCaptor.getValue();

//        ExchangeRates exchangeRates = mock(ExchangeRates.class);
//        String json = "json";
//        when(gson.fromJson(json, ExchangeRates.class)).thenReturn(exchangeRates);
//        //noinspection unchecked
//        listener.onResponse(json);
//        verify(callback).onFetchComplete(exchangeRates);

    }

}

