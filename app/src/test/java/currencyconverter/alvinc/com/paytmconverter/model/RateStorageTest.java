package currencyconverter.alvinc.com.paytmconverter.model;

import android.support.v4.util.Pair;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RateStorageTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setRateAndGet() throws RateStorage.RateNotFoundException {
        RateStorage rateStorageUnderTest = RateStorage.getInstance();
        Assert.assertEquals(0, rateStorageUnderTest.map.size());

        rateStorageUnderTest.insertRateAndInverse("EUR", "AUD", 0.1234F, "01-12-23");
        Assert.assertEquals(2, rateStorageUnderTest.map.size());

        Pair<Float, String> rate1 = rateStorageUnderTest.getRate("EUR", "AUD");
        Assert.assertTrue(rate1.first - 0.1234F < 0.0001F);
        Assert.assertEquals("01-12-23", rate1.second);

        Pair<Float, String> rate2 = rateStorageUnderTest.getRate("AUD", "EUR");
        Assert.assertTrue(rate2.first - (1F/0.1234F) < 0.0001F);
        Assert.assertEquals("01-12-23", rate2.second);

        thrown.expect(RateStorage.RateNotFoundException.class);
        @SuppressWarnings("unused")
        Pair<Float, String> rate3 = rateStorageUnderTest.getRate("GBP", "EUR");
    }
}

