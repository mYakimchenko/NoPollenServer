package com.mihanjk.model;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.mihanjk.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Database {
    public static final String MOSCOW_PATH_DATABASE = "moscow";
    public static final String NN_PATH_DATABASE = "NN";
    private static final String DATABASE_URL = "https://nopollen-24897.firebaseio.com/";
    private final InputStream pathToFirebaseJson;

    @Autowired
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

    public <T> void updateData(String type, String date, List<T> data, String city) {
        if (FirebaseApp.getApps().isEmpty()) {
            createDatabaseApp();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(city).child(date).child(type);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    ref.setValue(data);
                    if (city.equals(MOSCOW_PATH_DATABASE)) {
                        NotificationService.sendNotification(city, data);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError.getMessage());
            }
        });
    }

    public List<ForecastNN> getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(NN_PATH_DATABASE);
        List<ForecastNN> result = new ArrayList<>();

        final Query query = ref.orderByKey().limitToLast(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot date : dataSnapshot.getChildren()) {
                    for (DataSnapshot group : date.getChildren()) {
                        for (DataSnapshot allergen : group.getChildren()) {
                            ForecastNN value = allergen.getValue(ForecastNN.class);
                            result.add(value);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError.getMessage());
            }
        });

        return result;
    }
}
