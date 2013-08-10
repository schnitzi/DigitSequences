package org.computronium.digitsequences;

import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Tests {@link DigitSequence}.
 */
@Test
public class DigitSequenceTest {

    public void testConstructorsAndToString() {
        Assert.assertTrue(new DigitSequence(123).toString().equals("123"));
        Assert.assertTrue(new DigitSequence(0).toString().equals("0"));
        Assert.assertTrue(new DigitSequence(123, true).toString().equals("123"));
        Assert.assertTrue(new DigitSequence(123, false).toString().equals("...123"));
        Assert.assertTrue(new DigitSequence(1234567890).toString().equals("1234567890"));
        Assert.assertTrue(new DigitSequence("...12345678901231324234").toString().equals("...12345678901231324234"));
        Assert.assertTrue(new DigitSequence("12345678901231324234").toString().equals("12345678901231324234"));
        badStringTest("");
        badStringTest("abc");
        badStringTest("..123");
        badStringTest("....123");
        badStringTest("-1");
        badStringTest("123.456");
        badStringTest("123a");
    }

    private void badStringTest(String s) {
        try {
            new DigitSequence(s);
            Assert.fail("Expected error");
        } catch (Error e) {
            // Expected -- do nothing.
        }
    }

    public void testEquals() {
        Assert.assertEquals(new DigitSequence(123), new DigitSequence(123));
        Assert.assertEquals(new DigitSequence(123, false), new DigitSequence(123, false));
        Assert.assertEquals(new DigitSequence(0), new DigitSequence(0));
        Assert.assertEquals(new DigitSequence(0, false), new DigitSequence(0, false));
        Assert.assertEquals(new DigitSequence("...123123123"), new DigitSequence("...123123123"));
        Assert.assertEquals(new DigitSequence("123123123"), new DigitSequence("123123123"));
        Assert.assertEquals(new DigitSequence("04"), new DigitSequence("4"));
        Assert.assertEquals(new DigitSequence("00"), new DigitSequence("0"));
        Assert.assertNotSame(new DigitSequence("...123123123"), new DigitSequence("123123123"));
        Assert.assertNotSame(new DigitSequence("123123123"), new DigitSequence("...123123123"));
        Assert.assertNotSame(new DigitSequence("...0"), new DigitSequence("...1"));
        Assert.assertNotSame(new DigitSequence("...0"), new DigitSequence("0"));
        Assert.assertNotSame(new DigitSequence("...4"), new DigitSequence("4"));
        Assert.assertNotSame(new DigitSequence("...4"), new DigitSequence("...14"));
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
        Assert.assertEquals(new DigitSequence(sum), new DigitSequence(augend).add(new DigitSequence(addend)));
    }

    public void testSubtraction() {

    }

    public void testMultiplication() {

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
