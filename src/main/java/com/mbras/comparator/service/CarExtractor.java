package com.mbras.comparator.service;

import com.mbras.comparator.model.Car;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.simple.SimpleLoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mbras.comparator.service.LinearRegressionService.CSV_SPLIT;

public class CarExtractor {

    private static final Logger LOGGER = new SimpleLoggerFactory().getLogger(CarExtractor.class.getName());
    private static final Pattern pricePattern = Pattern.compile("(\"price\":\\[)(\\d+)(])");
    private static final Pattern kmPattern = Pattern.compile("(\"mileage\",\"value\":\")(\\d+)");
    private static final Pattern yearPattern = Pattern.compile("(\"regdate\",\"value\":\")(\\d+)");
    private static final Pattern datePattern = Pattern.compile("(\"first_publication_date\":\")([^\"]+)(\")");
    private static final String RESULTS_CSV = "./estimate/results.csv";
    private static final String TRAINING_DATA_CSV = "./model/trainingData.csv";
    private static final String LINEAR_REGRESSION_MODEL_CSV = "./model/linearRegressionModel.csv";
    private static final String DELIMITER = System.getProperty("line.separator");
    private static final int PAGE_LIMIT = 5;

    private LinearRegressionService linearRegressionService = new LinearRegressionService();

    private Double slope;

    private Double intercept;


    /**
     * Extrait les annonces d'une recherche leboncoin et détermine une relation linéaire prix/km
     * @param url Adresse de la recherche leboncoin
     * @param proxy Proxy à utiliser pour la connection
     * @throws IOException Erreur d'enregistrement des données
     */
    public void buildPriceModel(String url, Proxy proxy) throws IOException {
        this.extractData(url, proxy, true);
    }

    /**
     * Extrait les annonces d'une recherche leboncoin et estime le prix théorique
     * @param url Adresse de la recherche leboncoin
     * @param proxy Proxy à utiliser pour la connection
     * @throws IOException Erreur d'enregistrement des données
     */
    public void estimateCars(String url, Proxy proxy) throws IOException {
        //Chargement du modèle de regression linéaire si il a été créé précédement
        List<String[]> datas = this.loadCsvFile(LINEAR_REGRESSION_MODEL_CSV);
        if(datas.size() == 2){
            Map<String, String> regressionModelMap = new HashMap<>();
            regressionModelMap.put(datas.get(0)[0], datas.get(1)[0]);
            regressionModelMap.put(datas.get(0)[1], datas.get(1)[1]);
            this.slope = Double.parseDouble(regressionModelMap.get("a"));
            this.intercept = Double.parseDouble(regressionModelMap.get("b"));
        }
        this.extractData(url, proxy, false);
    }

    /**
     * Extrait les données d'offre de voitures leboncoin
     * @param url adresse de la recherche
     * @param proxy Proxy à utiliser pour la connection
     * @param isForTraining true si les données seront utilisées pour calculer le modèle de regression
     * @throws IOException Erreur d'enregistrement des données
     */
    private void extractData(String url, Proxy proxy, boolean isForTraining) throws IOException {
        boolean hasModel = this.slope != null && this.intercept != null;

        int pageNumber = 0;
        Elements links = extractCarAddLinks(url, proxy, pageNumber, new Elements());

        double [][] modeldata = new double[links.size()][2];

        List<Car> cars = new ArrayList<>();
        for(int i = 0; i < links.size(); i++) {
            Car car = extractCarData(proxy, links.get(i));
            if (car == null) {
                return;
            }
            cars.add(car);

            if(isForTraining) {
                modeldata[i][0] = Integer.parseInt(car.getMileage());
                modeldata[i][1] = Integer.parseInt(car.getPrice());
            } else if(hasModel) {
                car.setEstimatedPrice(Integer.parseInt(car.getMileage()) * slope + intercept);
            }
        }

        if(isForTraining){
            linearRegressionService.addTrainData(modeldata);
            writeToFile(linearRegressionService.getModelAsCsvString(), LINEAR_REGRESSION_MODEL_CSV, false);
        }

        String recordAsCsv = cars.stream()
                .map(Car::toCsvRow)
                .collect(Collectors.joining(DELIMITER)) + DELIMITER;

        String fileName = isForTraining ? TRAINING_DATA_CSV : RESULTS_CSV;
        writeToFile(recordAsCsv, fileName, true);
    }

    /**
     * Extrait les urls des annonces de la liste des annonces d'une page données
     * @param url Url de la liste des annonces
     * @param proxy Proxy à utiliser
     * @param pageNumber Numéro de la page de la liste des annonces
     * @param links Liste des urls des annonces
     * @return La liste des liens mise à jour
     * @throws IOException problème de connection à la page
     */
    private Elements extractCarAddLinks(String url, Proxy proxy, int pageNumber, Elements links) throws IOException {
        Document doc = Jsoup.connect(url).proxy(proxy).get();
        links.addAll(doc.select(".tabsContent .list_item"));
        pageNumber ++;
        Element next = doc.getElementById("next");
        if(next != null && pageNumber < PAGE_LIMIT){
            extractCarAddLinks(next.attr("abs:href"), proxy, pageNumber, links);
        }
        return links;
    }

    /**
     * Extrait les données des annonces de voiture
     * @param proxy Proxy à utiliser
     * @param element Balise html contenant le lien de l'annonce
     * @return Les données de la voiture
     * @throws IOException Problème de connection à l'annonce
     */
    private Car extractCarData(Proxy proxy, Element element) throws IOException {
        Car car = new Car();
        car.setAdUrl(element.attr("abs:href"));
        car.setTitle(element.attr("title"));
        Document carAd = Jsoup.connect(car.getAdUrl()).proxy(proxy).get();

        Elements scripts = carAd.body().getElementsByTag("script");
        if (scripts.isEmpty()) {
            return null;
        }
        Element script = scripts.get(3);
        String scriptContent = script.html();

        Matcher priceMatcher = pricePattern.matcher(scriptContent);
        if (priceMatcher.find()) {
            String priceString = priceMatcher.group(2);
            car.setPrice(priceString);
        }

        Matcher mileageMatcher = kmPattern.matcher(scriptContent);
        if (mileageMatcher.find()) {
            String mileageString = mileageMatcher.group(2);
            car.setMileage(mileageString);
        }

        Matcher yearMatcher = yearPattern.matcher(scriptContent);
        if (yearMatcher.find()) {
            car.setYear(yearMatcher.group(2));
        }

        Matcher dateMatcher = datePattern.matcher(scriptContent);
        if (dateMatcher.find()) {
            car.setDate(dateMatcher.group(2));
        }
        return car;
    }

    /**
     * Ecrit les données dans un fichier csv
     * @param string données au format csv
     * @param fileName nom du fichier à créer
     * @param append true ajoute à la fin du fichier false écrase le contenu
     */
    private void writeToFile(String string, String fileName, boolean append) {
        try (FileWriter writer = new FileWriter(fileName, append)){
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Error while creating csv file", e);
        }
    }

    /**
     * Charge les données d'un fichier csv
     * @param path chemin du fichier à charger
     * @return Tableau contenant les données du fichier csv
     */
    private List <String[]>  loadCsvFile(String path) {
        String line;
        List <String[]> datas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                String [] cells = line.split(CSV_SPLIT);
                datas.add(cells);
            }

        } catch (IOException e) {
            LOGGER.error("error while reading file", e);
        }
        return datas;
    }

}
