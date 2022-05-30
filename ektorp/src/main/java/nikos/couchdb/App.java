package nikos.couchdb;

import org.ektorp.ViewQuery;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import java.net.MalformedURLException;

public class App
{
    public static void main(String[] args) {
        HttpClient client;

        try {
            client = new StdHttpClient.Builder()
                    .url("http://localhost:5984")
                    .username("admin")
                    .password("password")
                    .build();
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
            return;
        }

        var dbInstance = new StdCouchDbInstance(client);
        var db = dbInstance.createConnector("animaldb", true);

//        Map<String, Object> doc = new HashMap<String, Object>();
//        doc.put("_id", "reference");
//        doc.put("Name", "Panda");
//        doc.put("Size", "90cm");
//        doc.put("Weight", 100);
//        db.create(doc);

//        Map<String, Object> result = db.get(Map.class, "reference");

//        var viewQuery = new ViewQuery();
//        viewQuery.allDocs().includeDocs(true);
//        var result = db.queryView(viewQuery);

        var viewQuery = new ViewQuery()
                .viewName("getHeavyAnimals")
                .designDocId("_design/heavyanimals");
        var result = db.queryView(viewQuery);

        System.out.println(result);
    }
}
