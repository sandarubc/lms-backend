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

@WebServlet(name = "IssuNoteServlet", value = "/issue-notes/*")
public class IssuNoteServlet extends HttpServlet {

    @Resource(lookup = "java:comp/env/jdbc/dep9-lms")
    DataSource pool;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println(request.getPathInfo());
        if(request.getPathInfo() != null || !request.getPathInfo().equals("/")){
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
        try{
            if(request.getContentType() == null || !request.getContentType().startsWith("application/json")){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                return;
            }
            IssueNoteDTO issueNoteDTO = JsonbBuilder.create().fromJson(request.getReader(), IssueNoteDTO.class);
            createIssueNote(issueNoteDTO,response);

        }
        catch (JsonbException e){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }


    private void createIssueNote(IssueNoteDTO issueNoteDTO, HttpServletResponse response) throws IOException {

//        Data Validation
        if(issueNoteDTO.getMemberId()==null || !issueNoteDTO.getMemberId().matches("([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})")){
            throw new JsonbException("Invalid Member ID");
        } else if (issueNoteDTO.getBooks().isEmpty()) {
            throw new JsonbException("A issue note requires at least one book");
        } else if (issueNoteDTO.getBooks().size()>3) {
            throw new JsonbException("Member can't borrow more than 3 books");
        }else if(issueNoteDTO.getBooks().stream().anyMatch(isbn -> isbn.matches("([0-9][0-9\\\\-]*[0-9])"))){
            throw new JsonbException("Invalid isbn has been found");
        }else if(issueNoteDTO.getBooks().stream().collect(Collectors.toSet()).size() !=
        issueNoteDTO.getBooks().size()){
            throw new JsonbException("");
        }

        try(Connection connection = pool.getConnection()){

            PreparedStatement stmExist = connection.prepareStatement("SELECT id FROM member WHERE id=?");
            stmExist.setString(1,issueNoteDTO.getMemberId());
            if(!stmExist.executeQuery().next()){
                throw new JsonbException("Member does not exist within the database");
            }

            PreparedStatement stm = connection.prepareStatement("SELECT book.isbn,title,copies, COUNT(ii.isbn), COUNT(title), copies - COUNT(ii.isbn) FROM book\n" +
                    "LEFT OUTER JOIN issue_item ii on book.isbn = ii.isbn\n" +
                    "LEFT OUTER JOIN `return` r on ii.issue_id = r.issue_id and ii.isbn = r.isbn\n" +
                    "WHERE r.date IS NULL and book.isbn=? GROUP BY book.isbn;");

            PreparedStatement stmDuplicate = connection.prepareStatement("SELECT * FROM member\n" +
                    "INNER JOIN issue_note `in` on member.id = `in`.member_id\n" +
                    "LEFT OUTER JOIN issue_item ii on `in`.id = ii.issue_id\n" +
                    "LEFT OUTER JOIN `return` r on ii.issue_id = r.issue_id and ii.isbn = r.isbn\n" +
                    "WHERE r.date AND member.id=? AND ii.isbn=?;");
            stmDuplicate.setString(1,issueNoteDTO.getMemberId());
            for(String isbn:issueNoteDTO.getBooks()){

                stm.setString(1,isbn);
                ResultSet rst = stm.executeQuery();
                if(!rst.next()) throw new JsonbException(isbn + " book doesn't exist within the database");
                if(!rst.getBoolean("availability")){
                    throw new JsonbException(isbn + " book is not available at the moment");
                }
                stmDuplicate.setString(2,isbn);
                ResultSet rst2 = stmDuplicate.executeQuery();
                if (rst2.next()) {
                    throw new JsonbException(isbn+ " book is already issued to the same member");
                }

                try{
                    connection.setAutoCommit(false);
                    PreparedStatement stmIssueNote = connection.prepareStatement("INSERT INTO issue_note (date, member_id) VALUES (?,?)");
                    stmIssueNote.setDate(1, Date.valueOf(LocalDate.now()));
                    stmIssueNote.setString(2,issueNoteDTO.getMemberId());
                    if(stmIssueNote.executeUpdate() !=1 ) throw new SQLException("Failed to insert the issue note");

                    ResultSet generatedKeys = stmIssueNote.getGeneratedKeys();
                    generatedKeys.next();

                    int issueNoteId=generatedKeys.getInt(1);

                    PreparedStatement stmIssueItem = connection.prepareStatement("INSERT INTO issue_item (issue_id, isbn) VALUES (?,?)");
                    stmIssueItem.setInt(1,issueNoteId);

                    for(String isbn1:issueNoteDTO.getBooks()){

                        stmIssueItem.setString(2,isbn1);
                        if(stmIssueItem.executeUpdate()!=1) throw new JsonbException("Failed to place the issu note");
                    }



                }catch (Throwable t){
                    connection.rollback();
                    t.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to place the issue note");
                }
                finally {
                    connection.setAutoCommit(true);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to place the issue note");
        }

    }
}
