package com.codecool.klondike;

public enum SuitEnum {
        HEARTS("hearts", "red"),
        DIAMONDS("diamonds", "red"),
        SPADES("spades", "black"),
        CLUBS("clubs", "black");

    final String name;
    final String color;

    SuitEnum(String name, String color) {
        this.name = name;
        this.color = color;
    }


    }

