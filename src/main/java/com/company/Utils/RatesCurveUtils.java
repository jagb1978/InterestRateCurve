package com.company.Utils;

import com.company.beans.DataPoint;
import com.company.beans.RatePoint;
import com.company.beans.RatesCurve;
import com.company.enums.RateBasis;
import com.company.exceptions.InterpolationException;
import com.company.interfaces.Interpolation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RatesCurveUtils - Helper class for the InterestRate Curve project
 *
 * @author Jose Gonzalez
 */
public class RatesCurveUtils {

    public static List<DataPoint> getDataPointsFromFile(String fileName) throws IOException {
        String line;
        FileReader fileReader;
        BufferedReader bufferedReader;
        fileReader = new FileReader(fileName);
        bufferedReader = new BufferedReader(fileReader);
        List<DataPoint> dataPointList = new ArrayList<>();

        line = bufferedReader.readLine();

        int counter = 0;

        while (line != null) {
            String[] splitLine = line.split(",");
            if (counter > 0) {
                DataPoint dataPoint = new DataPoint.Builder()
                        .withType(splitLine[0])
                        .withSettle(splitLine[1])
                        .withRollDate(splitLine[2])
                        .withMaturity(splitLine[3])
                        .withRate(splitLine[4])
                        .withBasis(splitLine[5])
                        .withFrequency(splitLine[6])
                        .withCouponBase(splitLine[7])
                        .build();
                dataPointList.add(dataPoint);
            }
            counter++;
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return dataPointList;
    }


    public static RatesCurve getFullSwapsCurveAfterInterpolatingMissingMaturities(RatesCurve swapsRatesCurve, Interpolation interpolator) throws InterpolationException {

        double startingMaturity = swapsRatesCurve.firstTermInCurve();
        double finalMaturity = swapsRatesCurve.lastTermInCurve();
        double swapFrequency = swapsRatesCurve.getCashFlowYearlyFrequency();
        double frequencyInYears = 1 / swapFrequency;
        int numberOfPointsInCurve = (int) Math.round((finalMaturity - startingMaturity) * swapFrequency);

        RatesCurve newSwapCurve = new RatesCurve();
        newSwapCurve.setCashFlowYearlyFrequency(swapFrequency);

        double term;
        for (int i = 0; i <= numberOfPointsInCurve; i++) {
            term = startingMaturity + i * frequencyInYears;
            RatePoint ratePoint = new RatePoint(term, interpolator.getModeledRate(term), RateBasis.CONTINOUS);

            newSwapCurve.add(ratePoint);
        }

        return newSwapCurve;
    }

//    public static RatePoint getRatePointUsingLinearInterpolation(Swap swap, int index, RatesCurve ratesCurve) throws InterpolationException {
//        Interpolation linearInterpolation = (index + 1) != ratesCurve.size() ?
//                new LinearInterpolation(ratesCurve.getRatePoint(index), ratesCurve.getRatePoint(index + 1)) :
//                new LinearInterpolation(ratesCurve.getRatePoint(index), ratesCurve.getRatePoint(index));
//
//        return new RatePoint(swap.getSwapTermInYears(), swap.bootStrapAndGetZeroRate(linearInterpolation));
//    }
}
