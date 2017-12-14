package com.company.beans;

import com.company.Utils.MathUtils;
import com.company.enums.RateType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class holds the information of the
 * different points in an interest rate curve.
 * It is a wrapper class for a list of rate
 * points
 *
 * @author Jose Gonzalez
 */
public class RatesCurve {
    /** List with rate points in the curve*/
    private List<RatePoint> ratePointsList = new ArrayList<>();
    private Double cashFlowYearlyFrequency;

    public RatesCurve(){}

    public RatesCurve(List<DataPoint> dataPointList, RateType rateType){
        this.addDataPointList(dataPointList,rateType);
    }

    public void add(RatePoint ratePoint){
        int numberOfDiscretePoints= this.ratePointsList.size();

        if( numberOfDiscretePoints>0){ //firstly sets capitalX in the discrete rate point
            double tempCapitalX = ratePoint.getYearsToMaturity()-this.ratePointsList.get( numberOfDiscretePoints-1).getYearsToMaturity();
            this.ratePointsList.get( numberOfDiscretePoints-1).setCapitalX(tempCapitalX);
        }
        this.ratePointsList.add(ratePoint);
        this.ratePointsList.sort(Comparator.comparing(rate->rate.getYearsToMaturity()));
    }

    public Double getCashFlowYearlyFrequency() {
        return cashFlowYearlyFrequency;
    }

    public double lastTermInCurve(){
        this.ratePointsList.sort(Comparator.comparing(ratePoint->ratePoint.getYearsToMaturity()));
        return this.ratePointsList.get(this.ratePointsList.size()-1).getYearsToMaturity();
    }

    public double firstTermInCurve(){
        this.ratePointsList.sort(Comparator.comparing(ratePoint->ratePoint.getYearsToMaturity()));
        return this.ratePointsList.get(0).getYearsToMaturity();
    }

    private void addDataPointList(List<DataPoint> dataPointList, RateType rateType){
        dataPointList.stream().filter(dataPoint -> dataPoint.getType().equals(rateType))
                .forEach(dataPoint -> {
                    RatePoint ratePoint = new RatePoint(dataPoint);
                    if( !this.ratePointsList.contains(ratePoint) ){
                        this.add(ratePoint);
                    }
                });

        if(rateType.equals(RateType.SWAP)) {
            this.cashFlowYearlyFrequency = dataPointList.stream().filter(dataPoint -> dataPoint.getType().equals(rateType)).findFirst().get().getFrequency();
        }
    }

    public double getMaturity(int index){
        return this.ratePointsList.get(index).getYearsToMaturity();
    }

    public double getRate(int index) {
        return this.ratePointsList.get(index).getRate();
    }

    public double getContinousRate(int index) {
        return this.ratePointsList.get(index).getContinousRate();
    }

    public double getCapitalX(int index){
        return this.ratePointsList.get(index).getCapitalX();
    }

    public int size(){
        return this.ratePointsList.size();
    }

    public void setCashFlowYearlyFrequency(Double cashFlowYearlyFrequency) {
        this.cashFlowYearlyFrequency = cashFlowYearlyFrequency;
    }

    public boolean negativeRatesPresent(){
       return this.ratePointsList.stream().anyMatch(ratePoint -> ratePoint.getRate()<0);
    }

    public void setRate(double rate, int index){
        this.ratePointsList.get(index).setRate(rate);
    }

    public boolean termZeroNotPresent(){
       return this.ratePointsList.stream().anyMatch(ratePoint -> MathUtils.almostZero(ratePoint.getYearsToMaturity()));
    }

    public List<RatePoint> getRatePointsList() {
        return ratePointsList;
    }


}
