package currencyconverter.alvinc.com.currencyconverter.net;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import currencyconverter.alvinc.com.currencyconverter.application.BaseApplication;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseApplication.class})
public class VolleyWrapperTest {

    @Test
    public void replace()  {
        String result = VolleyWrapper.replaceBase("http://something.net/&Meow=%%BASE%%","cat");

        Assert.assertEquals("http://something.net/&Meow=cat", result);
    }
}

