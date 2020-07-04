package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.cart.Cart;
import com.kirilo.java.eshop.cart.CartItem;
import com.kirilo.java.eshop.utils.InputFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckoutServlet extends BaseServlet {

    private String custName;
    private String custEmail;
    private String custPhone;
    private Cart cart;

    @Override
    protected boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        // Display the name, email and phone (arranged in a table)
        out.println("<table>");
        out.println("<tr>");
        out.println("<td>Customer Name:</td>");
        out.println("<td>" + custName + "</td></tr>");
        out.println("<tr>");
        out.println("<td>Customer Email:</td>");
        out.println("<td>" + custEmail + "</td></tr>");
        out.println("<tr>");
        out.println("<td>Customer Phone Number:</td>");
        out.println("<td>" + custPhone + "</td></tr>");
        out.println("</table>");

        // Print the book(s) ordered in a table
        out.println("<br />");
        out.println("<table border='1' cellpadding='6'>");
        out.println("<tr>");
        out.println("<th>AUTHOR</th>");
        out.println("<th>TITLE</th>");
        out.println("<th>PRICE</th>");
        out.println("<th>QTY</th></tr>");

        float totalPrice = 0f;
        for (CartItem item : cart.getItems()) {
            int id = item.getId();
            String author = item.getAuthor();
            String title = item.getTitle();
            int qtyOrdered = item.getQtyOrdered();
            float price = item.getPrice();

            String sqlStr = "SELECT qty FROM books WHERE id = " + id;
            //System.out.println(sqlStr);  // for debugging
            ResultSet rset = statement.executeQuery(sqlStr);
            // Expect only one row in ResultSet
            rset.next();
            int qtyAvailable = rset.getInt("qty");

            // No check for price and qtyAvailable change
            // Update the books table and insert an order record
            // Validate quantity ordered
//            final int qtyOrdered = InputFilter.parsePositiveInt(req.getParameter("qty" + id));
            if (qtyOrdered <= 0) {
                out.println("<h3>Please Enter a valid quantity for \"" + title + "\"!</h3>");
                return false;
            } else if (qtyOrdered > qtyAvailable) {
                out.println("<h3>There are insufficient copies of \"" + title + "\" available!</h3>");
                return false;
            } else {
                sqlStr = "UPDATE books SET qty = qty - " + qtyOrdered + " WHERE id = " + id;
                //System.out.println(sqlStr);  // for debugging
                statement.executeUpdate(sqlStr);

                sqlStr = "INSERT INTO order_records values ("
                        + id + ", " + qtyOrdered + ", '" + custName + "', '"
                        + custEmail + "', '" + custPhone + "')";
                //System.out.println(sqlStr);  // for debugging
                statement.executeUpdate(sqlStr);

                // Show the book ordered
                out.println("<tr>");
                out.println("<td>" + author + "</td>");
                out.println("<td>" + title + "</td>");
                out.println("<td>" + price + "</td>");
                out.println("<td>" + qtyOrdered + "</td></tr>");
                totalPrice += price * qtyOrdered;
            }
        }
        out.println("<tr><td colspan='4' align='right'>Total Price: $");
        out.printf("%.2f</td></tr>", totalPrice);
        out.println("</table>");

        out.println("<h3>Thank you.</h3>");
        out.println("<a href='start'>Back to Search Menu</a>");
        cart.clear();   // empty the cart
        return true;
    }

    @Override
    protected String createHeader() {
        return "YAEBS - Checkout";
    }

    @Override
    protected String createTitle() {
        return "Checkout";
    }

    @Override
    protected boolean validation(HttpServletRequest req, PrintWriter out) {
        // Retrieve the Cart
        HttpSession session = req.getSession(false);
        if (session == null) {
            out.println("<h3>Your Shopping cart is empty!</h3></body></html>");
            return false;
        }
        synchronized (session.getId()) {
            cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                out.println("<h3>Your Shopping cart is empty!</h3></body></html>");
                return false;
            }
        }

        // Retrieve and process request parameters: id(s), cust_name, cust_email, cust_phone
        custName = req.getParameter("cust_name");
        boolean hasCustName = custName != null &&
                ((custName = InputFilter.htmlFilter(custName.trim())).length() > 0);
        custEmail = req.getParameter("cust_email");
        boolean hasCustEmail = custEmail != null &&
                ((custEmail = InputFilter.htmlFilter(custEmail.trim())).length() > 0);
        custPhone = req.getParameter("cust_phone");
        boolean hasCustPhone = custPhone != null &&
                ((custPhone = InputFilter.htmlFilter(custPhone.trim())).length() > 0);

        // Validate inputs
        if (!hasCustName) {
            out.println("<h3>Please Enter Your Name!</h3></body></html>");
            return false;
        } else if (!hasCustEmail || (custEmail.indexOf('@') == -1)) {
            out.println("<h3>Please Enter Your email (user@host)!</h3></body></html>");
            return false;
        } else if (!hasCustPhone || !InputFilter.isValidPhone(custPhone)) {
            out.println("<h3>Please Enter an 8-digit Phone Number!</h3></body></html>");
            return false;
        }

        return true;
    }
}
