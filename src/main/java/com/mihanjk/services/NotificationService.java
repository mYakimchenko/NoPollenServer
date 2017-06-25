package com.mihanjk.services;

import com.mihanjk.model.AllergenMoscow;
import com.mihanjk.model.AllergenNN;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class NotificationService {
    private static final String KEY = "key=AAAAI2MXccI:APA91bF7AR7SC9xtIVwBj_3Lh8vYNg1isizAj-DjgBp_3prO_WDPgD7WrXMd1ZQcFQ2Da9CSM-prkipPVcmhoyaAAwPe25JMlk9uoFEygPZSwM7tXhCUMiw4tpdQ9eeiv6bB69KOWfaa";
    private static HttpClient client = HttpClientBuilder.create().build();

    public static <T> String sendNotification(String city, List<T> data) {
        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Authorization", KEY);

        JSONObject notification = new JSONObject();
        notification.put("to", "/topics/" + city);
        if (city.equals(DatabaseService.MOSCOW_PATH_DATABASE)) {
            notification.put("data", getMoscowJson((List<AllergenMoscow>) data));
        } else {
            notification.put("data", getNNJson((List<AllergenNN>) data));
        }

        post.setEntity(new StringEntity(notification.toString(), "UTF-8"));

        try {
            HttpResponse response = client.execute(post);
            String responseData = EntityUtils.toString(response.getEntity());
            if (responseData.contains("message_id")) {
                return "Operation success" + responseData;
            } else {
                return "Operation failed cause " + responseData;
            }
        } catch (IOException e) {
            return "Problem with executing http request" + e.getMessage();
        }
    }

    private static JSONObject getMoscowJson(List<AllergenMoscow> data) {
        JSONObject result = new JSONObject();
        for (AllergenMoscow object : data) {
            result.append(object.getName(), object.getCurrentLevel());
        }
        return result;
    }

    private static JSONObject getNNJson(List<AllergenNN> data) {
        JSONObject result = new JSONObject();
        for (AllergenNN object : data) {
            // TODO: 6/25/2017 why value become an array?
            result.append(object.getName(), object.getCurrentLevel());
        }
        return result;
    }
}
