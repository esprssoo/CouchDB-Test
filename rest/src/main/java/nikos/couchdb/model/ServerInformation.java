package nikos.couchdb.model;

import com.ibm.cloud.sdk.core.util.GsonSingleton;

public class ServerInformation {
    public String couchdb;
    public String uuid;
    public String version;

    @Override
    public String toString() {
        return GsonSingleton.getGson().toJson(this);
    }
}
