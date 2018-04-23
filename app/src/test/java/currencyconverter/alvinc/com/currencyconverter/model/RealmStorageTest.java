package currencyconverter.alvinc.com.currencyconverter.model;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertEquals;

import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Realm.class, RealmStorage.class, RealmConfiguration.class,
        RealmResults.class, OrderedRealmCollection.class})
@SuppressStaticInitializationFor({"io.realm.RealmConfiguration",
        "io.realm.internal.OsResults", "io.realm.internal.OsSharedRealm"})
public class RealmStorageTest {
    RealmStorage realmStorageUnderTest = RealmStorage.getInstance();
    @Mock
    Realm realm;
    @Mock
    Context context;
    @Mock
    RealmConfiguration.Builder builder;
    @Mock
    RealmConfiguration realmConfiguration;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Realm.class);
        PowerMockito.mockStatic(RealmConfiguration.class);
        PowerMockito.when(Realm.getDefaultInstance()).thenReturn(realm);
        whenNew(RealmConfiguration.Builder.class).withNoArguments().thenReturn(builder);
    }

    @Test
    public void setRateAndGet() { }

    @Test
    public void init() {
        when(builder.build()).thenReturn(realmConfiguration);

        realmStorageUnderTest.init(context);

        verifyStatic();
        Realm.init(context);
        verifyStatic();
        Realm.setDefaultConfiguration(realmConfiguration);
        verify(builder).build();
    }

    @Test
    public void close() {
        this.init();
        realmStorageUnderTest.close();

        verify(realm).close();
    }

    @Test
    public void insertRate() throws Exception {
        mockStatic(RealmStorage.class);
        RealmList<Rate> realmList = mock(RealmList.class);
        whenNew(RealmList.class).withNoArguments().thenReturn(realmList);

        //mocking for currency query
        RealmQuery<Currency> usdQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(usdQuery);
        when(usdQuery.equalTo("currencySymbol", "USD")).thenReturn(usdQuery);
        when(usdQuery.findFirst()).thenReturn(null);
        Currency currencyUSD = mock(Currency.class);
        when(realm.createObject(Currency.class)).thenReturn(currencyUSD);

        //mocking for rate
        RealmQuery<Rate> cadRateQuery = mock(RealmQuery.class);
        when(realmList.where()).thenReturn(cadRateQuery);
        when(cadRateQuery.equalTo("currencySymbol", "CAD")).thenReturn(cadRateQuery);
        Rate cadRate = mock(Rate.class);
        when(cadRateQuery.findFirst()).thenReturn(null);
        when(realm.createObject(Rate.class)).thenReturn(cadRate);

        this.init();
        realmStorageUnderTest.insertRate("USD", "CAD", 1.121F, "My Date!");

        ArgumentCaptor<Realm.Transaction> captor = ArgumentCaptor.forClass(Realm.Transaction.class);
        verify(realm).executeTransactionAsync(captor.capture());
        Realm.Transaction transaction = captor.getValue();
        transaction.execute(realm);

        //verifies for currency
        verify(usdQuery).equalTo("currencySymbol", "USD");
        verify(usdQuery).findFirst();

        //verifies for rate
        ArgumentCaptor<Rate> rateArgumentCaptor = ArgumentCaptor.forClass(Rate.class);
        verify(realmList).add(rateArgumentCaptor.capture());
        Rate result = rateArgumentCaptor.getValue();

        assertEquals("CAD", result.currencySymbol);
        assertEquals(currencyUSD, result.parent);
        assertEquals("My Date!", result.date);
        assertEquals(1.121F, result.exchangeRate, 0.01F);
    }

    @Test
    public void getCurrenciesSet() throws Exception{
        mockStatic(RealmStorage.class);
        RealmResults<Currency> realmResults = mock(RealmResults.class);
        RealmQuery<Currency> currenciesQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(currenciesQuery);
        when(currenciesQuery.findAll()).thenReturn(realmResults);

        Currency aud = mock(Currency.class);
        aud.currencySymbol = "AUD";
        aud.rates = new RealmList<>();

        ArrayList<Rate> audList = new ArrayList<>();
        Rate audSGD = new Rate();
        audSGD.currencySymbol = "SGD";
        audList.add(audSGD);
        Rate audCAD = new Rate();
        audCAD.currencySymbol = "CAD";
        audList.add(audSGD);

        Currency zar = mock(Currency.class);
        zar.currencySymbol = "ZAR";
        zar.rates = new RealmList<>();
        ArrayList<Rate> zarList = new ArrayList<>();
        Rate zarHKD = new Rate();
        zarHKD.currencySymbol = "HKD";
        zarList.add(zarHKD);

        ArrayList<Currency> currencyList = new ArrayList<>();
        currencyList.add(aud); currencyList.add(zar);

        whenNew(ArrayList.class).withArguments(realmResults).thenReturn(currencyList);
        whenNew(ArrayList.class).withArguments(aud.rates).thenReturn(audList);
        whenNew(ArrayList.class).withArguments(zar.rates).thenReturn(zarList);

        this.init();
        Set<String> results = realmStorageUnderTest.getCurrenciesSet();

        assertTrue(results.contains("CAD"));
        assertTrue(results.contains("SGD"));
        assertTrue(results.contains("AUD"));
        assertTrue(results.contains("ZAR"));
        assertEquals(4, results.size());
    }

    @Test
    public void clearData() {
        init();
        realmStorageUnderTest.clearData(context);

        verify(realm).close();
        verifyStatic(times(2));
        Realm.init(context);
        verifyStatic(times(2));
        Realm.setDefaultConfiguration(realmConfiguration);
        verify(builder, times(2)).build();
    }

    @Test
    public void getRate() {
    }
}

