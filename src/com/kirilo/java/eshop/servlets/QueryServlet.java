package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.cart.Cart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryServlet extends BaseServlet {
    private boolean hasAuthor;
    private boolean hasSearch;
    private String author;
    private String searchWord;

    @Override
    protected boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException {
        // Form a SQL command based on the param(s) present
        StringBuilder sqlStr = new StringBuilder();  // more efficient than String
        sqlStr.append("SELECT * FROM books WHERE qty > 0 AND (");
        if (hasAuthor) {
            sqlStr.append("author = '").append(author).append("'");
        }
        if (hasSearch) {
            if (hasAuthor) {
                sqlStr.append(" OR ");
            }
            sqlStr.append("author LIKE '%").append(searchWord)
                    .append("%' OR title LIKE '%").append(searchWord).append("%'");
        }
        sqlStr.append(") ORDER BY author, title");
        //System.out.println(sqlStr);  // for debugging
        ResultSet rset = statement.executeQuery(sqlStr.toString());

        if (!rset.next()) {  // Check for empty ResultSet (no book found)
            out.println("<h3>No book found. Please try again!</h3>");
            out.println("<p><a href='start'>Back to Select Menu</a></p>");
        } else {
            // Print the result in an HTML form inside a table
            out.println("<form method='get' action='cart'>");
            out.println("<input type='hidden' name='todo' value='add' />");
            out.println("<table border='1' cellpadding='6'>");
            out.println("<tr>");
            out.println("<th>&nbsp;</th>");
            out.println("<th>AUTHOR</th>");
            out.println("<th>TITLE</th>");
            out.println("<th>PRICE</th>");
            out.println("<th>QTY</th>");
            out.println("</tr>");

            // ResultSet's cursor now pointing at first row
            do {
                // Print each row with a checkbox identified by book's id
                String id = rset.getString("id");
                out.println("<tr>");
                out.println("<td><input type='checkbox' name='id' value='" + id + "' /></td>");
                out.println("<td>" + rset.getString("author") + "</td>");
                out.println("<td>" + rset.getString("title") + "</td>");
                out.println("<td>$" + rset.getString("price") + "</td>");
                out.println("<td><input type='text' size='3' value='1' name='qty" + id + "' /></td>");
                out.println("</tr>");
            } while (rset.next());
            out.println("</table><br />");

            // Ask for name, email and phone using text fields (arranged in a table)
/*            out.println("<table>");
            out.println("<tr><td>Enter your Name:</td>");
            out.println("<td><input type='text' name='cust_name' /></td></tr>");
            out.println("<tr><td>Enter your Email (user@host):</td>");
            out.println("<td><input type='text' name='cust_email' /></td></tr>");
            out.println("<tr><td>Enter your Phone Number (8-digit):</td>");
            out.println("<td><input type='text' name='cust_phone' /></td></tr></table><br />");*/

            // Submit and reset buttons
            out.println("<input type='submit' value='Add to My Shopping Cart' />");
            out.println("<input type='reset' value='CLEAR' /></form>");

            // Hyperlink to go back to search menu
            out.println("<p><a href='start'>Back to Select Menu</a></p>");

            // Show "View Shopping Cart" if cart is not empty
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
        return false;
    }

    @Override
    protected String createHeader() {
        return "YAEBS - Query Results";
    }

    @Override
    protected String createTitle() {
        return "Query Results";
    }

    @Override
    protected boolean validation(HttpServletRequest req, PrintWriter out) {
        author = req.getParameter("author");
        hasAuthor = author != null && !author.equals("Select...");
        searchWord = req.getParameter("search");
        hasSearch = searchWord != null && (searchWord = searchWord.trim()).length() > 0;
        if (!hasAuthor && !hasSearch) {
            out.println("<h3>Please select an author or enter a search term!</h3>");
            out.println("<p><a href='start'>Back to Select Menu</a></p>");
            return false;
        }
        return true;
    }
}
