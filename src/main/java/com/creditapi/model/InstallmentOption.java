package com.creditapi.model;

public enum InstallmentOption {
    SIX(6), NINE(9), TWELVE(12), TWENTY_FOUR(24);

    private final int value;

    InstallmentOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValid(int value) {
        for (InstallmentOption option : values()) {
            if (option.value == value) return true;
        }
        return false;
    }
}
