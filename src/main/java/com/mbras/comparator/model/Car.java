package com.mbras.comparator.model;


import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Car {
    String mileage;
    String year;
    String price;
    String adUrl;
    String title;

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAdUrl() {
        return adUrl;
    }

    public void setAdUrl(String adUrl) {
        this.adUrl = adUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toCsvRow() {
        return Stream.of(adUrl, title, price, mileage, year)
                .map(value -> value.replaceAll("\"", "\"\""))
                .collect(Collectors.joining(";"));
    }
}
