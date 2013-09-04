package org.computronium.digitsequences;

import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Tests {@link DigitSequence}.
 */
@Test
public class DigitSequenceTest {

    public void testConstructorsAndToString() {
        final String[] validStrings = new String[]{
                "123", "0", "1", "-1", "...123", "1234234876",
                "12312387123876342364129837",
                "...12312387123876342364129837",
                "-2349872947832",
                "-...2349872947832",
                "1234b9", "123123124b234"
        };
        for (String validString : validStrings) {
            Assert.assertTrue(DigitSequence.of(validString).toString().equals(validString));
        }

        final String[] invalidStrings = new String[]{
                "", "abc", "..123", "....123", "123.3", "123a"
        };

        for (String invalidString : invalidStrings) {
            try {
                DigitSequence.of(invalidString);
                Assert.fail("Expected error");
            } catch (Error e) {
                // Expected -- do nothing.
            }
        }
    }

    public void testEquals() {
        Assert.assertEquals(DigitSequence.of("123"), DigitSequence.of("123"));
        Assert.assertEquals(DigitSequence.of("...123"), DigitSequence.of("...123"));
        Assert.assertEquals(DigitSequence.of("0"), DigitSequence.of("0"));
        Assert.assertEquals(DigitSequence.of("...0"), DigitSequence.of("...0"));
        Assert.assertEquals(DigitSequence.of("...123123123"), DigitSequence.of("...123123123"));
        Assert.assertEquals(DigitSequence.of("123123123"), DigitSequence.of("123123123"));
        Assert.assertEquals(DigitSequence.of("04"), DigitSequence.of("4"));
        Assert.assertEquals(DigitSequence.of("00"), DigitSequence.of("0"));
        Assert.assertNotSame(DigitSequence.of("...123123123"), DigitSequence.of("123123123"));
        Assert.assertNotSame(DigitSequence.of("123123123"), DigitSequence.of("...123123123"));
        Assert.assertNotSame(DigitSequence.of("...0"), DigitSequence.of("...1"));
        Assert.assertNotSame(DigitSequence.of("...0"), DigitSequence.of("0"));
        Assert.assertNotSame(DigitSequence.of("...4"), DigitSequence.of("4"));
        Assert.assertNotSame(DigitSequence.of("...4"), DigitSequence.of("...14"));
    }

    public void testAddition() {

        // Basic tests of finites.
        additionTest("123", "123", "246");
        additionTest("123", "0", "123");
        additionTest("0", "123", "123");
        additionTest("500", "500", "1000");
        additionTest("999", "2", "1001");
        additionTest("999", "2", "1001");
        additionTest("12345678901234567890", "2", "12345678901234567892");
        additionTest("0", "0", "0");
        additionTest("0", "1", "1");
        additionTest("99", "0", "99");
        additionTest("2", "2", "4");

        // Add finite value to infinite one.
        additionTest("...123", "123", "...246");
        additionTest("123", "...123", "...246");
        additionTest("999", "...2", "...1");
        additionTest("...2", "999", "...1");
        additionTest("...999", "2", "...001");
        additionTest("...123", "0", "...123");
        additionTest("0", "...123", "...123");
        additionTest("1", "...123", "...124");

        // Adding two infinites.
        additionTest("...123", "...123", "...246");
        additionTest("...999", "...2", "...1");
        additionTest("...999", "...002", "...001");
        additionTest("...9", "...2", "...1");
        additionTest("...9", "...0", "...9");
        additionTest("...0", "...9", "...9");
    }

    private void additionTest(String augend, String addend, String sum) {
        Assert.assertEquals(DigitSequence.of(sum), DigitSequence.of(augend).add(DigitSequence.of(addend)));
    }

    public void testSubtraction() {

        // Test the finites.
        subtractionTest("10", "5", "5");
        subtractionTest("10", "0", "10");
        subtractionTest("0", "1", "-1");
        subtractionTest("10", "10", "0");

        // Finite minus positive infinite - should be a negative infinite number.
        subtractionTest("123", "...145", "-...022");
        subtractionTest("0", "...12345", "-...12345");
        subtractionTest("999", "...1", "-...2");
        subtractionTest("9", "...1432", "-...1423");

        // Finite minus negative infinite - should be positive infinite.
        subtractionTest("123", "-...145", "...268");
        subtractionTest("0", "-...145", "...145");

        // Positive infinite minus finite - should be positive finite still.
        subtractionTest("...842", "34", "...808");
        subtractionTest("...842", "-3", "...845");

        // Negative infinite minus finite - should be negative finite still.
        subtractionTest("-...842", "34", "-...876");
        subtractionTest("-...842", "-3", "-...839");

        // Finite minus positive infinite - should be negative infinite.
        subtractionTest("-842", "...34", "-...76");
        subtractionTest("0", "...3", "-...3");

        // Finite minus negative infinite - should be positive infinite.
        subtractionTest("-842", "-...34", "...92");
        subtractionTest("0", "-...3", "...3");
    }

    private void subtractionTest(String minuend, String subtrahend, String difference) {
        Assert.assertEquals(DigitSequence.of(difference), DigitSequence.of(minuend).subtract(DigitSequence.of(subtrahend)));
    }
    
    public void testAdditiveAssociativity() {
        DigitSequence[] testNumbers = new DigitSequence[] {
                DigitSequence.of("0"),
                DigitSequence.of("1"),
                DigitSequence.of("-1"),
                DigitSequence.of("123"),
                DigitSequence.of("456"),
                DigitSequence.of("-123"),
                DigitSequence.of("-999"),
                DigitSequence.of("12389712391872391723197"),
                DigitSequence.of("...123"),
                DigitSequence.of("...0"),
                DigitSequence.of("...1"),
                DigitSequence.of("-...3282"),
                DigitSequence.of("-...456"),
                DigitSequence.of("-1")
        };
        for (int i=0; i<testNumbers.length; i++) {
            for (int j=0; j<testNumbers.length; j++) {
                DigitSequence a = testNumbers[i];
                DigitSequence b = testNumbers[j];
                // For each possible pairing (including pairing with itself), test that a+b-b == a.
                Assert.assertEquals("" + a + "+" + b + "-" + b + " != " +a, a, a.add(b).subtract(b));
            }
        }
    }

    public void testMultiplication() {
        // Test the finites.
        multiplicationTest("5", "1", "5");
        multiplicationTest("1", "5", "5");
        multiplicationTest("2", "0", "0");
        multiplicationTest("0", "3", "0");
        multiplicationTest("-5", "5", "-25");
        multiplicationTest("10", "5", "50");
        multiplicationTest("1", "-5", "-5");
        multiplicationTest("2", "2", "4");
        multiplicationTest("11111111111111111111", "2", "22222222222222222222");

        // Finite times infinite.
        multiplicationTest("5", "...1", "...5");
        multiplicationTest("5", "-...1", "-...5");
        multiplicationTest("-5", "...1", "-...5");
        multiplicationTest("-5", "...2", "-...0");
        multiplicationTest("5", "...111", "...555");
        multiplicationTest("3", "...117", "...351");
        multiplicationTest("-...117", "...3", "-...1");
        multiplicationTest("-...98098", "...23412", "-...70376");
        multiplicationTest("...9809823408", "...23412", "...28096");
    }

    private void multiplicationTest(String mulitiplicand, String multiplier, String product) {
        Assert.assertEquals(DigitSequence.of(product), DigitSequence.of(mulitiplicand).multiply(DigitSequence.of(multiplier)));
    }

    public void testDivision() {

    }

    public void testSquareRoot() {

    }

    public void testBaseConversion() {

    }

    public void testExpressionEvaluation() {

    }
}
