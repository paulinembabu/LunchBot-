package com.bot;

public class CafeOrder {
    private String menuItem;
    private int quantity = 0;
    private Double price = 0d;

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = Integer.parseInt(quantity);
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotal(){
        return quantity * price;
    }
}
