package org.computronium.digitsequences;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An unsigned sequence of digits in a given base.
 */
class PowerSeries {

    static final Pattern FORMAT = Pattern.compile("(\\.{3})?([0-9]+)(b([0-9]+))?");

    /**
     * The base that this sequence is defined in.  Defaults to 10.
     */
    private final int base;

    /**
     * The digits that make up this power series, least significant first.
     */
    private final Short[] digits;

    /**
     * The set of unique tokens that represent a particular number's infinite sequence of otherwise
     * unspecified digits.  An infinite number just created has a single token with an associated
     * count of one.  If it gets added to itself, it will have a count of two.  If another infinite
     * number gets added to it, that other number's token will get added to the map with a count of
     * 1.  When numbers get subtracted from this number, the resulting number will have those tokens
     * removed.
     */
    private final Map<String, Integer> tokens;

    static enum ComparisonResult {
        LESS_THAN,
        EQUAL,
        GREATER_THAN,
        CANT_TELL
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    PowerSeries(int base, Map<String, Integer> tokens, Short[] digits) {
        this.base = base;
        this.tokens = tokens;
        this.digits = digits;
        // TODO trim leading zeroes
    }

    public ComparisonResult compareTo(PowerSeries that) {
        assert that.base == base;
        if (isFinite()) {
            if (that.isFinite()) {
                if (digits.length > that.digits.length) {
                    return ComparisonResult.GREATER_THAN;
                }
                if (digits.length < that.digits.length) {
                    return ComparisonResult.LESS_THAN;
                }
                int i = digits.length - 1;
                while (i >= 0) {
                    short thisDigit = digits[i];
                    short thatDigit = that.digits[i];
                    if (thisDigit > thatDigit) {
                        return ComparisonResult.GREATER_THAN;
                    }
                    if (thisDigit < thatDigit) {
                        return ComparisonResult.LESS_THAN;
                    }
                    i--;
                }
                return ComparisonResult.EQUAL;
            }

            // This number is finite, but the other is infinite.
            return ComparisonResult.LESS_THAN;
        }

        if (that.isFinite()) {
            // This number is infinite, the other is finite.
            return ComparisonResult.GREATER_THAN;
        }

        // Both are infinite.  We can't really tell.
        // TODO compare tokens.
        return ComparisonResult.CANT_TELL;
    }


    public short digitAt(int index) {
        if (index >= digits.length && isFinite()) {
            return 0;
        }
        return digits[index];
    }

    public int getBase() {
        return base;
    }

    public boolean isFinite() {
        return tokens.isEmpty();
    }

    public Short[] getDigits() {
        return digits;
    }

    public int size() {
        return digits.length;
    }

    public PowerSeries add(PowerSeries addend) {

        Builder sum = new Builder(this).addToken(finite && addend.isFinite());
        int carry = 0;
        int index = 0;
        while (canKeepAdding(carry, this, addend, index)) {
            short thisDigit = this.digitAt(index);
            short addendDigit = addend.digitAt(index);
            int digitSum = carry + thisDigit + addendDigit;
            sum.addDigit((short) (digitSum % 10));
            carry = digitSum / 10;

            index++;
        }
        return sum.build();
    }

    private static boolean canKeepAdding(int carry, PowerSeries augend, PowerSeries addend, int index) {
        if (augend.isFinite() && addend.isFinite()) {
            return carry > 0 || index < augend.size() || index < addend.size();
        } else {
            return augend.hasKnownDigitAt(index) && addend.hasKnownDigitAt(index);
        }
    }

    public PowerSeries subtract(PowerSeries subtrahend) {

        Builder difference = new Builder().withFinite(finite && subtrahend.isFinite());
        short borrowed = 0;
        int index = 0;
        while (canKeepSubtracting(this, subtrahend, index)) {
            int a = this.digitAt(index) - borrowed;
            int b = subtrahend.digitAt(index);
            if (b > a) {
                // Need to borrow.
                borrowed = 1;
                a += 10;
            } else {
                borrowed = 0;
            }
            difference.addDigit((short) (a - b));
            index++;
        }
        return difference.build();
    }
    
    private static boolean canKeepSubtracting(PowerSeries minuend, PowerSeries subtrahend, int index) {
        if (minuend.isFinite() && subtrahend.isFinite()) {
            return index < minuend.size() || index < subtrahend.size();
        } else {
            return minuend.hasKnownDigitAt(index) && subtrahend.hasKnownDigitAt(index);
        }
    }


    public PowerSeries multiply(PowerSeries multiplier) {
        Builder product = new Builder().withFinite(isFinite() && multiplier.isFinite());
        int carry = 0;
        int index = 0;
        while (canKeepMultiplying(carry, this, multiplier, index)) {
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

    private static boolean canKeepMultiplying(int carry, PowerSeries multiplicand, PowerSeries multiplier, int index) {
        if (multiplicand.isFinite() && multiplier.isFinite()) {
            return carry > 0 || index < multiplicand.size() || index < multiplier.size();
        } else {
            return multiplicand.hasKnownDigitAt(index) && multiplier.hasKnownDigitAt(index);
        }
    }

    private boolean hasKnownDigitAt(int index) {
        return isFinite() || index < size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PowerSeries that = (PowerSeries) o;

        if (base != that.base) return false;
        if (!tokens.equals(that.tokens)) return false;
        if (!Arrays.equals(digits, that.digits)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = base;
        result = 31 * result + (digits != null ? Arrays.hashCode(digits) : 0);
        result = 31 * result + (tokens != null ? tokens.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Short digit : digits) {
            sb.insert(0, digit);
        }
        if (!isFinite()) {
            sb.insert(0, "...");
        }
        if (base != 10) {
            sb.append("b").append(base);
        }
        return sb.toString();
    }

    public static class Builder {
        private Map<String, Integer> tokens = new HashMap<>();
        private int base = 10;
        private List<Short> digits = new ArrayList<>();

        public Builder() {
        }

        public Builder(int n, boolean finite) {
            assert n > 0;
            if (!finite) {
                tokens.put(generateToken(), 1);
            }
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

            boolean infinite = matcher.group(1) != null;
            if (infinite) {
                // It started with "...", so it's infinite.  Assign it a token.
                tokens.put(generateToken(), 1);
            }

            String digitString = matcher.group(2);
            if (!infinite) {
                while (digitString.length() > 1 && digitString.charAt(0) == '0') {
                    digitString = digitString.substring(1);
                }
            }
            for (int i = digitString.length() - 1; i >= 0; i--) {
                digits.add((short) (digitString.charAt(i) - '0'));
            }

            this.base = matcher.group(3) == null ? 10 : Integer.valueOf(matcher.group(4));
        }

        public Builder(PowerSeries series) {
            this.tokens.putAll(series.tokens);
            this.base = series.base;
            this.digits = Arrays.asList(series.digits);
        }

        public Builder withDigits(List<Short> digits) {
            this.digits = digits;
            return this;
        }

        public Builder addToken(String token) {
            addToken(token, 1);
            return this;
        }

        public Builder removeToken(String token) {
            addToken(token, -1);
            return this;
        }

        private void addToken(String token, int delta) {
            int current = tokens.containsKey(token) ? tokens.get(token) : 0;
            int newCount = current + delta;
            if (newCount == 0) {
                tokens.remove(token);
            } else {
                tokens.put(token, newCount);
            }
        }

        public Builder withBase(int base) {
            this.base = base;
            return this;
        }

        public Builder addDigit(short digit) {
            digits.add(digit);
            return this;
        }

        public PowerSeries build() {
            if (tokens.isEmpty()) {
                // Trim leading zeros.
                while (digits.size() > 1 && digits.get(digits.size()-1) == 0) {
                    digits.remove(digits.size()-1);
                }
            }
            return new PowerSeries(base, tokens, digits.toArray(new Short[0]));
        }
    }
}
