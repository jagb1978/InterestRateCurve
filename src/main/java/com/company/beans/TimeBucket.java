package com.company.beans;

public class TimeBucket {
    private double minMaturity;
    private double maxMaturity;

    public TimeBucket(double minMaturity, double maxMaturity){
        this.minMaturity = minMaturity;
        this.maxMaturity = maxMaturity;
    }

    public double getMinMaturity() {
        return minMaturity;
    }

}
