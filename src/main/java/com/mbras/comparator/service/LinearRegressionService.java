package com.mbras.comparator.service;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class LinearRegressionService {

    public static final String CSV_SPLIT = ";";

    private SimpleRegression simpleRegression = new SimpleRegression();

    public void addTrainData(double [][] data){
        simpleRegression.addData(data);
    }

    public String getModelAsCsvString(){
        return  "a" + CSV_SPLIT + "b" + System.getProperty("line.separator") + simpleRegression.getSlope() + CSV_SPLIT + simpleRegression.getIntercept();
    }


}
