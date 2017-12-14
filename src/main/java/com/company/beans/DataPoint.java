package com.company.beans;

import com.company.enums.Basis;
import com.company.enums.RateType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Bean container holding the raw data point information
 * from the csv input files
 *
 * @author Jose Gonzalez
 */
public class DataPoint {
    private RateType type;
    private LocalDate settle;
    private LocalDate rollDate;
    private LocalDate maturity;
    private double rate;
    private Basis basis;
    private double frequency;
    private double couponBase;

    public static class Builder {
        private RateType type;
        private LocalDate settle;
        private LocalDate rollDate;
        private LocalDate maturity;
        private double rate;
        private Basis basis;
        private double frequency;
        private double couponBase;
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        public Builder withType(String type) {
            this.type = RateType.fromString(type);
            return this;
        }

        public Builder withSettle(String settle) {
            try {
                this.settle = LocalDate.parse(settle, formatter);
            } catch (DateTimeParseException e) {
                //
            }
            return this;
        }

        public Builder withRollDate(String rollDate) {
            try {
                this.rollDate = LocalDate.parse(rollDate, formatter);
            } catch (DateTimeParseException e) {
                //
            }
            return this;
        }

        public Builder withMaturity(String maturity) {
            try {
                this.maturity = LocalDate.parse(maturity, formatter);
            } catch (DateTimeParseException e) {
                //
            }
            return this;
        }

        public Builder withRate(String rate) {
            this.rate = Double.valueOf(rate);
            return this;
        }

        public Builder withBasis(String basis) {
            this.basis = Basis.fromString(basis);
            return this;
        }

        public Builder withFrequency(String frequency) {
            try {
                this.frequency = Double.valueOf(frequency);
            } catch (NumberFormatException e) {
                //
            }
            return this;
        }

        public Builder withCouponBase(String couponBase) {
            try {
                this.couponBase = Double.valueOf(couponBase);
            } catch (NumberFormatException e) {
                //
            }
            return this;
        }

        public DataPoint build() {
            return new DataPoint(this);
        }
    }

    private DataPoint(DataPoint.Builder builder) {
        this.type = builder.type;
        this.settle = builder.settle;
        this.rollDate = builder.rollDate;
        this.maturity = builder.maturity;
        this.basis = builder.basis;
        this.frequency = builder.frequency;
        this.couponBase = builder.couponBase;
        this.rate = builder.rate * 1 / this.couponBase;
    }


    public RateType getType() {
        return type;
    }

    public LocalDate getSettle() {
        return settle;
    }

    public LocalDate getRollDate() {
        return rollDate;
    }

    public LocalDate getMaturity() {
        return maturity;
    }

    public double getRate() {
        return rate;
    }

    public Basis getBasis() {
        return basis;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getCouponBase() {
        return couponBase;
    }



}
