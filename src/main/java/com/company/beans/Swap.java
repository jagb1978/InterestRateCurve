package com.company.beans;

import com.company.interfaces.Interpolation;
import com.company.exceptions.InterpolationException;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Class holding the swap information.
 * It performs the bootstrapping of the zero rate
 * given an Interpolator.
 *
 * The interpolator contains the zero curve
 * information for all the swap cash flows. That
 * way the Zero Rate for the cash flow maturity
 * can be bootstrapped.
 *
 * @author Jose Gonzalez
 */
public class Swap {
    private double fixedRate;
    private double swapTermInYears;
    private double yearlyFrequency;
    private double zeroRateSwapTerm;
    private static final double DAYS_IN_YEAR = 365.0;
    private double totalNumberOfCashFlow;
    private double frequencyInYears;


    public Swap(DataPoint dataPoint) {
        this.fixedRate = dataPoint.getRate();
        this.swapTermInYears = DAYS.between(dataPoint.getSettle(), dataPoint.getMaturity()) / DAYS_IN_YEAR;
        this.frequencyInYears = 1 / dataPoint.getFrequency();
        this.totalNumberOfCashFlow = Math.round(this.swapTermInYears * this.yearlyFrequency);
    }

    public Swap(double swapTermInYears, double fixedRate, double yearlyFrequency) {
        this.fixedRate = fixedRate;
        this.swapTermInYears = swapTermInYears;
        this.yearlyFrequency = yearlyFrequency;
        this.frequencyInYears = 1 / yearlyFrequency;
        this.totalNumberOfCashFlow = Math.round(this.swapTermInYears * this.yearlyFrequency);
    }

    public double bootStrapAndGetZeroRate(Interpolation interpolation) throws InterpolationException {
        double sumOfDiscountedCouponCashFlows = 0;
        double term ;
        for (int i = 1; i < this.totalNumberOfCashFlow; i++) {
            term = i * this.frequencyInYears;
            sumOfDiscountedCouponCashFlows += (this.fixedRate / this.yearlyFrequency) * interpolation.getDiscountFactor(term);

        }
        this.zeroRateSwapTerm = Math.pow((1 + this.fixedRate / this.yearlyFrequency) / (1 - sumOfDiscountedCouponCashFlows), (1 / this.swapTermInYears)) - 1;
        // this.zeroRateSwapTerm = Math.log((1 - sumOfDiscountedCouponCashFlows) / (1 + this.fixedRate / this.yearlyFrequency)) / this.swapTermInYears;

        return zeroRateSwapTerm;
    }

    public double getSwapTermInYears() {
        return swapTermInYears;
    }

}
