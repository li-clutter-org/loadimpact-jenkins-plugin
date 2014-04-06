package com.loadimpact.jenkins_plugin.client;

import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

/**
 * Collection of utilities functions.
 *
 * @author jens
 * @date 2013-09-11, 09:25
 */
@Deprecated
public class Util {

    /**
     * Parses a text string with a date in ISO 8601 format into a {@link java.util.Date} object.
     * @param s     text with ISO 8601 formatted date
     * @return Date 
     */
    public static Date toDateFromIso8601(String s) {
        return ISODateTimeFormat.dateTimeNoMillis().parseDateTime(s).toDate();
    }

    /**
     * Adjusts a LoadImpact/Python timestamp [us] value into an epoch based ditto [ms] and returns it as a {@link java.util.Date} object.
     * @param ts    timestamp value in micro-seconds
     * @return Date 
     */
    public static Date toDateFromTimestamp(long ts) {
        return new Date(ts / 1000);
    }

    /**
     * Returns true if the target string has a value and starts with the given prefix.
     * @param target    string to investigate
     * @param prefix    substring to check for
     * @return true if prefix starts in target
     */
    public static boolean startsWith(String target, String prefix) {
        return target != null && target.length() > 0 && target.startsWith(prefix);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String fixEmpty(String s) {
        if (isBlank(s)) return null;
        return s.trim();
    }

    public static String toInitialCase(String s) {
        if (isBlank(s)) return s;
        if (s.length() == 1) return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    /**
     * Map-Extract function that collects all 'value_int' elements from a list of {@link TestResult}.
     * @param results   list of {@link TestResult}
     * @return list of ints
     */
    public static List<Integer> collectInts(List<TestResult> results) {
        List<Integer> values = new ArrayList<Integer>();
        for (TestResult r : results) {
            values.add(r.value.intValue());
        }
        return values; 
    }

    /**
     * Similar as {@link #collectInts(java.util.List)}, but for 'value_decimal'.
     * @param results   list of {@link TestResult}
     * @return list of doubles
     */
    public static List<Double> collectDecimals(List<TestResult> results) {
        List<Double> values = new ArrayList<Double>();
        for (TestResult r : results) {
            values.add(r.value.doubleValue());
        }
        return values; 
    }

    /**
     * Returns the last element of a list, or null if empty.
     * @param lst   the list
     * @param <T>   element type
     * @return last element or null
     */
    public static <T> T last(List<T> lst) {
        if (lst == null || lst.isEmpty()) return null;
        return lst.get(lst.size() - 1);
    }

    /**
     * Computes the median value of a list of integers.
     * @param values    list of values
     * @return the computed medium
     */
    @SuppressWarnings("unchecked")
    public static int median(List<Integer> values) {
        if (values == null || values.isEmpty()) return 0;

        values = new ArrayList<Integer>(values);
        Collections.sort(values);

        final int size = values.size();
        final int sizeHalf = size / 2;
        if (size % 2 == 1) { //is odd?
            // 0 1 [2] 3 4: size/2 = 5/2 = 2.5 -> 2
            return values.get(sizeHalf);
        }

        // 0 1 [2 3] 4 5: size/2 = 6/2 = 3
        return (values.get(sizeHalf - 1) + values.get(sizeHalf)) / 2;
    }

    /**
     * Computes the average/mean/expected value of a list of numbers.
     * @param values    list of values
     * @return the computed average
     */
    public static double average(List<? extends Number> values) {
        if (values == null || values.isEmpty()) return 0D;
        
        double sum = 0D;
        for (Number v : values) sum += v.doubleValue();

        return sum / values.size();
    }

    /**
     * Concatenates a list of strings using the given separator. 
     * @param lst           list of text strings
     * @param separator     separator
     * @return string
     */
    public static String join(Collection<String> lst, String separator) {
        StringBuilder buf = new StringBuilder(lst.size() * 64);
        boolean first = true;
        for (String value : lst) {
            if (first) first = false; else buf.append(separator);
            buf.append(value);
        }
        return buf.toString();
    }


    /**
     * Creates a percentage ASCII bar.
     * @param percentage    value in [0, 100]
     * @return "[###......] nn%"
     */
    public static String percentageBar(double percentage) {
        final char  dot   = '.';
        final char  mark  = '#';
        final int   slots = 40;

        StringBuilder bar = new StringBuilder(replicate(String.valueOf(dot), slots));
        int numSlots = (int) (slots * percentage / 100.0);
        for (int k = 0; k < numSlots; ++k) bar.setCharAt(k, mark);

        return String.format("[%s] %3.0f%%", bar, percentage);
    }

    /**
     * Replicates a string.
     * @param s         the string to replicate
     * @param times     number of times
     * @return combined string
     */
    public static String replicate(String s, int times) {
        StringBuilder b = new StringBuilder(s.length() * times);
        for (int k = 1; k <= times; ++k) b.append(s);
        return b.toString();
    }

    /**
     * Call-back interface for {@link #map(java.util.List, com.loadimpact.jenkins_plugin.client.Util.MapClosure)}.
     * @param <From>    value type
     * @param <To>      destination type
     */
    public interface MapClosure<From, To> {
        To eval(From value);
    }

    /**
     * Applies a function to every item in a list.
     * @param list      the list with values
     * @param f         closure to apply
     * @param <From>    value type
     * @param <To>      destination type
     * @return list of transformed values
     */
    @SuppressWarnings("unchecked")
    public static <From, To> List<To> map(List<From> list, MapClosure<From,To> f) {
        List<To> result = new ArrayList<To>(list.size());
        for (From value : list) {
            result.add( f.eval(value) );
        }
        return result;
    }
    
    
    public interface ReduceClosure<Accumulator, Value> {
        Accumulator eval(Accumulator acc, Value value);
    }

    public static <Accumulator, Value> Accumulator reduce(List<Value> list, Accumulator init, ReduceClosure<Accumulator,Value> f) {
        Accumulator accumulator = init;
        for (Value value : list) {
            accumulator = f.eval(accumulator, value);
        }
        return accumulator;
    }
    
    
}
