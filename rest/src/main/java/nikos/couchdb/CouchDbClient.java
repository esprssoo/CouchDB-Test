package nikos.couchdb;

import com.ibm.cloud.sdk.core.util.GsonSingleton;
import nikos.couchdb.model.Ok;
import nikos.couchdb.model.ServerInformation;
import okhttp3.*;

import java.io.IOException;

public class CouchDbClient {
    OkHttpClient client;
    String url;
    String credentials;

    public CouchDbClient(String url, String user, String password) {
        this.client = new OkHttpClient();
        this.url = url;
        this.credentials = Credentials.basic(user, password);
    }

    public ServerInformation getServerInformation() throws IOException {
        Request request = new Request.Builder()
                .get()
                .url(this.url)
                .header("Accept", "application/json")
                .build();

        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            return null;
        }

        return GsonSingleton.getGson().fromJson(response.body().charStream(), ServerInformation.class);
    }

    public Ok putDatabase(String dbName) throws IOException {
        Request request = new Request.Builder()
                .put(RequestBody.create("".getBytes()))
                .url(this.url + "/" + dbName)
                .header("Accept", "application/json")
                .header("Authorization", credentials)
                .build();

        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            System.err.println(response.code());
            return null;
        }

        return GsonSingleton.getGson().fromJson(response.body().charStream(), Ok.class);
    }
}
