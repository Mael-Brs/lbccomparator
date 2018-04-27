package com.mbras.comparator;

import com.mbras.comparator.service.CarExtractor;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.simple.SimpleLoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Properties;

public class Main {

    private static final Logger LOGGER = new SimpleLoggerFactory().getLogger(Main.class.getName());

    public static void main(String[] args) {
        CarExtractor carExtractor = new CarExtractor();
        try {
            Properties properties = getProperties();
            carExtractor.buildPriceModel(properties.getProperty("lbc.url"), getProxy(properties));
            //carExtractor.estimateCars(properties.getProperty("lbc.url"), getProxy(properties));
        } catch (IOException e) {
            LOGGER.error("Error while extracting car data", e);
        }
    }

    private static Properties getProperties() throws IOException {
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
        return mainProperties;
    }

    private static Proxy getProxy(Properties properties) {
        String proxyHost = properties.getProperty("http.proxyHost");
        String proxyPort = properties.getProperty("http.proxyPort");
        if(!StringUtil.isBlank(proxyHost) && !StringUtil.isBlank(proxyPort)){
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
        } else {
            return Proxy.NO_PROXY;
        }
    }
}
