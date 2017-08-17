package com.mihanjk.services;

import com.mihanjk.model.AllergenMoscow;
import com.mihanjk.model.AllergenNN;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PollenActivityService {
    private static final String TODAY = "сегодня";
    private static final String YESTERDAY = "вчера";
    // TODO: 6/11/2017 maybe better use hashMap here?
    private final ZoneId moscowZone = ZoneId.of("Europe/Moscow");

    private static final String SITE_TREE = "derevev";
    private static final String SIRE_CEREAL = "zlakov";
    private static final String SITE_WEED = "sornykh-trav";
    private static final String SITE_SPORE = "sporonosheniya";

    private static final String TREE = "Tree";
    private static final String CEREAL = "Cereal";
    private static final String WEED = "Weed";
    private static final String SPORE = "Spore";

    private static final String NOTHING = "Nothing";
    private static final String LOW = "Low";
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String EXTRA_HIGH = "Extra high";

    private static final String PREFIX_MOSCOW_SITE = "http://allergotop.com/pyltsevoj-monitoring/prognoz-urovnya-";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(new Locale("ru", "RU"));

    private static final List<String> types = new ArrayList<>();
    private static final List<AllergenNN> cerealList = new ArrayList<>();
    private static final List<AllergenNN> weedList = new ArrayList<>();
    private static final List<AllergenNN> treeList = new ArrayList<>();
    private static final List<AllergenNN> sporeList = new ArrayList<>();
    private static final Map<String, List<AllergenNN>> forecastNNTemplate = new HashMap<>();

    static {
        types.add(SITE_TREE);
        types.add(SIRE_CEREAL);
        types.add(SITE_WEED);
        types.add(SITE_SPORE);

        String defaultLevel = NOTHING;
        cerealList.add(new AllergenNN("Общий фон", defaultLevel, 0));

        treeList.add(new AllergenNN("Ольха", defaultLevel, 0));
        treeList.add(new AllergenNN("Тополь", defaultLevel, 0));
        treeList.add(new AllergenNN("Орешник", defaultLevel, 0));
        treeList.add(new AllergenNN("Берёза", defaultLevel, 0));
        treeList.add(new AllergenNN("Сосна", defaultLevel, 0));
        treeList.add(new AllergenNN("Ель", defaultLevel, 0));
        treeList.add(new AllergenNN("Вяз", defaultLevel, 0));
        treeList.add(new AllergenNN("Липа", defaultLevel, 0));
        treeList.add(new AllergenNN("Клён", defaultLevel, 0));
        treeList.add(new AllergenNN("Ясень", defaultLevel, 0));
        treeList.add(new AllergenNN("Ива", defaultLevel, 0));
        treeList.add(new AllergenNN("Дуб", defaultLevel, 0));

        weedList.add(new AllergenNN("Полынь", defaultLevel, 0));
        weedList.add(new AllergenNN("Крапива", defaultLevel, 0));
        weedList.add(new AllergenNN("Подорожник", defaultLevel, 0));
        weedList.add(new AllergenNN("Щавель", defaultLevel, 0));
        weedList.add(new AllergenNN("Амброзия", defaultLevel, 0));
        weedList.add(new AllergenNN("Маревые", defaultLevel, 0));

        sporeList.add(new AllergenNN("Кладоспориум", defaultLevel, 0));
        sporeList.add(new AllergenNN("Альтернария", defaultLevel, 0));

        forecastNNTemplate.put(CEREAL, cerealList);
        forecastNNTemplate.put(TREE, treeList);
        forecastNNTemplate.put(SPORE, sporeList);
        forecastNNTemplate.put(WEED, weedList);
    }

    private final String NNSite;
    private String lastDateOfPollenForecastNN;
    private EmailService emailService;
    private Map<String, List<AllergenMoscow>> ForecastMoscow;
    private Map<String, Document> documents;
    private DatabaseService databaseService;

    @Autowired
    public PollenActivityService(EmailService emailService, DatabaseService databaseService) throws UnsupportedEncodingException {
        this.emailService = emailService;
        this.databaseService = databaseService;
        NNSite = URLDecoder.decode("http://nika-nn.ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8?page=0", "UTF-8");
        dateFormatSymbols.setMonths(new String[]{
                "Января",
                "Февраля",
                "Марта",
                "Апреля",
                "Мая",
                "Июня",
                "Июля",
                "Августа",
                "Сентября",
                "Октября",
                "Ноября",
                "Декабря"
        });
    }

    public Map<String, List<AllergenMoscow>> getForecastMoscow() throws Exception {
        documents = getDocuments();
        ForecastMoscow = parseForecastData();
        String date = getDateOfForecast(documents.get(SITE_TREE));

        ForecastMoscow.forEach((typeOfForecast, forecasts) ->
                databaseService.updateData(typeOfForecast, date, forecasts, DatabaseService.MOSCOW));

        return ForecastMoscow;
    }

    private Map<String, Document> getDocuments() throws Exception {
        Map<String, Document> docs = new HashMap<>();

        types.forEach(typeOfPollen -> {
            try {
                docs.put(typeOfPollen, Jsoup.connect(PREFIX_MOSCOW_SITE + getSuffixOfURL(typeOfPollen)).get());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        return docs;
    }

    private String getSuffixOfURL(String typeOfPollen) {
        return typeOfPollen.equals(SITE_SPORE) ? typeOfPollen : "pyleniya-" + typeOfPollen;
    }

    Map<String, List<AllergenMoscow>> parseForecastData() {
        // need view html code to understand this cycle
        Map<String, List<AllergenMoscow>> data = new HashMap<>();
        documents.forEach((key, value) -> {
            List<AllergenMoscow> listOfAllergenMoscow = new ArrayList<>();
            Element table = value.getElementsByTag("table").get(0);
            Elements rows = table.getElementsByTag("tr");
            for (Element row : rows) {
                Elements columns = row.select("td[align='center']");
                for (Element column : columns) {
                    Elements divs = column.getElementsByTag("div");
                    if (!divs.isEmpty()) {
                        String name = formatString(divs.get(2).text());
                        String currentLevel = divs.get(4).getElementsByTag("img").attr("src");
                        String tomorrowLevel = divs.last().getElementsByTag("img").attr("src");
                        listOfAllergenMoscow.add(new AllergenMoscow(name, getForecastLevel(currentLevel), getForecastLevel(tomorrowLevel)));
                    }
                }
                data.put(getCategoryName(key), listOfAllergenMoscow);
            }
        });
        return data;
    }

    // TODO: 6/11/2017 maybe need create utility class for this?

    String formatString(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    String getForecastLevel(String input) {
        String output;
        switch (input) {
            case "/images/pm/00.png":
                output = NOTHING;
                break;
            case "/images/pm/01.png":
                output = LOW;
                break;
            case "/images/pm/02.png":
                output = MEDIUM;
                break;
            case "/images/pm/03.png":
                output = HIGH;
                break;
            case "/images/pm/04.png":
                output = EXTRA_HIGH;
                break;
            default:
                throw new IllegalArgumentException("Unexpected value");
        }
        return output;
    }

    String getCategoryName(String input) {
        // TODO: 6/11/2017 how can i avoid code duplication?
        String output;
        switch (input) {
            case SITE_TREE:
                output = TREE;
                break;
            case SIRE_CEREAL:
                output = CEREAL;
                break;
            case SITE_WEED:
                output = WEED;
                break;
            case SITE_SPORE:
                output = SPORE;
                break;
            default:
                throw new IllegalArgumentException("Unexpected value");
        }
        return output;
    }

    String getDateOfForecast(Document doc) throws ParseException {
        // Дата обновления информации: 28 Июля 2017 -> 2017-07-28
        // Дата обновления информации: сегодня, 13:26 -> 2017-07-28
        // Дата обновления информации: вчера, 13:26 -> 2017-07-28
        String dataOfForecast = doc.getElementsByClass("col-xs-12 col-sm-12 col-md-12 col-lg-12 text-center")
                .get(0).getElementsByTag("p").get(0).text().toLowerCase();
        int beginIndex = dataOfForecast.indexOf(':') + 2;

        if (dataOfForecast.contains(TODAY)) {
            return dateFormatter.format(LocalDate.now(moscowZone));
        }

        // TODO: 7/27/2017 test it
        if (dataOfForecast.contains(YESTERDAY)) {
            return dateFormatter.format(LocalDate.now(moscowZone).minusDays(1));
        }

        // TODO: 7/31/2017 test it
        String date = dataOfForecast.substring(beginIndex).trim();
        Date parse = new SimpleDateFormat("dd MMMMM yyyy", dateFormatSymbols).parse(date);
        return new SimpleDateFormat("yyyy-MM-dd").format(parse);
    }


    // TODO: 6/11/2017 maybe need divide into two classes for every city?

    public void receiveRequestNN() throws IOException {
        if (lastDateOfPollenForecastNN == null) {
            getLastDateFromDatabase();
        } else {
            createTemplate(getNewForecastsDate());
        }
    }

    private void createTemplate(List<String> forecastDates) {
        if (!forecastDates.isEmpty()) {
            for (String date : forecastDates) {
                forecastNNTemplate.forEach((category, forecasts) ->
                        databaseService.updateData(category, date, forecasts, DatabaseService.NN));

                emailService.sendMailNotification("mihanjk@mail.ru",
                        "Need to update firebase database from news nika.nn " + date,
                        "Get data from link:\n" +
                                "http://nika-nn.ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8\n" +
                                "Insert data into database: \n" +
                                "https://console.firebase.google.com/u/0/project/nopollen-24897/database/data\n" +
                                "Then send notification to devices from link:\n" +
                                "188.166.74.55:8080/sendNotificationNN");
            }
            lastDateOfPollenForecastNN = forecastDates.get(0);
        }
    }

    // TODO: 6/11/2017 find news pollen

    private List<String> getNewForecastsDate() throws IOException {
        Elements newsTitles = Jsoup.connect(NNSite).get().getElementsByClass("last-news-list-item_title");
        List<String> result = new ArrayList<>();

        // TODO: 6/11/2017 refactoring
        for (Element titleElement : newsTitles) {
            String title = titleElement.getElementsByTag("a").text();

            if (title.toUpperCase().contains("ПЫЛЬЦЕВОЙ МОНИТОРИНГ")) {
                String dateFromTitle = getDateFromTitle(title);
                if (!dateFromTitle.equals(lastDateOfPollenForecastNN)) {
                    result.add(dateFromTitle);
                } else {
                    // TODO: 6/11/2017 develop if all news from page not equals to last page, take next page
                    break;
                }
            }
        }
        return result;
    }

    private String getDateFromTitle(String title) {
        String notFormatDate;
        //Пыльцевой мониторинг от 15.06.17 г.
        if (title.contains("Г.")) {
            notFormatDate = title.substring(title.length() - 11, title.length() - 3);
        } else {
            //ПЫЛЬЦЕВОЙ МОНИТОРИНГ ОТ 30.05.17 -> 2017-05-30
            notFormatDate = title.substring(title.length() - 8);
        }

        return "20" + notFormatDate.substring(notFormatDate.length() - 2) + "-"
                + notFormatDate.substring(3, 5) + "-" + notFormatDate.substring(0, 2);
    }

    private void getLastDateFromDatabase() {
        databaseService.getDateOfLastRecordNN(this);
    }

    void setLastDateFromDatabase(String date) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Date not found in database. Can't detect breakpoint for algorithm");
        }
        lastDateOfPollenForecastNN = date;
        createTemplate(getNewForecastsDate());
    }
}
