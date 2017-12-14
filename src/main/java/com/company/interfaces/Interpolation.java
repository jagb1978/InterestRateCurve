package com.company.interfaces;

import com.company.exceptions.InterpolationException;

public interface Interpolation {
    double getDiscountFactor(double xValue)  throws InterpolationException;
    double getModeledRate(double maturity) throws InterpolationException;
    double getLastTerm();
    double getForwardRate(double xValue) throws InterpolationException;
    double getForwardRate(double startTerm, double endTerm) throws InterpolationException;
}
