package com.company.enums;

public enum Basis {
    ACT365 ("ACT/365"),
    ACT360 ("30/360");

    private String text;

    Basis(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static Basis fromString(String text) {
        for (Basis b : Basis.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
