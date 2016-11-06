package com.stolser.javatraining.block02.morelessgame.controller;

import com.stolser.javatraining.view.ViewPrinter;

import java.util.Scanner;

public class ConsoleInputReader implements InputReader {
    private static final String GENERAL_MESSAGE_BUNDLE = "generalMessages";
    private static final String INPUT_INTEGER_ERROR = "input.integer.error";

    private Scanner scanner;
    private ViewPrinter output;

    public ConsoleInputReader(ViewPrinter output) {
        this.output = output;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public int readIntValue() {
        while( ! scanner.hasNextInt()) {
            output.printMessageWithKey(GENERAL_MESSAGE_BUNDLE, INPUT_INTEGER_ERROR);
            scanner.next();
        }

        return scanner.nextInt();
    }
}