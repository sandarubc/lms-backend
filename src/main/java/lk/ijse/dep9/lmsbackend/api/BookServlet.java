package lk.ijse.dep9.lmsbackend.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.api.util.HttpServlet2;

import java.io.IOException;

@WebServlet(name = "BookServlet", value = "/books")
public class BookServlet extends HttpServlet2 {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().println("Book: doGet()");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("Book: doPost()");
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("Book: doPatch()");
    }
}
