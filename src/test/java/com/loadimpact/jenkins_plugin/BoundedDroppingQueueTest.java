package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.BoundedDroppingQueue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * DESCRIPTION
 *
 * @author jens
 * @date 2013-10-06, 09:13
 */
public class BoundedDroppingQueueTest {
    private BoundedDroppingQueue<Integer> target;
    private final int N = 3;

    @Before
    public void setUp() throws Exception {
        target = new BoundedDroppingQueue<Integer>(N);
    }

    @Test
    public void a_few_put_and_get_should_pass() throws Exception {
        assertThat(target.size(), is(0));
        assertThat(target.empty(), is(true));

        for (int k = 1; k <= N; ++k) target.put(k);
        assertThat(target.full(), is(true));

        for (int k = 1; k <= N; ++k) {
            int x = target.get();
            assertThat(x, is(k));
        }
        assertThat(target.size(), is(0));
        assertThat(target.empty(), is(true));
    }

    @Test
    public void more_put_than_size_should_pass() throws Exception {
        for (int k = 1; k <= N * 2; ++k) target.put(k);
        assertThat(target.size(), is(N));
        
        int k = N + 1;
        while (!target.empty()) {
            int n = target.get();
            assertThat(n, is(k++));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void get_on_empty_should_fail() {
        target.put(1);
        target.get();
        target.get();
    }

    @Test
    public void queue_size_1_should_pass() {
        target = new BoundedDroppingQueue<Integer>(1);
        assertThat(target.size(), is(0));

        int M = 100;
        for (int k = 1; k <= M; ++k) {
            target.put(k);
            assertThat(target.size(), is(1));
        }
        assertThat(target.get(), is(M));
        assertThat(target.size(), is(0));
    }

    @Test
    public void foreach_loop_should_pass() {
        for (int k = 1; k <= N + 1; ++k) target.put(k);
        assertThat(target.size(), is(N));

        int k = 2;
        for (Integer n : target) assertThat(n, is(k++));
        assertThat(target.size(), is(N));
    }

    @Test
    public void toList_should_pass() {
        for (int k = 1; k <= N * 2; ++k) target.put(k);
        assertThat(target.size(), is(N));

        List<Integer> lst = target.toList();
        assertThat(lst, notNullValue());
        assertThat(lst.size(), is(target.size()));
        assertThat(lst.size(), is(N));

        int k = N + 1;
        for (Integer n : lst) {
            assertThat(n, is(k++));
        }
    }
    
}
