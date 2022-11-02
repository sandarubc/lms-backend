package lk.ijse.dep9.lmsbackend.api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;

@WebServlet(name = "IssueNoteServlet", value = "/issue-note")
public class IssueNoteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("IssueNote: doPost()");
    }
}
