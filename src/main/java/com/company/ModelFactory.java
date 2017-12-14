package com.company;

import com.company.beans.RatesCurve;
import com.company.enums.ModelType;
import com.company.interfaces.Interpolation;
import com.company.interpolation.CubicSplines;
import com.company.interpolation.MonotoneConvex;

/**
 * Interpolation model factory
 *
 * @author Jose Gonzalez
 */
public class ModelFactory {

    public static Interpolation createModel(ModelType modelType, RatesCurve ratesCurve){
        if(modelType.equals(ModelType.MONOTONE_CONVEX)){
            return  new MonotoneConvex(ratesCurve);
        } else if(modelType.equals(ModelType.CUBIC_SPLINES)){
            return  new CubicSplines(ratesCurve);
        } else{
            return null;
        }
    }
}
