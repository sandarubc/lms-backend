package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;

@WebServlet(name = "IssueNoteServlet", value = "/issue-note")
public class IssueNoteServlet extends HttpServlet {

    @Resource(lookup = "java:comp/env/jdbc/dep9-lms")
    private DataSource pool;
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("IssueNote: doPost()");
    }
}
