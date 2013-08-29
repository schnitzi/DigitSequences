package org.computronium.digitsequences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a digit sequence.
 */
public class DigitSequence {

    private static final short UNKNOWN = -1;
    private static final Pattern FORMAT = Pattern.compile("(\\-)?(\\.{3})?([0-9]+)(b([0-9]+))?");

    public static final DigitSequence ZERO = DigitSequence.of("0");
    public static final DigitSequence ONE = DigitSequence.of("1");

    private final boolean negative;
    private final Series series;

    private static class Series {
        private final int base;
        private final boolean finite;
        private final Short[] digits;

        private Series(int base, boolean finite, Short[] digits) {
            this.base = base;
            this.finite = finite;
            this.digits = digits;
        }
    }

    public DigitSequence(int base, boolean negative, boolean finite, Short[] digits) {
        this.negative = negative;
        this.series = new Series(base, finite, digits);
    }

    public static DigitSequence of(String s) {
        return new Builder(s).build();
    }

    public short digitAt(int index) {
        if (index < size()) {
            return series.digits[index];
        } else if (series.finite) {
            return 0;
        } else {
            return UNKNOWN;
        }
    }

    public int size() {
        return series.digits.length;
    }

    public boolean isFinite() {
        return series.finite;
    }

    public DigitSequence add(DigitSequence addend) {

        Builder sum = new Builder().withFinite(series.finite && addend.isFinite());
        int carry = 0;
        int index = 0;
        while (canComputeMoreDigits(carry, this, addend, index)) {
            short thisDigit = this.digitAt(index);
            short addendDigit = addend.digitAt(index);
            int digitSum = carry + thisDigit + addendDigit;
            sum.addDigit((short) (digitSum % 10));
            carry = digitSum / 10;

            index++;
        }
        return sum.build();
    }

    private static boolean canComputeMoreDigits(int carry, DigitSequence augend, DigitSequence addend, int index) {
        if (augend.isFinite() && addend.isFinite()) {
            return carry > 0 || index < augend.size() || index < addend.size();
        } else {
            return augend.hasKnownDigitAt(index) && addend.hasKnownDigitAt(index);
        }
    }

    public DigitSequence subtract(DigitSequence subtrahend) {
        boolean resultNegative;
        Series resultSeries;

        if (negative) {
            if (subtrahend.negative) {
                resultNegative = true;
                resultSeries = seriesSubtract(series, subtrahend.series);
            }
        }
        Builder difference = new Builder().withNegative(resultNegative).with
    }

    private boolean hasKnownDigitAt(int index) {
        return series.finite || index < size();
    }

    public DigitSequence multiply(DigitSequence multiplier) {
        Builder product = new Builder().withFinite(series.finite && multiplier.isFinite());
        int carry = 0;
        int index = 0;
        while (true) {
            if (index >= size() || index >= multiplier.size()) {
                break;
            }
            int columnSum = carry;
            for (int i = 0; i <= index; i++) {
                columnSum += digitAt(i) * multiplier.digitAt(index - i);
            }
            product.digits.add((short) (columnSum % 10));
            carry = columnSum / 10;
            index++;
        }
        return product.build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Short digit : series.digits) {
            sb.insert(0, digit);
        }
        if (!series.finite) {
            sb.insert(0, "...");
        }
        if (negative) {
            sb.insert(0, "-");
        }
        if (series.base != 10) {
            sb.append("b").append(series.base);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DigitSequence that = (DigitSequence) o;

        if (series.finite != that.series.finite) return false;
        if (negative != that.negative) return false;
        if (!Arrays.equals(series.digits, that.series.digits)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = series.digits.hashCode();
        result = 31 * result + (series.finite ? 1 : 0);
        return result;
    }

    public static class Builder {
        private boolean finite;
        private boolean negative;
        private int base = 10;
        private List<Short> digits = new ArrayList<>();

        public Builder() {
        }

        public Builder(int n) {
            this(n, true);
        }

        public Builder(int n, boolean finite) {
            this.negative = n < 0;
            this.finite = finite;
            if (n == 0) {
                digits.add((short) 0);
            } else {
                if (n < 0) {
                    n = -n;
                }
                while (n > 0) {
                    digits.add((short) (n % 10));
                    n = n / 10;
                }
            }
        }

        public Builder(String s) {

            Matcher matcher = FORMAT.matcher(s);
            assert matcher.matches();

            this.negative = matcher.group(1) != null;

            this.finite = matcher.group(2) == null;

            String digitString = matcher.group(3);
            if (finite) {
                while (digitString.length() > 1 && digitString.charAt(0) == '0') {
                    digitString = digitString.substring(1);
                }
            }
            for (int i = digitString.length() - 1; i >= 0; i--) {
                digits.add((short) (digitString.charAt(i) - '0'));
            }

            this.base = matcher.group(4) == null ? 10 : Integer.valueOf(matcher.group(5));
        }

        public Builder withDigits(List<Short> digits) {
            this.digits = digits;
        }

        public Builder withFinite(boolean finite) {
            this.finite = finite;
            return this;
        }

        public Builder withNegative(boolean negative) {
            this.negative = negative;
            return this;
        }

        public Builder withBase(int base) {
            this.base = base;
            return this;
        }

        public Builder addDigit(short digit) {
            digits.add(digit);
            return this;
        }

        public DigitSequence build() {
            return new DigitSequence(base, negative, finite, digits.toArray(new Short[0]));
        }
    }
}
