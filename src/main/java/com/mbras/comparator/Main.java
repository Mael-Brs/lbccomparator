package com.mbras.comparator;

import com.mbras.comparator.service.CarExtractor;
import org.slf4j.Logger;
import org.slf4j.simple.SimpleLoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final Logger LOGGER = new SimpleLoggerFactory().getLogger(Main.class.getName());

    public static void main(String[] args) {
        CarExtractor carExtractor = new CarExtractor();
        try {
            carExtractor.extractData(getInputUrl());
        } catch (IOException e) {
            LOGGER.error("Error while extracting car data", e);
        }
    }

    private static String getInputUrl() throws IOException{

        String url = null;

        //to load application's properties, we use this class
        Properties mainProperties = new Properties();

        FileInputStream file;

        //the base folder is ./, the root of the application.properties file
        String path = "./application.properties";

        //load the file handle for main.properties
        file = new FileInputStream(path);

        //load all the properties from this file
        mainProperties.load(file);

        //we have loaded the properties, so close the file handle
        file.close();

        //retrieve the property we are intrested, the app.version
        url = mainProperties.getProperty("lbc.url");

        return url;
    }
}
