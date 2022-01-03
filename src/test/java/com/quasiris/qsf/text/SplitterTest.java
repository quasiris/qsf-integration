package com.quasiris.qsf.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class SplitterTest {

    @Test
    public void testSplitToSet() {
        Set<String> splitted = Splitter.splitToSet("foo,bar");
        assertThat(splitted, containsInAnyOrder("foo", "bar"));
    }
    @Test
    public void testSplitToList() {
        List<String> splitted = Splitter.splitToList("foo,bar");
        assertThat(splitted, containsInAnyOrder("foo", "bar"));
    }
    @Test
    public void testSplitToListWithCustomSeperator() {
        List<String> splitted = Splitter.splitToList("foo,bar|bar", "|");
        assertThat(splitted, containsInAnyOrder("foo,bar", "bar"));
    }

}