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
import static org.mockito.Matchers.any;
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
        Rate insertedRate = rateArgumentCaptor.getValue();

        assertEquals("CAD", insertedRate.currencySymbol);
        assertEquals(currencyUSD, insertedRate.parent);
        assertEquals("My Date!", insertedRate.date);
        assertEquals(1.121F, insertedRate.exchangeRate, 0.01F);
    }

    @Test
    public void getCurrenciesSet() throws Exception{
        mockStatic(RealmStorage.class);
        mockStatic(RealmResults.class);
        RealmResults<Currency> realmResults = PowerMockito.mock(RealmResults.class);
        RealmQuery<Currency> currenciesQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(currenciesQuery);
        when(currenciesQuery.findAll()).thenReturn(realmResults);

        //mocked currency results
        Currency aud = mock(Currency.class);
        aud.currencySymbol = "AUD";
        aud.rates = PowerMockito.mock(RealmList.class);
        ArrayList<Rate> audList = new ArrayList<>();
        Rate audSGD = new Rate();
        audSGD.currencySymbol = "SGD";
        audList.add(audSGD);
        Rate audCAD = new Rate();
        audCAD.currencySymbol = "CAD";
        audList.add(audCAD);
        Currency zar = mock(Currency.class);
        zar.currencySymbol = "ZAR";
        zar.rates = PowerMockito.mock(RealmList.class);
        ArrayList<Rate> zarList = new ArrayList<>();
        Rate zarHKD = new Rate();
        zarHKD.currencySymbol = "CAD";
        zarList.add(zarHKD);
        ArrayList<Currency> currencyList = new ArrayList<>();
        currencyList.add(aud);
        currencyList.add(zar);

        // This here is a pretty glorious piece of mocking work
        // since RealmResults has a custom iterator (due to managed object)
        // we must also mock it's iterator. so, we create our own arrayLists
        // and pass the arraylist's iterators in place of the default!
        PowerMockito.when(realmResults.iterator()).thenReturn(currencyList.iterator());
        PowerMockito.when(aud.rates.iterator()).thenReturn(zarList.iterator());
        PowerMockito.when(zar.rates.iterator()).thenReturn(audList.iterator());

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
    public void getRate_found() {
        RealmStorage.GetRateListener getRateListener =
                mock(RealmStorage.GetRateListener.class);

        //mocking for currency query
        RealmQuery<Currency> usdQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(usdQuery);
        when(usdQuery.equalTo("currencySymbol", "USD")).thenReturn(usdQuery);
        Currency currencyUSD = mock(Currency.class);
        when(usdQuery.findFirst()).thenReturn(currencyUSD);
        currencyUSD.rates = mock(RealmList.class);

        //mocking for rate
        RealmQuery<Rate> cadRateQuery = mock(RealmQuery.class);
        when(currencyUSD.rates.where()).thenReturn(cadRateQuery);
        when(cadRateQuery.equalTo("currencySymbol", "CAD")).thenReturn(cadRateQuery);
        Rate cadRate = mock(Rate.class);
        when(cadRateQuery.findFirst()).thenReturn(cadRate);

        this.init();
        realmStorageUnderTest.getRate("USD", "CAD", getRateListener);

        ArgumentCaptor<Realm.Transaction> captor = ArgumentCaptor.forClass(Realm.Transaction.class);
        verify(realm).executeTransactionAsync(captor.capture());
        Realm.Transaction transaction = captor.getValue();
        transaction.execute(realm);
        verify(getRateListener, times(1)).onRateRetrieved(cadRate);
        verify(getRateListener, times(0)).onRateNotAvailable();
    }

    @Test
    public void getRate_currencyFound_rateNotFound() {
        RealmStorage.GetRateListener getRateListener =
                mock(RealmStorage.GetRateListener.class);

        //mocking for currency query
        RealmQuery<Currency> usdQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(usdQuery);
        when(usdQuery.equalTo("currencySymbol", "USD")).thenReturn(usdQuery);
        Currency currencyUSD = mock(Currency.class);
        when(usdQuery.findFirst()).thenReturn(currencyUSD);
        currencyUSD.rates = mock(RealmList.class);

        //mocking for rate
        RealmQuery<Rate> cadRateQuery = mock(RealmQuery.class);
        when(currencyUSD.rates.where()).thenReturn(cadRateQuery);
        when(cadRateQuery.equalTo("currencySymbol", "CAD")).thenReturn(cadRateQuery);
        when(cadRateQuery.findFirst()).thenReturn(null);

        this.init();
        realmStorageUnderTest.getRate("USD", "CAD", getRateListener);

        ArgumentCaptor<Realm.Transaction> captor = ArgumentCaptor.forClass(Realm.Transaction.class);
        verify(realm).executeTransactionAsync(captor.capture());
        Realm.Transaction transaction = captor.getValue();
        transaction.execute(realm);
        verify(getRateListener, times(0)).onRateRetrieved(any(Rate.class));
        verify(getRateListener, times(1)).onRateNotAvailable();
    }

    @Test
    public void getRate_currencyNotFound() {
        RealmStorage.GetRateListener getRateListener =
                mock(RealmStorage.GetRateListener.class);

        //mocking for currency query
        RealmQuery<Currency> usdQuery = mock(RealmQuery.class);
        when(realm.where(Currency.class)).thenReturn(usdQuery);
        when(usdQuery.equalTo("currencySymbol", "USD")).thenReturn(usdQuery);
        when(usdQuery.findFirst()).thenReturn(null);

        this.init();
        realmStorageUnderTest.getRate("USD", "CAD", getRateListener);

        ArgumentCaptor<Realm.Transaction> captor = ArgumentCaptor.forClass(Realm.Transaction.class);
        verify(realm).executeTransactionAsync(captor.capture());
        Realm.Transaction transaction = captor.getValue();
        transaction.execute(realm);

        verify(getRateListener, times(0)).onRateRetrieved(any(Rate.class));
        verify(getRateListener, times(1)).onRateNotAvailable();
    }
}
