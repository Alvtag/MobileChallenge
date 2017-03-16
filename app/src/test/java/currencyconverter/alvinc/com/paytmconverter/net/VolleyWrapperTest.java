package currencyconverter.alvinc.com.paytmconverter.net;

import com.android.volley.toolbox.Volley;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import currencyconverter.alvinc.com.paytmconverter.application.BaseApplication;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseApplication.class})
public class VolleyWrapperTest {

    @Test
    public void replace()  {
        String result = VolleyWrapper.replaceBase("http://something.net/&Meow=%%BASE%%","cat");

        Assert.assertEquals("http://something.net/&Meow=cat", result);
    }
}

