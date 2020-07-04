package com.kirilo.java.eshop.cart;

import java.util.ArrayList;
import java.util.List;

public class CartStore implements Cart {
    private List<CartItem> cart;

    public CartStore() {
        cart = new ArrayList<>();
    }

    @Override
    public void add(int id, String title, String author, float price, int qtyOrdered) {
        for (CartItem item : cart) {
            if (item.getId() == id) {
                item.setQtyOrdered(item.getQtyOrdered() + qtyOrdered);
                return;
            }
        }
        cart.add(new CartItem(id, title, author, price, qtyOrdered));
    }

    @Override
    public boolean update(int id, int newQty) {
        for (CartItem cartItem : cart) {
            if (cartItem.getId() == id) {
                cartItem.setQtyOrdered(newQty);
                return true;
            }
        }
        return false;
    }

    @Override
    public void remove(int id) {
        for (CartItem cartItem : cart) {
            if (cartItem.getId() == id) {
                cart.remove(cartItem);
                return;
            }
        }
        System.out.println("Remove: " + id);
    }

    @Override
    public int size() {
        return cart.size();
    }

    @Override
    public boolean isEmpty() {
        return cart.isEmpty();
    }

    @Override
    public List<CartItem> getItems() {
        return cart;
    }

    @Override
    public void clear() {
        cart.clear();
    }
}
