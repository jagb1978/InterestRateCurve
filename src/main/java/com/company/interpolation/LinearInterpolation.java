package com.company.interpolation;

import com.company.interfaces.Interpolation;
import com.company.beans.RatePoint;
import com.company.exceptions.InterpolationException;

/**
 * Linear Interpolation Class
 * It takes two rate point and interpolates the rates
 * values linearly between the two points.
 *
 * From the rate interpolation it can return the discount
 * factor for a specified maturity
 *
 * @author Jose Gonzalez
 */
public class LinearInterpolation implements Interpolation {
    private RatePoint startRatePoint;
    private RatePoint endRatePoint;
    private double timeDifference;
    private double rateDifference;
    private final double SECONDS_IN_YEAR = 525600;


    public LinearInterpolation(RatePoint startRatePoint, RatePoint endRatePoint) {
        this.startRatePoint = startRatePoint;
        this.endRatePoint = endRatePoint;
        this.timeDifference = endRatePoint.getYearsToMaturity() - startRatePoint.getYearsToMaturity();
        this.rateDifference = endRatePoint.getRate() - startRatePoint.getRate();
    }

    @Override
    public double getModeledRate(double maturity) throws InterpolationException {
        if(this.timeDifference!=0) {
            double differenceWithStartingTime = maturity - this.startRatePoint.getYearsToMaturity();
            return this.startRatePoint.getRate() + this.rateDifference * differenceWithStartingTime / this.timeDifference;
        }else{
            return this.startRatePoint.getRate();
        }
    }

    @Override
    public double getForwardRate(double maturity) throws InterpolationException {
        return this.getModeledRate(maturity) + ( this.getModeledRate(maturity+1/SECONDS_IN_YEAR) - this.getModeledRate(maturity) );
    }

    @Override
    public double getForwardRate(double startTerm, double endTerm) throws InterpolationException {
        double fwdValueEndTerm = Math.pow((1 + this.getModeledRate(endTerm)),endTerm);
        double fwdValueStartTerm = Math.pow((1 + this.getModeledRate(startTerm)),startTerm);
        return Math.pow(fwdValueEndTerm / fwdValueStartTerm, 1/(endTerm - startTerm)) - 1;
    }

    @Override
    public double getDiscountFactor(double maturity) throws InterpolationException {
        return 1/Math.pow((1+this.getModeledRate(maturity)),maturity);
    }

    @Override
    public double getLastTerm() {
        return 2.0;
    }


}
