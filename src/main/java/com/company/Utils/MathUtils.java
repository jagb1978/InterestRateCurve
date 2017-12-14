package com.company.Utils;

public class MathUtils {
    private static double EPSILON = 0.00000001;

    public static boolean almostZero(double number){
        return Math.abs(number)< EPSILON;
    }

    public static boolean almostEqual(double numberOne, double numberTwo){
        return Math.abs(numberOne-numberTwo)< EPSILON;
    }
}
