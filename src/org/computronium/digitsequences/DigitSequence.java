package org.computronium.digitsequences;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a digit sequence.
 */
public class DigitSequence {

    private static final short UNKNOWN = -1;
    private static final Pattern FORMAT = Pattern.compile("(\\.{3})?([0-9]+)");

    private final List<Short> digits = new ArrayList<>();
    private final boolean finite;

    private DigitSequence(boolean finite) {
        this.finite = finite;
    }

    public DigitSequence(int n) {
        this(n, true);
    }

    public DigitSequence(int n, boolean finite) {
        this(finite);
        if (n == 0) {
            digits.add((short) 0);
        } else {
            while (n>0) {
                digits.add((short)(n%10));
                n = n / 10;
            }
        }
    }

    public DigitSequence(String s) {
        Matcher matcher = FORMAT.matcher(s);
        assert matcher.matches();
        finite = matcher.group(1) == null;
        String digitString = matcher.group(2);
        if (finite) {
            while (digitString.length()>1 && digitString.charAt(0) == '0') {
                digitString = digitString.substring(1);
            }
        }
        for (int i=digitString.length()-1; i>=0; i--) {
            digits.add((short) (digitString.charAt(i)-'0'));
        }
    }

    public short digitAt(int index) {
        if (index < digits.size()) {
            return digits.get(index);
        } else if (finite) {
            return 0;
        } else {
            return UNKNOWN;
        }
    }

    public int size() {
        return digits.size();
    }

    public boolean isFinite() {
        return finite;
    }

    public DigitSequence add(DigitSequence addend) {
        DigitSequence sum = new DigitSequence(finite && addend.isFinite());
        int carry = 0;
        int index = 0;
        while (canComputeMoreDigits(carry, this, addend, index)) {
            short thisDigit = this.digitAt(index);
            short addendDigit = addend.digitAt(index);
            int digitSum = carry + thisDigit + addendDigit;
            sum.digits.add((short) (digitSum % 10));
            carry = digitSum / 10;

            index++;
        }
        return sum;
    }

    private static boolean canComputeMoreDigits(int carry, DigitSequence augend, DigitSequence addend, int index) {
        if (augend.isFinite() && addend.isFinite()) {
            return carry > 0 || index < augend.digits.size() || index < addend.digits.size();
        } else {
            return augend.hasKnownDigitAt(index) && addend.hasKnownDigitAt(index);
        }
    }

    private boolean hasKnownDigitAt(int index) {
        return finite || index < digits.size();
    }

    public DigitSequence multiply(DigitSequence multiplier) {
        DigitSequence product = new DigitSequence(finite && multiplier.isFinite());
        int carry = 0;
        int index = 0;
        while (true) {
            if (index >= size() || index >= multiplier.size()) {
                break;
            }
            int columnSum = carry;
            for (int i=0; i<=index; i++) {
                columnSum += digitAt(i) * multiplier.digitAt(index-i);
            }
            product.digits.add((short) (columnSum % 10));
            carry = columnSum / 10;
            index++;
        }
        return product;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Short digit : digits) {
            sb.insert(0, digit);
        }
        if (!finite) {
            sb.insert(0, "...");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DigitSequence that = (DigitSequence) o;

        if (finite != that.finite) return false;
        if (!digits.equals(that.digits)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = digits.hashCode();
        result = 31 * result + (finite ? 1 : 0);
        return result;
    }

    public static void main(String[] args) {
        DigitSequence ds1 = new DigitSequence(123);
        DigitSequence ds2 = new DigitSequence("...123456");
        System.out.println(ds1.toString());
        System.out.println(ds2.toString());
        System.out.println(ds2.add(new DigitSequence(4)));
        System.out.println(ds2.add(new DigitSequence(9)));
        System.out.println(ds2.add(new DigitSequence(999)));
        System.out.println(ds2.multiply(new DigitSequence(3)));
        System.out.println(ds2.multiply(new DigitSequence(6)));
        System.out.println(ds1.add(ds2));
        System.out.println(ds1.multiply(ds2));
    }
}
