package com.sophie;

import com.formdev.flatlaf.FlatDarkLaf;

public class SuperflatAllPortalsCalculator {
    public static void main(String[] args) {
        //Initialize UI and F3+C listener
        FlatDarkLaf.setup();
        new UI();
        F3CReader.init();
    }
}
