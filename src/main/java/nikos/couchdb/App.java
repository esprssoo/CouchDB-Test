package nikos.couchdb;

import com.google.gson.*;
import com.ibm.cloud.cloudant.v1.Cloudant;
import com.ibm.cloud.cloudant.v1.model.IndexDefinition;
import com.ibm.cloud.cloudant.v1.model.IndexField;
import com.ibm.cloud.cloudant.v1.model.PostFindOptions;
import com.ibm.cloud.cloudant.v1.model.PostIndexOptions;
import com.ibm.cloud.sdk.core.security.BasicAuthenticator;
import com.ibm.cloud.sdk.core.util.GsonSingleton;
import okhttp3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        var cloudant = new IBMCloudant();
        cloudant.main();

        var rest = new RestClient();
        try {
            rest.main();
        } catch (IOException e) {
        }
    }

}

class IBMCloudant {
    public void main() {
        // Connect
        var authenticator = new BasicAuthenticator.Builder()
                .username("admin")
                .password("password")
                .build();
        var client = new Cloudant(Cloudant.DEFAULT_SERVICE_NAME, authenticator);
        var serverInfo = client.getServerInformation().execute().getResult();
        System.out.println(serverInfo.getVersion());

        // Create index for "Weight"
        var fields = new IndexField.Builder()
                .add("Weight", "asc")
                .build();
        var index = new IndexDefinition.Builder()
                .addFields(fields)
                .build();
        var indexOptions = new PostIndexOptions.Builder()
                .db("animaldb")
                .name("animalWeightIndex")
                .ddoc("weight-json-index")
                .type("json")
                .index(index)
                .build();
        client.postIndex(indexOptions).execute();

        // Find animal documents by name "Brown bear" sorted by Weight
        Map<String, Object> selector = Collections.singletonMap("Name",
                Collections.singletonMap("$eq", "Brown bear"));
        var findOptions = new PostFindOptions.Builder()
                .db("animaldb")
                .selector(selector)
                .fields(Arrays.asList("_id", "Type", "Name", "Size", "Weight"))
                .addSort(Collections.singletonMap("Weight", "desc"))
                .build();
        var result = client.postFind(findOptions).execute().getResult();
        System.out.println(result);
    }
}

class RestClient {

    public void main() throws IOException {
        var url = "http://localhost:5984";
        var credentials = Credentials.basic("admin", "password");
        var client = new OkHttpClient();

        // Connect - get server info
        var request = new Request.Builder()
                .get()
                .url(url)
                .header("Accept", "application/json")
                .build();
        Response infoResponse = client.newCall(request).execute();
        var info = GsonSingleton.getGson().fromJson(infoResponse.body().charStream(), JsonElement.class);
        System.out.println(info.getAsJsonObject().get("version"));

        // Create index for "Weight"
        var jsonObj = new JsonObject();
        jsonObj.addProperty("name", "animalWeightIndex");
        jsonObj.addProperty("ddoc", "weight-json-index");
        jsonObj.addProperty("type", "json");
        jsonObj.add("index", GsonSingleton.getGson().toJsonTree(Collections.singletonMap(
                "index", Collections.singletonMap("fields", Collections.singletonList("Weight"))
        )));
        var json = GsonSingleton.getGson().toJson(jsonObj);
        var body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        request = new Request.Builder()
                .post(body)
                .url(url + "/animaldb/_index")
                .header("Authorization", credentials)
                .build();
        client.newCall(request).execute();

        // Find animal documents by name "Brown bear" sorted by Weight
        Map<String, Object> selector = Collections.singletonMap("Name",
                Collections.singletonMap("$eq", "Brown bear"));
        jsonObj = new JsonObject();
        jsonObj.add("sort", GsonSingleton.getGson().toJsonTree(
                Collections.singletonList(Collections.singletonMap("Weight", "desc"))
        ));
        jsonObj.add("selector", GsonSingleton.getGson().toJsonTree(
                Collections.singletonMap("Name", Collections.singletonMap(
                        "$eq", "Brown bear"
                ))
        ));
        json = GsonSingleton.getGson().toJson(jsonObj);
        body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        request = new Request.Builder()
                .post(body)
                .url(url + "/animaldb/_find")
                .header("Authorization", credentials)
                .build();
        Response animalResponse = client.newCall(request).execute();
        var docs = GsonSingleton.getGson().fromJson(animalResponse.body().charStream(), JsonElement.class);
        System.out.println(docs);
    }

}
