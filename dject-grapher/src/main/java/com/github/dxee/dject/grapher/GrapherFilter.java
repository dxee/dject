package com.github.dxee.dject.grapher;

import com.google.inject.ImplementedBy;

import java.util.List;

@ImplementedBy(FileGrapherFilter.class)
public interface GrapherFilter {
    void dispatch(String dot);

    List<String> packages();
}
