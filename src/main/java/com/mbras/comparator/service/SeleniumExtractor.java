package com.mbras.comparator.service;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class SeleniumExtractor {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "./geckodriver.exe");
        WebDriver driver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        try {
            driver.get("https://www.leboncoin.fr/recherche/?category=2&text=136h&regions=6&owner_type=pro&model=Auris&brand=Toyota&regdate=2013-max");
            List<WebElement> elements = driver.findElements(By.cssSelector("._3DFQ- a"));
            elements.get(0).getAttribute("href");
            //WebElement firstResult = wait.until(presenceOfElementLocated(By.cssSelector("a ._3DFQ-")));
            System.out.println(elements.size());
        } finally {
            driver.quit();
        }
    }
}
