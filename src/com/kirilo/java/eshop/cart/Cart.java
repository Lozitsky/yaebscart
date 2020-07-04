package com.kirilo.java.eshop.cart;

import java.util.List;

public interface Cart {
    public void add(int id, String title, String author, float price, int qtyOrdered);

    public boolean update(int id, int newQty);

    public void remove(int id);

    public int size();

    public boolean isEmpty();

    public List<CartItem> getItems();

    public void clear();
}
