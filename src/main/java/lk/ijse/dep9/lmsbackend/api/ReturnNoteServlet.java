package lk.ijse.dep9.lmsbackend.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "ReturnNoteServlet", value = "/return-note")
public class ReturnNoteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Return Note: doPost()");
    }
}
