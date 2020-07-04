package com.kirilo.java.eshop.servlets;

import com.kirilo.java.eshop.cart.Cart;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseServlet extends HttpServlet {
    private DataSource pool;

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            final InitialContext context = new InitialContext();
            final Context envContext = (Context) context.lookup("java:comp/env");

            pool = (DataSource) envContext.lookup("cartDataSource");
            if (pool == null) {
                throw new ServletException("Unknown DataSource 'jdbc/mysql_ebookshop'");
            }
        } catch (NamingException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Init connection error", e);
        }
    }

    //    https://refactoring.guru/design-patterns/template-method/java/example
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;");
        resp.setCharacterEncoding("UTF-8");

        //            https://stackoverflow.com/a/8106090/9586230
        //            https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
        try (PrintWriter out = resp.getWriter()) {
            out.println("<html><head><title>" + createTitle() + "</title></head><body>");
            out.println("<h2>" + createHeader() + "</h2>");
            if (validation(req, out)) {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                try (Connection connection = pool.getConnection();
                     final Statement statement = connection.createStatement()) {
                    // We shall manage our transaction (because multiple SQL statements issued)
                    connection.setAutoCommit(false);

                    if (createDynamicPageBody(out, statement, req)) {
                        connection.commit();
                    } else {
                        connection.rollback();
                    }

                    out.println("</body></html>");

                } catch (SQLException throwables) {
                    final HttpSession session = req.getSession(false);
                    if (session != null) {
                        synchronized (session.getId()) {
                            final Cart cart = (Cart) session.getAttribute("cart");
                            if (cart != null) {
                                cart.clear();
                            }
                        }
                    }
                    out.println("<h3>Service not available. Please try again later!</h3></body></html>");
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Connection error!", throwables);
                }
            }
        } catch (IllegalAccessException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "IllegalAccess!", e);
        } catch (InstantiationException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "InstantiationException!", e);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "ClassNotFound!", e);
        }
    }

    protected abstract boolean createDynamicPageBody(PrintWriter out, Statement statement, HttpServletRequest req) throws SQLException;

    protected abstract String createHeader();

    protected abstract String createTitle();

    protected abstract boolean validation(HttpServletRequest req, PrintWriter out);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
