package com.company.beans;

import com.company.enums.RateBasis;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Bean container
 * holds the information of rate points in the
 * curve
 *
 * @author Jose Gonzalez
 */
public class RatePoint {
    private double yearsToMaturity;
    private double rate;
    private static final double DAYS_IN_YEAR = 365.0;
    private LocalDate settle;
    private LocalDate maturity;
    private double capitalX = 0;

    public RatePoint(DataPoint dataPoint) {
        this.settle = dataPoint.getSettle();
        this.maturity = dataPoint.getMaturity();
        this.yearsToMaturity = DAYS.between(this.settle, this.maturity) / DAYS_IN_YEAR;
        this.rate = dataPoint.getRate();
    }

    public RatePoint(LocalDate settlement, LocalDate maturity, double rate) {
        this.settle = settlement;
        this.maturity = maturity;
        this.yearsToMaturity = DAYS.between(this.settle, this.maturity) / DAYS_IN_YEAR;
        this.rate = rate;
    }

    public RatePoint(double x, double y, RateBasis rateBasis) {
        this.yearsToMaturity = x;
        this.rate = rateBasis.equals(RateBasis.ANNUALIZED)? y: Math.pow( Math.exp(y*yearsToMaturity),1/yearsToMaturity)-1;
    }

    public double getYearsToMaturity() {
        return yearsToMaturity;
    }

    public double getRate() {
        return rate;
    }

    public double getContinousRate() {
        return Math.log(1 + rate) / this.yearsToMaturity;
    }

    public double getCapitalX() {
        return capitalX;
    }

    public void setCapitalX(double capitalX) {
        this.capitalX = capitalX;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RatePoint)) {
            return false;
        }

        RatePoint object = (RatePoint) o;

        return Math.abs(object.getYearsToMaturity() - this.yearsToMaturity) < 0.5 / 365;
    }

}
