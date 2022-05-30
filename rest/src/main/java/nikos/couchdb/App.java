package nikos.couchdb;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        var client = new CouchDbClient("http://localhost:5984", "admin", "password");
        System.out.println(client.getServerInformation());

//        var response = client.putDatabase("testdatabase2");
//
//        System.out.println(response);
    }
}
