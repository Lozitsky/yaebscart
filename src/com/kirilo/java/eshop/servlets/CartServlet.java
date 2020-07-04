package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.cart.Cart;
import com.kirilo.java.eshop.cart.CartItem;
import com.kirilo.java.eshop.cart.CartStore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CartServlet extends BaseServlet {

    @Override
    protected boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        // Retrieve current HTTPSession object. If none, create one.
        HttpSession session = req.getSession(true);
        Cart cart;
        synchronized (session.getId()) {  // synchronized to prevent concurrent updates
            // Retrieve the shopping cart for this session, if any. Otherwise, create one.
            cart = (Cart) session.getAttribute("cart");
            if (cart == null) {  // No cart, create one.
                cart = new CartStore();
                session.setAttribute("cart", cart);  // Save it into session
            }
        }

        String todo = req.getParameter("todo");
        if (todo == null) {
            todo = "view";  // to prevent null pointer}
        } else if (todo.equals("add") || todo.equals("update")) {
            // (1) todo=add id=1001 qty1001=5 [id=1002 qty1002=1 ...]
            // (2) todo=update id=1001 qty1001=5
            String[] ids = req.getParameterValues("id");
            if (ids == null) {
                out.println("<h3>Please Select a Book!</h3></body></html>");
                return false;
            }
            for (String id : ids) {
                String sqlStr = "SELECT * FROM books WHERE id = " + id;
                //System.out.println(sqlStr);  // for debugging
                ResultSet rset = statement.executeQuery(sqlStr);
                rset.next(); // Expect only one row in ResultSet
                String title = rset.getString("title");
                String author = rset.getString("author");
                float price = rset.getFloat("price");

                // Get quantity ordered - no error check!
                int qtyOrdered = Integer.parseInt(req.getParameter("qty" + id));
                int idInt = Integer.parseInt(id);
                if (todo.equals("add")) {
                    cart.add(idInt, title, author, price, qtyOrdered);
                } else {
                    cart.update(idInt, qtyOrdered);
                }
            }
        } else if (todo.equals("remove")) {
            String id = req.getParameter("id");  // Only one id for remove case
            cart.remove(Integer.parseInt(id));
        }
        // All cases - Always display the shopping cart
        if (cart.isEmpty()) {
            out.println("<p>Your shopping cart is empty</p>");
        } else {
            out.println("<table border='1' cellpadding='6'>");
            out.println("<tr>");
            out.println("<th>AUTHOR</th>");
            out.println("<th>TITLE</th>");
            out.println("<th>PRICE</th>");
            out.println("<th>QTY</th>");
            out.println("<th>REMOVE</th></tr>");

            float totalPrice = 0f;
            for (CartItem item : cart.getItems()) {
                int id = item.getId();
                String author = item.getAuthor();
                String title = item.getTitle();
                float price = item.getPrice();
                int qtyOrdered = item.getQtyOrdered();

                out.println("<tr>");
                out.println("<td>" + author + "</td>");
                out.println("<td>" + title +  "</td>");
                out.println("<td>" + price +  "</td>");

                out.println("<td><form method='get'>");
                out.println("<input type='hidden' name='todo' value='update' />");
                out.println("<input type='hidden' name='id' value='" + id + "' />");
                out.println("<input type='text' size='3' name='qty"
                        + id + "' value='" + qtyOrdered + "' />" );
                out.println("<input type='submit' value='Update' />");
                out.println("</form></td>");

                out.println("<td><form method='get'>");
                out.println("<input type='hidden' name='todo' value='remove' />");
                out.println("<input type='hidden' name='id' value='" + id + "' />");
                out.println("<input type='submit' value='Remove' />");
                out.println("</form></td>");
                out.println("</tr>");
                totalPrice += price * qtyOrdered;
            }
            out.println("<tr><td colspan='5' align='right'>Total Price: $");
            out.printf("%.2f</td></tr>", totalPrice);
            out.println("</table>");
        }

        out.println("<p><a href='start'>Select More Books...</a></p>");

        // Display the Checkout
        if (!cart.isEmpty()) {
            out.println("<br /><br />");
            out.println("<form method='get' action='checkout'>");
            out.println("<input type='submit' value='CHECK OUT'>");
            out.println("<p>Please fill in your particular before checking out:</p>");
            out.println("<table>");
            out.println("<tr>");
            out.println("<td>Enter your Name:</td>");
            out.println("<td><input type='text' name='cust_name' /></td></tr>");
            out.println("<tr>");
            out.println("<td>Enter your Email:</td>");
            out.println("<td><input type='text' name='cust_email' /></td></tr>");
            out.println("<tr>");
            out.println("<td>Enter your Phone Number:</td>");
            out.println("<td><input type='text' name='cust_phone' /></td></tr>");
            out.println("</table>");
            out.println("</form>");
        }
            return true;
    }

    @Override
    protected String createHeader() {
        return "YAEBS - Your Shopping Cart";
    }

    @Override
    protected String createTitle() {
        return "Shopping Cart";
    }

    @Override
    protected boolean validation(HttpServletRequest req, PrintWriter out) {
        return true;
    }
}
