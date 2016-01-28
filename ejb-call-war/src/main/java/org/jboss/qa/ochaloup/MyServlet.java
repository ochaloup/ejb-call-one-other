package org.jboss.qa.ochaloup;

import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name="MyServlet", urlPatterns={"/"})
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @EJB
    private EjbBean bean;
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("<h4>Called " + EjbBean.class.getName()  + "</h4>");
        
        bean.callNext();
    }
}
