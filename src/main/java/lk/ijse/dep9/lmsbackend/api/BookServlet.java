package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.api.util.HttpServlet2;
import lk.ijse.dep9.lmsbackend.dto.BooksDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "BookServlet", value = "/books/*")
public class BookServlet extends HttpServlet2 {

    @Resource(lookup="java:/comp/env/jdbc/dep9_lms")
    private DataSource pool;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String q = request.getParameter("q");
        String size = request.getParameter("size");
        String page = request.getParameter("page");
        String path = request.getPathInfo();
        if(path==null || path.equals("/")) {

            if (!(q == null || size == null || page == null)) {
                searchBookByPage(q,Integer.parseInt(size),Integer.parseInt(page),response);
            } else if (!(size == null || page == null)) {
                paginatedBooks(Integer.parseInt(size),Integer.parseInt(page),response);
            } else if (q != null) {
                searchBook(q,response);
            } else {
                loadAllBooks(response);
            }
        }else{
            if(path.matches("^/[0-9]+-[0-9]+-[0-9]+-[0-9]+-[0-9]+/?$") && path.matches("^/[0-9-]{17}/?")){
                Pattern compile = Pattern.compile("^/([0-9]+-[0-9]+-[0-9]+-[0-9]+-[0-9]+)/?$");
                Matcher matcher = compile.matcher(path);
                matcher.matches();
                String isbn = matcher.group(1);
                getBook(isbn,response);

            }else{
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    private void loadAllBooks(HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM book");
            ArrayList<BooksDTO> booksDTO = new ArrayList<>();
            while(rst.next()){
                String isbn = rst.getString("isbn");
                String title = rst.getString("title");
                String author = rst.getString("author");
                int copies = rst.getInt("copies");
                BooksDTO book = new BooksDTO(isbn, title, author, copies);
                booksDTO.add(book);
            }
            Jsonb jsonb = JsonbBuilder.create();
            String books = jsonb.toJson(booksDTO);

            response.setContentType("application/json");

            response.getWriter().println(books);

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void getBook(String bookIsbn, HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()){
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM book WHERE isbn=?");

            stm.setString(1,bookIsbn);
            ResultSet rst = stm.executeQuery();

            if(rst.next()){
                String isbn = rst.getString("isbn");
                String title = rst.getString("title");
                String author = rst.getString("author");
                int copies = rst.getInt("copies");
                BooksDTO booksDTO = new BooksDTO(isbn, title, author, copies);
                Jsonb jsonb = JsonbBuilder.create();
                String book = jsonb.toJson(booksDTO);

                response.setHeader("Access-Control-Allow-Origin","*");
                response.setContentType("application/json");

                response.getWriter().println(book);

            }else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void searchBook(String query,HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()){

            query="%"+query+"%";

            PreparedStatement stm = connection.prepareStatement("SELECT * FROM book WHERE isbn LIKE ? OR title LIKE ? OR author LIKE ?");
            stm.setString(1,query);
            stm.setString(2,query);
            stm.setString(3,query);

            ResultSet rst = stm.executeQuery();

            ArrayList<BooksDTO> booksDTO = new ArrayList<>();
            while(rst.next()){
                String isbn = rst.getString("isbn");
                String title = rst.getString("title");
                String author = rst.getString("author");
                int copies = rst.getInt("copies");

                BooksDTO book = new BooksDTO(isbn, title, author, copies);
                booksDTO.add(book);
            }

            Jsonb jsonb = JsonbBuilder.create();
            String books = jsonb.toJson(booksDTO);
            response.setContentType("application/json");
            response.getWriter().println(books);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void paginatedBooks(int size,int page, HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()) {

            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT COUNT(isbn) AS count1 FROM book");
            rst.next();
            int count = rst.getInt("count1");
            response.setHeader("X-Total-Count",String.valueOf(count));
            PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM book LIMIT ? OFFSET ?");

            stm2.setInt(1,size);
            stm2.setInt(2,(page-1)*size);
            ResultSet rst2 = stm2.executeQuery();
            ArrayList<BooksDTO> books = new ArrayList<>();
            while(rst2.next()){
                String isbn = rst2.getString("isbn");
                String title = rst2.getString("title");
                String author = rst2.getString("author");
                int copies = rst2.getInt("copies");
                BooksDTO booksDTO = new BooksDTO(isbn, title, author, copies);
                books.add(booksDTO);

            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonBooks = jsonb.toJson(books);
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setContentType("application/json");
            response.getWriter().println(jsonBooks);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
    private void searchBookByPage(String query, int size, int page, HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()) {

            query="%"+query+"%";
            PreparedStatement stm = connection.prepareStatement("SELECT COUNT(isbn) AS count1 FROM book WHERE isbn LIKE ? OR title LIKE ? OR author LIKE ?");


            PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM book WHERE isbn LIKE ? OR title LIKE ? OR author LIKE ? LIMIT ? OFFSET ?");
            for (int i = 0; i < 3; i++) {
                stm.setString(i+1,query);
                stm2.setString(i+1,query);
            }
            stm2.setInt(4,size);
            stm2.setInt(5,(page-1)*size);
            ResultSet rst2 = stm2.executeQuery();
            ResultSet rst = stm.executeQuery();
            rst.next();
            int count = rst.getInt("count1");
            response.setHeader("X-Total-Count",String.valueOf(count));
            ArrayList<BooksDTO> books = new ArrayList<>();
            while(rst2.next()){
                String isbn = rst2.getString("isbn");
                String title = rst2.getString("title");
                String author = rst2.getString("author");
                int copies = rst2.getInt("copies");
                BooksDTO booksDTO = new BooksDTO(isbn, title, author, copies);
                books.add(booksDTO);

            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonBooks = jsonb.toJson(books);
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setContentType("application/json");
            response.getWriter().println(jsonBooks);
            System.out.println(jsonBooks);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

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
