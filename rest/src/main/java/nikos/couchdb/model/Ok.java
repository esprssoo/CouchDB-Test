package nikos.couchdb.model;

import com.ibm.cloud.sdk.core.util.GsonSingleton;

public class Ok {
    protected Boolean ok;

    public Boolean isOk() {
        return ok;
    }

    @Override
    public String toString() {
        return GsonSingleton.getGson().toJson(this);
    }
}
