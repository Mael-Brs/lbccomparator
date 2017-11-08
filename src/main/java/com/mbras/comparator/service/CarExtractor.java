package com.mbras.comparator.service;

import com.mbras.comparator.model.Car;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.simple.SimpleLoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CarExtractor {

    private static final Logger LOGGER = new SimpleLoggerFactory().getLogger(CarExtractor.class.getName());
    private static final Pattern pricePattern = Pattern.compile("(prix.*:.*\")(.*)(\",)");
    private static final Pattern kmPattern = Pattern.compile("(km.*:.*\")(.*)(\",)");
    private static final Pattern yearPattern = Pattern.compile("(annee.*:.*\")(.*)(\",)");
    private static final String FILE_NAME = "cars.csv";

    public void extractData(String url) throws IOException{
        Proxy proxy = getProxy();
        List<Car> cars = new ArrayList<>();
        Document doc = Jsoup.connect(url).proxy(proxy).get();
        Elements links = doc.select(".tabsContent .list_item");

        for(Element element : links){
            Car car = new Car();
            car.setAdUrl(element.attr("abs:href"));
            car.setTitle(element.attr("title"));
            Document carAd = Jsoup.connect(car.getAdUrl()).proxy(proxy).get();

            Elements scripts = carAd.body().getElementsByTag("script");
            if(scripts.isEmpty()){
                return;
            }
            Element script = scripts.get(3);
            String scriptContent = script.html();

            Matcher priceMatcher  = pricePattern.matcher(scriptContent);
            if (priceMatcher.find()) {
                car.setPrice(priceMatcher.group(2));
            }
            Matcher mileageMatcher = kmPattern.matcher(scriptContent);
            if (mileageMatcher.find()) {
                car.setMileage(mileageMatcher.group(2));
            }
            Matcher yearMatcher = yearPattern.matcher(scriptContent);
            if (yearMatcher.find()) {
                car.setYear(yearMatcher.group(2));
            }
            cars.add(car);
        }

        String recordAsCsv = cars.stream()
                .map(Car::toCsvRow)
                .collect(Collectors.joining(System.getProperty("line.separator")));

        try (FileWriter writer = new FileWriter(FILE_NAME)){
            writer.write(recordAsCsv);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Error while creating csv file", e);
        }

    }

    private Proxy getProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if(!StringUtil.isBlank(proxyHost) && !StringUtil.isBlank(proxyPort)){
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
        } else {
            return Proxy.NO_PROXY;
        }
    }

}
