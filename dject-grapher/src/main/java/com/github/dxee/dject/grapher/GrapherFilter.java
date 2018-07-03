package com.github.dxee.dject.grapher;

import com.google.inject.ImplementedBy;

import java.util.List;

@ImplementedBy(FileGrapherFilter.class)
public interface GrapherFilter {
    List<String> packages();
    void dispatch(String dot);
}
