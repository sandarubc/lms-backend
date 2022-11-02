package lk.ijse.dep9.lmsbackend.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.db.ConnectionPool;

import java.io.IOException;

@WebServlet(name = "ReleaseAllConnectionServlet", value = "/release")
public class ReleaseAllConnectionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ConnectionPool pool = (ConnectionPool) getServletContext().getAttribute("pool");
        pool.releaseAllConnection();
    }

}
