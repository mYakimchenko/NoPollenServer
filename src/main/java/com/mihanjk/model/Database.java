package com.mihanjk.model;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;

public class Database {
    private static final String DATABASE_URL = "https://nopollen-24897.firebaseio.com/";
    private static final String rootPathDatabase = "server/forecast/";
    private final URI pathToFirebaseJson;

    public Database(URI path) {
        pathToFirebaseJson = path;
    }

    void createDatabaseApp() {
        try {
            FileInputStream serviceAccount = new FileInputStream(new File(pathToFirebaseJson));

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateData(String type, String date, List<Forecast> data) {
        // Create new FirebaseApp instance if it not exists
        if (FirebaseApp.getApps().size() == 0) {
            createDatabaseApp();
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //TODO add type of data for database
        DatabaseReference ref = database.getReference(rootPathDatabase).child(date).child(type);
        //TODO add check if data already exists in database
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    ref.setValue(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
