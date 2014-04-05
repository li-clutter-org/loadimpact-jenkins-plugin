package com.loadimpact.jenkins_plugin.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test of class Util
 *
 * @author jens
 * @date 2013-09-11, 14:45
 */
public class UtilTest {

    @Test
    public void testToDateFromIso8601() {
        String input = "2013-09-09T02:34:51+00:00";
        GregorianCalendar expected = new GregorianCalendar(2013, 9 - 1, 9, 2 + 2, 34, 51);
        assertThat(clearMilliSecs(Util.toDateFromIso8601(input)), is(clearMilliSecs(expected.getTime())));
    }

    @Test
    public void testToDateFromTimestamp() {
        GregorianCalendar expected = new GregorianCalendar(2013, 8, 10, 17, 38, 36);
        assertThat(clearMilliSecs(Util.toDateFromTimestamp(1378827516043595L)), is(clearMilliSecs(expected.getTime())));
    }

    private Date clearMilliSecs(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        c.clear(Calendar.MILLISECOND);
        return c.getTime();
    }

    @Test
    public void testSelectValueInt() {
        List<TestResult> values = Arrays.asList(
                new TestResult(null, null, 0, 1),
                new TestResult(null, null, 0, 2),
                new TestResult(null, null, 0, 3),
                new TestResult(null, null, 0, 4)
        );
        assertThat(Util.collectInts(values), is(Arrays.asList(1, 2, 3, 4)));
    }

    @Test
    public void testSelectValueDecimal() {
        List<TestResult> values = Arrays.asList(
                new TestResult(null, null, 0, 1.5),
                new TestResult(null, null, 0, 2.5),
                new TestResult(null, null, 0, 3.5),
                new TestResult(null, null, 0, 4.5)
        );
        assertThat(Util.collectDecimals(values), is(Arrays.asList(1.5, 2.5, 3.5, 4.5)));
    }

    @Test
    public void testMedianWithOddNumbers() {
        List<Integer> target = Arrays.asList(1, 2, -10, 10, 5);
        // -10 1 [2] 5 10
        assertThat(Util.median(target), is(2));
    }

    @Test
    public void testMedianWithEvenNumbers() {
        List<Integer> target = Arrays.asList(1, 2, -10, 10, 5, 4);
        // -10 1 [2 4] 5 10
        assertThat(Util.median(target), is(3));
    }

    @Test
    public void testMedianWithTwoNumbers() {
        List<Integer> target = Arrays.asList(-10, 10);
        assertThat(Util.median(target), is(0));
    }

    @Test
    public void testMedianWithOneNumber() {
        List<Integer> target = Arrays.asList(10);
        assertThat(Util.median(target), is(10));
    }

    @Test
    public void testMedianWithZeroNumber() {
        List<Integer> target = Arrays.asList();
        assertThat(Util.median(target), is(0));
    }

    @Test
    public void testAverage() {
        List<Double> target = Arrays.asList(1.0, 2.0, -10.0, 10.0, 5.0, 4.0);
        // 12 / 6 = 2
        assertThat(Util.average(target), is(2.0));
    }

    @Test
    public void testMap() {
        List<Integer> inputs = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> expected = Arrays.asList(1, 4, 9, 16, 25);
        List<Integer> actual = Util.map(inputs, new Util.MapClosure<Integer, Integer>() {
            public Integer eval(Integer value) {
                return value * value;
            }
        });
        assertThat(actual, is(expected));
    }

    @Test
    public void testMap2() {
        List<ResultsCategory> inputs = Arrays.asList(
                ResultsCategory.bandwidth,
                ResultsCategory.clients_active,
                ResultsCategory.requests_per_second,
                ResultsCategory.user_load_time
        );
        List<String> expected = Arrays.asList(
                "__li_bandwidth",
                "__li_clients_active",
                "__li_requests_per_second",
                "__li_user_load_time"
        );
        List<String> actual = Util.map(inputs, new Util.MapClosure<ResultsCategory, String>() {
            public String eval(ResultsCategory value) {
                return value.param;
            }
        });
        assertThat(actual, is(expected));
    }

    @Test
    public void testJoin() {
        List<String> input = Arrays.asList("foo", "bar", "fee");
        String sep = "#";
        assertThat(Util.join(input,sep), is("foo#bar#fee"));
    }

    @Test
    public void testPercentageBar() {
        assertThat(Util.percentageBar(25.0), is("[##########..............................]  25%"));
        assertThat(Util.percentageBar(0)   , is("[........................................]   0%"));
        assertThat(Util.percentageBar(100) , is("[########################################] 100%"));
    }

    @Test
    public void testReduce() {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        int actual = Util.reduce(input, 0, new Util.ReduceClosure<Integer, Integer>() {
            public Integer eval(Integer sum, Integer value) {
                return sum + value;
            }
        });
        assertThat(actual, is(55));
    }

}
