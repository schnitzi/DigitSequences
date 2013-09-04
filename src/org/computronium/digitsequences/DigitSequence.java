package org.computronium.digitsequences;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents a digit sequence.
 */
public class DigitSequence {

    private static final short UNKNOWN = -1;
    private static final Pattern FORMAT = Pattern.compile("(\\-)?" + PowerSeries.FORMAT);

    public static final DigitSequence ZERO = DigitSequence.of("0");
    public static final DigitSequence ONE = DigitSequence.of("1");

    private final boolean negative;
    private final PowerSeries series;

    public DigitSequence(boolean negative, int base, boolean finite, Short[] digits) {
        this(negative, new PowerSeries(base, finite, digits));
    }

    public DigitSequence(boolean negative, PowerSeries series) {
        this.negative = negative;
        this.series = series;
    }

    public static DigitSequence of(String s) {
        return new Builder(s).build();
    }

    public short digitAt(int index) {
        if (index < size()) {
            return series.digitAt(index);
        } else if (series.isFinite()) {
            return 0;
        } else {
            return UNKNOWN;
        }
    }

    public int size() {
        return series.size();
    }

    public boolean isFinite() {
        return series.isFinite();
    }

    public DigitSequence negate() {
        if (this.equals(ZERO)) {
            return ZERO;
        }
        return new Builder(this).negate().build();
    }

    public DigitSequence subtract(DigitSequence subtrahend) {

        return add(subtrahend.negate());
    }

    public DigitSequence add(DigitSequence addend) {

        // TODO  assert bases match, everywhere.

        boolean resultNegative;
        PowerSeries resultSeries;

        if (this.negative == addend.negative) {
            // They are the same sign, so we can just add the digits and keep the sign.
            return new Builder()
                    .withNegative(negative)
                    .withSeries(series.add(addend.series)).build();
        }

        // Otherwise, it's a subtraction.  Figure out which number is larger in absolute magnitude
        // (if we can) and subtract the smaller one from it.  Keep the sign of the larger.

        PowerSeries.ComparisonResult comparison = series.compareTo(addend.series);
        if (comparison == PowerSeries.ComparisonResult.EQUAL) {
            // They're equal so the difference is just zero.
            return ZERO;
        }

        DigitSequence larger, smaller;

        if (comparison == PowerSeries.ComparisonResult.GREATER_THAN) {
            larger = this;
            smaller = addend;
        } else if (comparison == PowerSeries.ComparisonResult.LESS_THAN) {
            larger = addend;
            smaller = this;
        } else {
            // Can't tell.  What to do?  TODO
            larger = this;
            smaller = addend;
        }

        return new Builder().withNegative(larger.negative).withSeries(larger.series.subtract(smaller.series)).build();
    }

    public DigitSequence multiply(DigitSequence multiplier) {

        if (this.equals(ZERO) || multiplier.equals(ZERO)) {
            return ZERO;
        }

        return new Builder()
                .withNegative(negative ^ multiplier.negative)
                .withSeries(series.multiply(multiplier.series))
                .build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("-");
        }
        sb.append(series);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DigitSequence that = (DigitSequence) o;

        if (negative != that.negative) return false;
        return series.equals(that.series);
    }

    @Override
    public int hashCode() {
        int result = series.hashCode();
        result = 31 * result + (negative ? 1 : 0);
        return result;
    }

    public static class Builder {
        private boolean negative;
        private final PowerSeries.Builder series;

        public Builder() {
            series = new PowerSeries.Builder();
        }

        public Builder(int n, boolean finite) {
            negative = n < 0;
            series = new PowerSeries.Builder(n<0 ? -n : n, finite);
        }

        public Builder(String s) {

            if (s.startsWith("-")) {
                negative = true;
                s = s.substring(1);
            }
            series = new PowerSeries.Builder(s);
        }

        public Builder(DigitSequence digitSequence) {
            negative = digitSequence.negative;
            series = new PowerSeries.Builder(digitSequence.series);
        }

        public Builder withFinite(boolean finite) {
            series.withFinite(finite);
            return this;
        }

        public Builder withNegative(boolean negative) {
            this.negative = negative;
            return this;
        }

        public Builder withBase(int base) {
            series.withBase(base);
            return this;
        }

        public Builder withSeries(PowerSeries series) {
            this.series.withBase(series.getBase()).withDigits(Arrays.asList(series.getDigits())).withFinite(series.isFinite());
            return this;
        }

        public Builder addDigit(short digit) {
            series.addDigit(digit);
            return this;
        }

        public DigitSequence build() {
            return new DigitSequence(negative, series.build());
        }

        public Builder negate() {
            negative = !negative;
            return this;
        }
    }
}
