package nikos.couchdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.ibm.cloud.cloudant.v1.Cloudant;
import com.ibm.cloud.cloudant.v1.model.AllDocsResult;
import com.ibm.cloud.cloudant.v1.model.DesignDocument;
import com.ibm.cloud.cloudant.v1.model.DesignDocumentViewsMapReduce;
import com.ibm.cloud.cloudant.v1.model.Document;
import com.ibm.cloud.cloudant.v1.model.DocumentResult;
import com.ibm.cloud.cloudant.v1.model.FindResult;
import com.ibm.cloud.cloudant.v1.model.GetDocumentOptions;
import com.ibm.cloud.cloudant.v1.model.IndexDefinition;
import com.ibm.cloud.cloudant.v1.model.IndexField;
import com.ibm.cloud.cloudant.v1.model.IndexResult;
import com.ibm.cloud.cloudant.v1.model.PostAllDocsOptions;
import com.ibm.cloud.cloudant.v1.model.PostDocumentOptions;
import com.ibm.cloud.cloudant.v1.model.PostFindOptions;
import com.ibm.cloud.cloudant.v1.model.PostIndexOptions;
import com.ibm.cloud.cloudant.v1.model.PostViewOptions;
import com.ibm.cloud.cloudant.v1.model.PutDatabaseOptions;
import com.ibm.cloud.cloudant.v1.model.PutDesignDocumentOptions;
import com.ibm.cloud.cloudant.v1.model.ViewResult;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;

public class App {
    public static final String DBName = "animaldb";
    public static Cloudant client;

    public static void main(String[] args) {
        client = Cloudant.newInstance("CLOUDANT");

        var serverInformation = client
                .getServerInformation()
                .execute()
                .getResult();

        System.out.println("Server Version: " + serverInformation.getVersion());

        try {
            //createDatabase();
        } catch (ServiceResponseException e) {
            System.out.println("Database " + DBName + " already exists");
        }

//        var doc = new Document();
//        doc.put("Name", "Brown bear");
//        doc.put("Type", "Mammal");
//        doc.put("Size", "450cm");
//        doc.put("Weight", 700);
//        var result = createDoc(doc);

//        var result = getAllDocs();
//        var result = createIndex();
//        var result = findDoc("Dog");

//        var result = createMapDesignDocument();
        var result = query();

        System.out.println(result);
    }

    public static void createDatabase() {
        var putDatabaseOptions = new PutDatabaseOptions.Builder()
                .db(DBName)
                .build();

        var response = client.putDatabase(putDatabaseOptions) // PUT '/animaldb'
                .execute()
                .getResult();

        System.out.println("Put Database: " + response);
    }

    public static DocumentResult createDoc(Document document) {
        var postDocumentOptions = new PostDocumentOptions.Builder()
                .db(DBName)
                .document(document)
                .build();

        return client.postDocument(postDocumentOptions).execute().getResult();  // POST '/animaldb'
    }

    public static AllDocsResult getAllDocs() {
        var postDocumentOptions = new PostAllDocsOptions.Builder()
                .db(DBName)
                // .key("key")
                .includeDocs(true)
                .build();

        return client.postAllDocs(postDocumentOptions).execute().getResult(); // POST '/animaldb/_all_docs/'
    }

    public static Document getDoc(String id) {
        var options = new GetDocumentOptions.Builder()
                .db(DBName)
                .docId(id)
                .build();

        return client.getDocument(options)
                .execute()
                .getResult();
    }

    public static IndexResult createIndex() {
        var fields = new IndexField.Builder()
                .add("Name", "asc")
                .build();

        var index = new IndexDefinition.Builder()
                .addFields(fields)
                // .partialFilterSelector(selector)
                .build();

        var indexOptions = new PostIndexOptions.Builder()
                .db(DBName)
                .name("getAnimalByName")
                .ddoc("json-index")
                .type("json") // "json" or "text"
                .index(index)
                .build();

        return client.postIndex(indexOptions).execute().getResult(); // POST '/animaldb/_index'
    }

    public static FindResult findDoc(String animalName) {
        var expression = Collections.singletonMap("$eq", animalName);
        Map<String, Object> selector = Collections.singletonMap("Name", expression);

//        Map<String, Object> selector = Collections.singletonMap("$and", Arrays.asList(
//                Collections.singletonMap("Name", animalName),
//                Collections.singletonMap("Weight", Collections.singletonMap("$gte", 6))));

        var sort = Collections.singletonMap("Name", "asc");

        var findOptions = new PostFindOptions.Builder()
                .db(DBName)
                .selector(selector)
                .fields(Arrays.asList("_id", "Type", "Name", "Size", "Weight"))
                .addSort(sort)
                .build();

        return client.postFind(findOptions).execute().getResult(); // POST '/animaldb/_find'
    }

    public static DocumentResult createMapDesignDocument() {
        var animalsView = new DesignDocumentViewsMapReduce.Builder()
                .map("""
                        function(doc) {
                            if (doc.Weight && doc.Weight > 50) {
                                emit(doc.Name, doc.Weight);
                            }
                        }
                        """)
                .reduce("_count")
                .build();

        var designDocument = new DesignDocument();
        designDocument.setViews(Collections.singletonMap("getHeavyAnimals", animalsView));

        var designDocumentOptions = new PutDesignDocumentOptions.Builder()
                .db(DBName)
                .designDocument(designDocument)
                .ddoc("heavyanimals")
                .build();

        return client.putDesignDocument(designDocumentOptions).execute().getResult(); // PUT '/animaldb/_design/heavyanimals'
    }

    public static ViewResult query() {
        var viewOptions = new PostViewOptions.Builder()
                .db(DBName)
                .ddoc("heavyanimals")
                .view("getHeavyAnimals")
                .build();

        return client.postView(viewOptions).execute().getResult(); // POST '/animaldb/_design/heavyanimals/_view/getHeavyAnimals'
    }

}
