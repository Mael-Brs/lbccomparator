package com.mbras.comparator.service;

import com.mbras.comparator.model.Car;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.List;

public class SeleniumExtractor {

    private WebDriver driver;
    private static final int PAGE_LIMIT = 5;
    private boolean initialized = false;

    public SeleniumExtractor() {
        System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");
    }

    public void init(){
        initialized = true;
        driver = new FirefoxDriver();
    }

    public List<String> getLinks(String searchUrl, List<String> links, int pageNumber) {
        if(!initialized){
            return new ArrayList<>();
        }
        //Get web page
        driver.get(searchUrl);
        //find elements
        List<WebElement> ads = driver.findElements(By.cssSelector("._3DFQ- a"));
        //Put href in list of string
        for (WebElement element : ads) {
            String href = element.getAttribute("href");
            links.add(href);
        }
        List<WebElement> pageButtons = driver.findElements(By.cssSelector("._1f-eo"));

        WebElement next = null;
        if (!pageButtons.isEmpty()) {
            next = pageButtons.get(pageButtons.size() - 1);
        }

        if(next != null && pageNumber < PAGE_LIMIT){
            getLinks(next.getAttribute("href"), links, pageNumber);
        }

        return links;
    }

    public Car extractCarData(String href) {
        if(!initialized){
            return null;
        }
        driver.get(href);
        Car car = new Car();
        car.setAdUrl(href);

        String price = driver.findElement(By.cssSelector(".eVLNz ._386c2 ._1F5u3")).getText();
        price = price.replaceAll("\\s+", "");
        price = price.replaceAll("€", "");
        car.setPrice(price);

        List<WebElement> dataContainer = driver.findElements(By.cssSelector("._2B0Bw._1nLtd ._3Jxf3"));
        car.setYear(dataContainer.get(2).getText());
        car.setMileage(dataContainer.get(3).getText().replaceAll("\\skm", ""));

        car.setTitle(driver.findElement(By.cssSelector("._1KQme")).getText());
        car.setTitle(driver.findElement(By.cssSelector("[data-qa-id=\"adview_date\"]")).getText());
        return car;
    }

    public void closeDriver(){
        if (driver != null) {
            driver.quit();
            initialized = false;
        }
    }

}
