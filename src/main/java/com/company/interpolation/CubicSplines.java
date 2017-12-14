package com.company.interpolation;

import com.company.beans.RatesCurve;
import com.company.interfaces.Interpolation;
import com.company.exceptions.InterpolationException;
import com.company.linearequationsystem.GaussMatrix;
import com.company.beans.TimeBucket;

/**
 * Natural Cubic Splines interpolator class.
 * <p>
 * Given an Rates Curve, the class interpolates between
 * the discrete points in the curve using a piece wise
 * natural cubic splines interpolation.
 * <p>
 * The class returns the rate and/ or discount factor for
 * an specified maturity.
 * <p>
 * The class was written using the mathematical terminology
 * specified in:
 * <p>
 * "Fitting the term structure of interest rates: the practical implementation of cubic splines methodology"
 * by Rod Pienaar and Moorad Choudhry.
 * <p>
 * the authors use the following terminology for the 3rd degree polynomial that is used
 * to interpolate:
 * <p>
 * y = a.x^3 +b.x^2 +c.x^2 + d
 * <p>
 * CapitalX= x(n+1) - x(n)
 * <p>
 * The coefficient matrix and linear equation system construction
 * also follows the specifications of the above mentioned paper. For
 * further details review pp 16.
 * <p>
 * The linear system of equations is solved using a Gauss elimination
 * algorithm.
 *
 * @author Jose Gonzalez
 */
public class CubicSplines implements Interpolation {
    private RatesCurve ratesCurve;
    private double[] bValues;
    private TimeBucket[] timeBuckets;
    private double lastTerm;
    private final double SECONDS_IN_YEAR = 525600;

    public CubicSplines(RatesCurve ratesCurve) {
        this.ratesCurve = ratesCurve;
        this.timeBuckets = new TimeBucket[this.ratesCurve.size()];
        this.setTimeBuckets();
        this.lastTerm = this.ratesCurve.lastTermInCurve();

        this.solveLinearEquationSystem();
    }

    public void solveLinearEquationSystem() {
        GaussMatrix gaussMatrix = new GaussMatrix(this.ratesCurve.size());
        gaussMatrix.setCoefficientMatrix(this.getCoefficientMatrix());
        gaussMatrix.setConstants(this.getConstantsVector());
        gaussMatrix.findSolution();
        this.bValues = gaussMatrix.getSolution();
    }

    public double getModeledRate(double maturity) throws InterpolationException {
        if (maturity <= this.lastTerm) {
            int index = 0;
            if (maturity < this.timeBuckets[0].getMinMaturity()) {
                double capitalX = 0;
                return this.getA(index) * Math.pow(capitalX, 3) + this.getB(index) * Math.pow(capitalX, 2) + this.getC(index) * capitalX + this.getD(index);
            }
            while (maturity >= this.timeBuckets[index].getMinMaturity()) {
                index++;
                if (maturity == this.timeBuckets[index].getMinMaturity()) break;
            }
            index--;
            double capitalX = maturity - this.timeBuckets[index].getMinMaturity();

            return this.getA(index) * Math.pow(capitalX, 3) + this.getB(index) * Math.pow(capitalX, 2) + this.getC(index) * capitalX + this.getD(index);
        } else {
            int index = this.timeBuckets.length - 2;
            double capitalX = maturity - this.timeBuckets[index].getMinMaturity();
            return this.getA(index) * Math.pow(capitalX, 3) + this.getB(index) * Math.pow(capitalX, 2) + this.getC(index) * capitalX + this.getD(index);

        }
    }

    @Override
    public double getForwardRate(double maturity) throws InterpolationException {
        double fwdValueMaturity = Math.pow((1 + this.getModeledRate(maturity)),maturity);
        double fwdValueMaturityEpsilon = Math.pow((1 + this.getModeledRate(maturity+ 1 / SECONDS_IN_YEAR)),maturity+ 1 / SECONDS_IN_YEAR);
        return Math.pow(fwdValueMaturityEpsilon / fwdValueMaturity, 1/(maturity+ 1 / SECONDS_IN_YEAR - maturity)) - 1;
    }

    public double getForwardRate(double startTerm, double endTerm) throws InterpolationException {
        double fwdValueEndTerm = Math.pow((1 + this.getModeledRate(endTerm)),endTerm);
        double fwdValueStartTerm = Math.pow((1 + this.getModeledRate(startTerm)),startTerm);
        return Math.pow(fwdValueEndTerm / fwdValueStartTerm, 1/(endTerm - startTerm)) - 1;
    }

    public double getDiscountFactor(double xValue) throws InterpolationException {
        double rate = this.getModeledRate(xValue);
        return Math.exp(-rate * xValue);
    }

    public double getLastTerm() {
        return lastTerm;
    }

    /**
     * Creates the time buckets that define the piece wise
     * interpolation of the curve
     */
    private void setTimeBuckets() {
        for (int i = 0; i < this.ratesCurve.size(); i++) {
            if (i < this.ratesCurve.size() - 1) {
                this.timeBuckets[i] = new TimeBucket(this.ratesCurve.getMaturity(i), this.ratesCurve.getMaturity(i + 1));
            } else {
                this.timeBuckets[i] = new TimeBucket(this.ratesCurve.getMaturity(i), Double.MAX_VALUE);
            }
        }
    }

    private double[] getConstantsVector() {
        double[] constants = new double[this.ratesCurve.size()];
        for (int row = 0; row < this.ratesCurve.size(); row++) {
            if (row == 0) {
                constants[row] = 0;
            } else if (row == this.ratesCurve.size() - 1) {
                constants[row] = 0;
            } else {
                constants[row] = constantValue(row);
            }
        }
        return constants;
    }


    private double[][] getCoefficientMatrix() {
        double[][] coefficientMatrix = new double[this.ratesCurve.size()][this.ratesCurve.size()];
        int boost = 0;
        for (int row = 0; row < this.ratesCurve.size(); row++) {

            if (row == 0) {
                coefficientMatrix[row][0] = 1;
            } else if (row == this.ratesCurve.size() - 1) {
                coefficientMatrix[row][this.ratesCurve.size() - 1] = 1;
            } else {
                coefficientMatrix[row][boost] = this.ratesCurve.getCapitalX(boost);
                coefficientMatrix[row][boost + 1] = 2 * (this.ratesCurve.getCapitalX(boost) + this.ratesCurve.getCapitalX(boost + 1));
                coefficientMatrix[row][boost + 2] = this.ratesCurve.getCapitalX(boost + 1);
                boost++;
            }
        }
        return coefficientMatrix;
    }

    private double constantValue(int index) {
        double firstPart = (this.getD(index) - this.getD(index - 1)) / this.ratesCurve.getCapitalX(index - 1);

        double secondPart = (this.getD(index + 1) - this.getD(index)) / this.ratesCurve.getCapitalX(index);

        return -3 * (firstPart - secondPart);
    }

    private double getD(int index) {
        return this.ratesCurve.getRate(index);
    }

    private double getA(int index) {
        return (this.getB(index + 1) - this.getB(index)) / (3 * this.ratesCurve.getCapitalX(index));
    }

    private double getC(int index) {
        return -this.ratesCurve.getCapitalX(index) * (this.getB(index + 1) + 2 * this.getB(index)) / 3
                + (this.getD(index + 1) - this.getD(index)) / this.ratesCurve.getCapitalX(index);
    }

    private double getB(int index) {
        return this.bValues[index];
    }


}
