package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.dto.IssueNoteDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@WebServlet(name = "IssueNoteServlet", value = "/issue-note2/*")
public class IssueNoteServlet2 extends HttpServlet {

    @Resource(lookup = "java:comp/env/jdbc/dep9-lms")
    private DataSource pool;


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("sandaru");
        resp.getWriter().println("IssueNote: doPost()");


        if(req.getPathInfo()!=null && !req.getPathInfo().equals("/")){
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }

        try{
            if(req.getContentType()==null || req.getContentType().startsWith("application/json")){

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid Json");
            return;

        }
            IssueNoteDTO issueNoteDTO = JsonbBuilder.create().fromJson(req.getReader(), IssueNoteDTO.class);
            createIssueNote(issueNoteDTO,resp);

        }catch (JsonbException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid Json");
        }
    }

    private void createIssueNote(IssueNoteDTO issueNote, HttpServletResponse response){

        if(issueNote.getMemberId()==null ||
            !issueNote.getMemberId().matches("([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})")){
            throw new JsonbException("Member id is empty or invalid");
        } else if (issueNote.getBooks().isEmpty()) {
            throw new JsonbException("Can't place an issue note without book");
        } else if (issueNote.getBooks().size()>3) {
            throw new JsonbException("Can't issue more books");

        } else if(issueNote.getBooks().stream().anyMatch(isbn->isbn.matches("[0-9][0-9\\\\-]*[0-9]"))){
            throw new JsonbException("Invalid isbn in book list");
        } else if (issueNote.getBooks().stream().collect(Collectors.toSet()).size()
            != issueNote.getBooks().size()){
            throw new JsonbException("Duplicate isbns are found");
        }
        try(Connection connection = pool.getConnection()){
            PreparedStatement stm = connection.prepareStatement("SELECT id FROM member WHERE id=?");
            stm.setString(1, issueNote.getMemberId());

            if(!stm.executeQuery().next()){
                throw new JsonbException("Member doesn't exist");
            }
            PreparedStatement stm2 = connection.prepareStatement("SELECT b.title FROM issue_item ii\n" +
                    "    INNER JOIN `return` r ON NOT (ii.issue_id=r.issue_id and ii.isbn=r.isbn)\n" +
                    "    INNER JOIN book b on ii.isbn=b.isbn\n" +
                    "    INNER JOIN issue_note `in` on ii.issue_id = `in`.id\n" +
                    "    WHERE in.member_id=? AND b.isbn=?");
            stm2.setString(1,issueNote.getMemberId());

            for(String isbn: issueNote.getBooks()){
                stm.setString(1,isbn);
                stm2.setString(2,isbn);
                ResultSet rst = stm.executeQuery();
                ResultSet rst2 = stm2.executeQuery();
                if(rst2.next()) throw new JsonbException("Book has been already issued");
                if(!rst.next()) throw new JsonbException("Book doesn't exist");

                if(!rst.getBoolean("availability")){
                    throw new JsonbException(isbn + " is not available at the moment");
                }
                PreparedStatement stmAvailable = connection.prepareStatement("SELECT m.name, 3 - COUNT(r.issue_id) as available FROM issue_note\n" +
                        "    INNER JOIN issue_item ii on issue_note.id = ii.issue_id\n" +
                        "    INNER JOIN return r ON NOT(ii.issue_id = r.issue_id and ii.isbn = r.isbn)\n" +
                        "    RIGHT OUTER JOIN member m on issue_note.member_id = m.id\n" +
                        "    WHERE m.id = ? GROUP BY m.id;");
                stmAvailable.setString(1,issueNote.getMemberId());
                ResultSet rstAvailability = stmAvailable.executeQuery();
                rstAvailability.next();
                int available = rstAvailability.getInt("available");

                if(issueNote.getBooks().size() > available){
                    throw new JsonbException("Member can borrow only " + available + " books");
                }
                try{
                    connection.setAutoCommit(false);
                    PreparedStatement stmIssueNote = connection.prepareStatement("INSERT INTO issue_note (date, member_id) VALUES (?, ?)");
                    stmIssueNote.setDate(1, Date.valueOf(LocalDate.now()));
                    stmIssueNote.setString(2, issueNote.getMemberId());

                    if(stmIssueNote.executeUpdate() != 1){
                        throw new SQLException("Failed to Insert the issue note");
                    }

                    ResultSet generatedKeys = stmIssueNote.getGeneratedKeys();
                    generatedKeys.next();
                    int issueNoteId = generatedKeys.getInt(1);

                    PreparedStatement stmIssueItem = connection.prepareStatement("INSERT INTO issue_item (issue_id, isbn) VALUES (?, ?)");
                    stmIssueItem.setInt(1,issueNoteId);
                    for (String book_isbn : issueNote.getBooks()) {
                        stmIssueItem.setString(2,book_isbn);
                        if(stmIssueItem.executeUpdate() != 1){
                            throw new SQLException("Failde to insert an issue item");
                        }

                    }
                    connection.commit();
                    issueNote.setDate(LocalDate.now());
                    issueNote.setId(issueNoteId);
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setContentType("application/json");
                    String json = JsonbBuilder.create().toJson(issueNote);


                }catch (Throwable t){
                    connection.rollback();
                }finally {
                    connection.setAutoCommit(true);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
