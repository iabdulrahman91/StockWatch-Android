package edu.depaul.csc472.stockwatch;

public class Stock implements Comparable {
    private String sympol;
    private String name;
    private Double price;
    private Double priceChange;
    private Double changePercent;

    public Stock(String sympol, String name, Double price, Double priceChange, Double changePercent) {
        this.sympol = sympol;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercent = changePercent;
    }

    public String getSympol() {
        return sympol.toUpperCase();
    }

    public void setSympol(String sympol) {
        this.sympol = sympol.toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public int compareTo(Object o) {
        return this.sympol.compareTo(((Stock) o).getSympol());
    }
}
