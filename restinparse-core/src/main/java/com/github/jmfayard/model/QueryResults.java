package com.github.jmfayard.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResults {
    public final List<Something> results;

    public QueryResults(List<Something> results) {
        if (results == null) {
            this.results = Collections.emptyList();
        } else {
            this.results = new ArrayList<Something>(results);
        }
    }
}
