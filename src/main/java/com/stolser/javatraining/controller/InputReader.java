package com.stolser.javatraining.controller;

/**
 * An abstraction for input data source.
 */
public interface InputReader {
    int readIntValue();
    String readString();
    boolean readYesNoValue();
}