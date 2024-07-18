package com.sophie;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;

public class FirebaseConfig {
    public static void initializeFirebase() throws Exception {
        //Get credentials from file
        InputStream serviceAccount = FirebaseConfig.class.getClassLoader().getResourceAsStream("service_account_key.json");

        assert serviceAccount != null;
        //Setup login configuration
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://sfap-co-op-calculator-default-rtdb.firebaseio.com/")
                .build();

        //Initialize database
        FirebaseApp.initializeApp(options);
    }

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }
}