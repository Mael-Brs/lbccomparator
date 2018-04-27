package com.mbras.comparator.model;


import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Car {

    private String mileage;

    private String year;

    private String price;

    private String adUrl;

    private String title;

    private String estimatedPrice;

    private String priceGap;

    private String date;

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

    public String getEstimatedPrice() {
        return estimatedPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        if(this.price != null && estimatedPrice != null){
            this.estimatedPrice = estimatedPrice.toString();
            Double numericalGap = Double.parseDouble(this.price) - estimatedPrice;
            this.priceGap = numericalGap.toString();
        }
    }

    public String toCsvRow() {
        return Stream.of(adUrl, date, title, price, mileage, year, estimatedPrice, priceGap)
                .filter(Objects::nonNull)
                .map(value -> value.replaceAll("\"", "\"\""))
                .collect(Collectors.joining(";"));
    }
}
