package com.company.enums;

public enum RateType {
    CASH ("Cash"),
    SWAP ("Swap");
    private String text;

    RateType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static RateType fromString(String text) {
        for (RateType b : RateType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
