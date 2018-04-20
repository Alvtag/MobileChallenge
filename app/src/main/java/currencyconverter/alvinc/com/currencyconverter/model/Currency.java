package currencyconverter.alvinc.com.currencyconverter.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;

@SuppressWarnings("WeakerAccess")
public class Currency extends RealmObject{
    @Index
    public String currencySymbol;

    public RealmList<Rate> rates;
}
