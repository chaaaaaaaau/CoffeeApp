package com.example.chaaaaau.coffeeapp;

public class CoffeeRecordData {
    public CoffeeRecordData(String coffeeName, Float ratingValue) {
        this.coffeeName = coffeeName;
        this.ratingValue = ratingValue;
    }

    public String getCoffeeName() {
        return coffeeName;
    }

    public void setCoffeeName(String coffeeName) {
        this.coffeeName = coffeeName;
    }

    public Float getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Float ratingValue) {
        this.ratingValue = ratingValue;
    }

    protected String coffeeName;
    protected Float ratingValue;

}
