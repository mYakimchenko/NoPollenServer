package com.mihanjk.services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.mihanjk.model.AllergenNN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseService {
    public static final String MOSCOW_PATH_DATABASE = "Moscow";
    public static final String NN_PATH_DATABASE = "NN";
    private static final String DATABASE_URL = "https://nopollen-24897.firebaseio.com/";
    private final InputStream pathToFirebaseJson;

    FirebaseDatabase database;

    @Autowired
    public DatabaseService() {
        pathToFirebaseJson = getClass().getClassLoader().getResourceAsStream("firebase.json");
        if (FirebaseApp.getApps().isEmpty()) {
            createDatabaseApp();
        }
    }

    // TODO: 6/11/2017 try move it into constructor
    void createDatabaseApp() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(pathToFirebaseJson))
                .setDatabaseUrl(DATABASE_URL)
                .build();

        FirebaseApp.initializeApp(options);

        database = FirebaseDatabase.getInstance();
    }

    public <T> void updateData(String type, String date, List<T> data, String city) {

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

    public void getDataForNotificationAndSendIt() {
        DatabaseReference ref = database.getReference(NN_PATH_DATABASE);
        List<AllergenNN> result = new ArrayList<>();

        final Query query = ref.orderByKey().limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot date : dataSnapshot.getChildren()) {
                    for (DataSnapshot group : date.getChildren()) {
                        for (DataSnapshot allergen : group.getChildren()) {
                            AllergenNN value = allergen.getValue(AllergenNN.class);
                            result.add(value);
                        }
                    }
                }
                NotificationService.sendNotification(DatabaseService.NN_PATH_DATABASE, result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError.getMessage());
            }
        });
    }

    public void getDateOfLastRecordNN(PollenActivityService service) {
        database.getReference(NN_PATH_DATABASE).orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    try {
                        service.setLastDateFromDatabase(dateSnapshot.getKey());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println(databaseError.getMessage());
            }
        });
    }
}
