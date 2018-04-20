package currencyconverter.alvinc.com.currencyconverter.model;

import io.realm.RealmObject;
import io.realm.annotations.Index;

@SuppressWarnings("WeakerAccess")
public class Rate extends RealmObject{
    @Index
    public String currencySymbol;

    public String date;

    public float exchangeRate;

    public Currency parent;
}
