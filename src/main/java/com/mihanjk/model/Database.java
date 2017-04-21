package com.mihanjk.model;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;

import java.io.InputStream;
import java.util.List;

public class Database {
    private static final String DATABASE_URL = "https://nopollen-24897.firebaseio.com/";
    private static final String rootPathDatabase = "server/forecast/";
    private final InputStream pathToFirebaseJson;

    public Database(InputStream path) {
        pathToFirebaseJson = path;
    }

    void createDatabaseApp() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(pathToFirebaseJson))
                .setDatabaseUrl(DATABASE_URL)
                .build();

        FirebaseApp.initializeApp(options);
    }

    public void updateData(String type, String date, List<Forecast> data) {
        if (FirebaseApp.getApps().size() == 0) {
            createDatabaseApp();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(rootPathDatabase).child(date).child(type);
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
