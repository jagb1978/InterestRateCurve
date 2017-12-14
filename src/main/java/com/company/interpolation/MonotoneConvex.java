package com.company.interpolation;

import com.company.Utils.MathUtils;
import com.company.beans.RatesCurve;
import com.company.enums.MonotoneConvexZone;
import com.company.exceptions.InterpolationException;
import com.company.interfaces.Interpolation;

public class MonotoneConvex implements Interpolation {
    private double[] forwardRates;
    private double[] terms;
    private double[] discreteTerms;
    private double[] discreteForwardRates;  //discrete forward rates
    private double[] values; //rate values from data
    private double[] discreteRatesValues;//discrete forward value
    private double[] discreteInterpolationNode;
    private int indexOfLastTerm; //index of last term
    private boolean Negative_Forwards_Allowed = false;
    private int lastIndexUsed;
    private boolean curveSet;

    public MonotoneConvex(double[] values, double[] terms) {
        this.values = values;
        this.terms = terms;
        this.indexOfLastTerm = terms.length - 1;
        this.discreteForwardRates = new double[terms.length];
        this.forwardRates = new double[terms.length];
        this.discreteTerms = new double[terms.length]; //discreteTerms(-1 To iIndex)
        this.discreteRatesValues = new double[terms.length];
        this.discreteInterpolationNode = new double[terms.length];
        this.curveSet = true;
    }

    public MonotoneConvex(RatesCurve spotRatesCurve) {
        int arraySize = spotRatesCurve.size();
        double[] values = new double[arraySize + 1];
        double[] terms = new double[arraySize + 1];
        values[0] = spotRatesCurve.getRate(0);
        terms[0] = 0.0;

        for (int i = 1; i <= arraySize; i++) {
            values[i] = spotRatesCurve.getRate(i - 1);
            terms[i] = spotRatesCurve.getMaturity(i - 1);
        }

        this.values = values;
        this.terms = terms;
        this.indexOfLastTerm = terms.length - 1;
        this.discreteForwardRates = new double[terms.length];
        this.forwardRates = new double[terms.length];
        this.discreteTerms = new double[terms.length]; //discreteTerms(-1 To iIndex)
        this.discreteRatesValues = new double[terms.length];
        this.discreteInterpolationNode = new double[terms.length];
        this.curveSet = false;
    }

    private MonotoneConvexZone getZone(double gZero, double gOne) {
        if ((gZero < 0 && -0.5 * gZero <= gOne && gOne <= -2 * gZero) || (gZero > 0 && -0.5 * gZero >= gOne && gOne >= -2 * gZero)) {
            return MonotoneConvexZone.ZONE_ONE;
        } else if ((gZero < 0 && gOne > -2 * gZero) || (gZero > 0 && gOne < -2 * gZero)) {
            return MonotoneConvexZone.ZONE_TWO;
        } else if ((gZero > 0 && 0 > gOne && gOne > -0.5 * gZero) || (gZero < 0 && 0 < gOne && gOne < -0.5 * gZero)) {
            return MonotoneConvexZone.ZONE_THREE;
        } else if (MathUtils.almostZero(gZero) && MathUtils.almostZero(gOne)) {
            return MonotoneConvexZone.ZONE_ESPECIAL;
        } else {
            return MonotoneConvexZone.ZONE_FOUR;
        }
    }

    private double interpolate(double term) {
        setUpCurves();
        int i;
        if (term <= 0) {
            return this.forwardRates[0];
        } else if (term > this.terms[indexOfLastTerm]) {
            return interpolate(this.terms[this.indexOfLastTerm]) * this.terms[this.indexOfLastTerm] / term + forward(this.terms[this.indexOfLastTerm]) * (1 - this.terms[this.indexOfLastTerm] / term);
        } else {
            i = getIndex(term);
        }
        //the x in (25)
        double x = (term - this.terms[i]) / (this.terms[i + 1] - this.terms[i]);
        double gZero = this.forwardRates[i] - this.discreteForwardRates[i + 1];
        double gOne = this.forwardRates[i + 1] - this.discreteForwardRates[i + 1];
        double gFunction = 0;

        if (x == 0.0 || x == 1.0) {
            gFunction = 0;
        } else {
            double eta;
            switch (getZone(gZero, gOne)) {
                case ZONE_ONE:
                    gFunction = gZero * (x - 2 * Math.pow(x, 2) + Math.pow(x, 3)) + gOne * (-Math.pow(x, 2) + Math.pow(x, 3));
                    break;
                case ZONE_TWO:
                    eta = (gOne + 2 * gZero) / (gOne - gZero);//(29)
                    gFunction = x <= eta ? gZero * x : gZero * x + (gOne - gZero) * Math.pow((x - eta), 3) / Math.pow((1 - eta), 2) / 3;//(28)
                    break;
                case ZONE_THREE:
                    eta = 3 * gOne / (gOne - gZero);//(31)
                    gFunction = x < eta ? gOne * x - 1.0 / 3.0 * (gZero - gOne) * (Math.pow((eta - x), 3) / Math.pow(eta, 2) - eta) : (2.0 / 3.0 * gOne + 1.0 / 3.0 * gZero) * eta + gOne * (x - eta);
                    break;
                case ZONE_FOUR:
                    eta = gOne / (gOne + gZero);//(33)
                    double A = -gZero * gOne / (gZero + gOne); //(34)
                    //(32)
                    gFunction = x <= eta ? A * x - 1.0 / 3.0 * (gZero - A) * (Math.pow((eta - x), 3) / Math.pow(eta, 2) - eta) : (2.0 / 3.0 * A + 1.0 / 3.0 * gZero) * eta + A * (x - eta) + (gOne - A) / 3 * Math.pow((x - eta), 3) / Math.pow((1 - eta), 2);
                    break;
                case ZONE_ESPECIAL:
                    gFunction = 0;
                    break;
            }
        }

        return 1 / term * (this.terms[i] * this.discreteInterpolationNode[i] + (term - this.terms[i]) * this.discreteForwardRates[i + 1] + (this.terms[i + 1] - this.terms[i]) * gFunction);
    }

    private double forward(double Term) {
        this.setUpCurves();
        int i;
        if (Term <= 0) {
            return this.forwardRates[0];
        } else if (Term > this.terms[this.indexOfLastTerm]) {
            return forward(this.terms[this.indexOfLastTerm]);
        } else {
            i = getIndex(Term);
        }

        double x = (Term - terms[i]) / (terms[i + 1] - terms[i]); //the x in (25)
        double gZero = forwardRates[i] - discreteForwardRates[i + 1];
        double gOne = forwardRates[i + 1] - discreteForwardRates[i + 1];
        double gFunction = 0;

        if (x == 0) {
            gFunction = gZero;
        } else if (x == 1) {
            gFunction = gOne;
        } else {
            double eta;
            switch (getZone(gZero, gOne)) {
                case ZONE_ONE:
                    gFunction = gZero * (1 - 4 * x + 3 * Math.pow(x, 2)) + gOne * (-2 * x + 3 * Math.pow(x, 2));
                    break;
                case ZONE_TWO:
                    eta = (gOne + 2 * gZero) / (gOne - gZero); //(29)
                    gFunction = x <= eta ? gZero : gZero + (gOne - gZero) * Math.pow(((x - eta) / (1 - eta)), 2); //(28)
                    break;
                case ZONE_THREE:
                    eta = 3 * gOne / (gOne - gZero); //(31)
                    gFunction = x < eta ? gOne + (gZero - gOne) * Math.pow(((eta - x) / eta), 2) : gOne;//(30)
                    break;
                case ZONE_FOUR:
                    eta = gOne / (gOne + gZero);//(33)
                    double A = -gZero * gOne / (gZero + gOne);//(34)
                    gFunction = x <= eta ? A + (gZero - A) * Math.pow(((eta - x) / eta), 2) : A + (gOne - A) * Math.pow(((eta - x) / (1 - eta)), 2);
                    break;
                case ZONE_ESPECIAL:
                    gFunction = 0;
                    break;
            }
        }
        return gFunction + this.discreteForwardRates[i + 1];//(26)
    }

    private int getIndex(double Term) {
        int iLastIndex;
        if (!this.Negative_Forwards_Allowed) {
            iLastIndex = (int) (collar(0, this.lastIndexUsed, this.indexOfLastTerm));
        } else {
            iLastIndex = this.lastIndexUsed;
        }
        while (true) {
            if (Term >= this.terms[iLastIndex]) {
                if (iLastIndex == this.terms.length - 1) {
                    this.lastIndexUsed = Term == this.terms[iLastIndex] ? this.indexOfLastTerm - 1 : this.indexOfLastTerm;
                    break;
                } else {
                    if (Term >= this.terms[iLastIndex + 1]) {
                        iLastIndex = iLastIndex + 1;
                    } else {
                        this.lastIndexUsed = iLastIndex;
                        break;
                    }
                }
            } else {
                if (iLastIndex == 0) {
                    this.lastIndexUsed = 0;
                    break;
                } else {
                    if (Term >= terms[iLastIndex - 1]) {
                        this.lastIndexUsed = iLastIndex - 1;
                        break;
                    } else {
                        iLastIndex = iLastIndex - 1;
                    }
                }
            }
        }
        return this.lastIndexUsed;
    }

    private void setUpCurves() {
        if (!this.curveSet) {
            //extend the curve to time 0, for the purpose of calculating forward at time 1
            this.discreteTerms[0] = 0.0;
            this.discreteRatesValues[0] = discreteRatesValues[1];

            //step 1
            for (int j = 1; j <= this.indexOfLastTerm; j++) {
                this.discreteForwardRates[j] = (this.terms[j] * this.values[j] - this.terms[j - 1] * this.values[j - 1]) / (this.terms[j] - this.terms[j - 1]);
                this.discreteInterpolationNode[j] = this.values[j];
            }

            for (int j = 1; j <= this.indexOfLastTerm - 1; j++) {
                this.forwardRates[j] = (this.terms[j] - this.terms[j - 1]) / (this.terms[j + 1] - this.terms[j - 1]) * this.discreteForwardRates[j + 1]
                        + (this.terms[j + 1] - this.terms[j]) / (this.terms[j + 1] - this.terms[j - 1]) * this.discreteForwardRates[j];
            }

            this.forwardRates[0] = this.discreteForwardRates[1] - 0.5 * (this.forwardRates[1] - this.discreteForwardRates[1]); //(23)
            this.forwardRates[this.indexOfLastTerm] = this.discreteForwardRates[this.indexOfLastTerm] - 0.5 *
                    (this.forwardRates[this.indexOfLastTerm - 1] - this.discreteForwardRates[this.indexOfLastTerm]); //(24)

            if (!this.Negative_Forwards_Allowed) {
                this.forwardRates[0] = this.collar(0, this.forwardRates[0], 2 * this.discreteForwardRates[1]);

                for (int j = 1; j <= this.indexOfLastTerm - 1; j++) {
                    this.forwardRates[j] = this.collar(0, forwardRates[j], 2 * Math.min(this.discreteForwardRates[j], this.discreteForwardRates[j + 1]));
                }

                this.forwardRates[this.indexOfLastTerm] = this.collar(0, this.forwardRates[this.indexOfLastTerm], 2 * this.discreteForwardRates[this.indexOfLastTerm]);
            } else {
                double termRate = 0;
                for (int j = 1; j < this.indexOfLastTerm - 1; j++) {
                    this.discreteForwardRates[j] = this.values[j];
                    termRate = termRate + this.discreteForwardRates[j] * (this.terms[j] - this.terms[j - 1]);
                    this.discreteInterpolationNode[j] = termRate / this.terms[j];
                }
            }

            this.curveSet = true;
        }
    }

    private double collar(double minimum, double variable, double maximum) {
        if (variable < minimum) {
            return minimum;
        } else if (variable > maximum) {
            return maximum;
        } else {
            return variable;
        }
    }

    @Override
    public double getDiscountFactor(double xValue) throws InterpolationException {
        double rate = this.getModeledRate(xValue);
        return Math.exp(-rate * xValue);
    }

    @Override
    public double getModeledRate(double maturity) throws InterpolationException {
        return this.interpolate(maturity);
    }

    @Override
    public double getLastTerm() {
        return this.terms[this.indexOfLastTerm];
    }

    @Override
    public double getForwardRate(double xValue) throws InterpolationException {
        return this.forward(xValue);
    }

    @Override
    public double getForwardRate(double startTerm, double endTerm) throws InterpolationException {
        return this.getForwardRate(endTerm - startTerm);
    }
}
