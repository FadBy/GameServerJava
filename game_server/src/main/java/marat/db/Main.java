package marat.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        // String jsonString = "{\"id\":1,\"requestType\":\"LootPickUp\",\"authentication\":{\"nickname\":\"FadBy\"},\"body\":\"\"}";
        // System.out.println(jsonString);
        // Gson gson = new Gson();
        // ClientRequest clientRequest = gson.fromJson(jsonString, ClientRequest.class);

        // // Print the result
        // System.out.println(clientRequest);
        
         ServerApplication serverApplication = new ServerApplication();
         serverApplication.run();

//        TestDataBase testDataBase = new TestDataBase();
//        testDataBase.run();
    }
}