package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.cart.Cart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EntryServlet extends BaseServlet {
    @Override
    protected boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        String sqlString = "select distinct author from books where qty > 0";
        final ResultSet resultSet = statement.executeQuery(sqlString);

        // Begin an HTML form
        out.println("<form method='get' action='search'>");
        // A pull-down menu of all the authors with a no-selection option
        out.println("Choose an Author: <select name='author' size='1'>");
        out.println("<option value=''>Select...</option>");  // no-selection
        while (resultSet.next()) {  // list all the authors
            String author = resultSet.getString("author");
            out.println("<option value='" + author + "'>" + author + "</option>");
        }
        out.println("</select><br />");
        out.println("<p>OR</p>");
        // A text field for entering search word for pattern matching
        out.println("Search \"Title\" or \"Author\": <input type='text' name='search' />");

        // Submit and reset buttons
        out.println("<br /><br />");
        out.println("<input type='submit' value='SEARCH' />");
        out.println("<input type='reset' value='CLEAR' />");
        out.println("</form>");

        // Show "View Shopping Cart" if the cart is not empty
        HttpSession session = req.getSession(false); // check if session exists
        if (session != null) {
            Cart cart;
            synchronized (session.getId()) {
                // Retrieve the shopping cart for this session, if any. Otherwise, create one.
                cart = (Cart) session.getAttribute("cart");
                if (cart != null && !cart.isEmpty()) {
                    out.println("<p><a href='cart?todo=view'>View Shopping Cart</a></p>");
                }
            }
        }

        return true;
    }

    @Override
    protected String createHeader() {
        return "Welcome to Yet Another E-BookShop";
    }

    @Override
    protected String createTitle() {
        return "Welcome to YAEshop";
    }

    @Override
    protected boolean validation(HttpServletRequest req, PrintWriter out) {
        return true;
    }

}
