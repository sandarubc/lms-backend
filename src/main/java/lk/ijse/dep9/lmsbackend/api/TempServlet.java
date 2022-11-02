package lk.ijse.dep9.lmsbackend.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.api.util.HttpServlet2;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TempServlet", value = {"/temp/*","*.php"})
public class TempServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        try(PrintWriter out = response.getWriter()){
            out.printf("<p>Request URI: %s</p>",request.getRequestURI());
            out.printf("<p>Request URI: %s</p>",request.getRequestURI());
            out.printf("<p>Request URI: %s</p>",request.getServletPath());
            out.printf("<p>Servlet Path: %s</p>",request.getContextPath());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
