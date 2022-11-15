package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.api.util.HttpServlet2;
import lk.ijse.dep9.lmsbackend.dto.MemberDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "MemberServlet", value = "/members/*",loadOnStartup = 0)
public class MemberServlet extends HttpServlet2 {


//    @Resource(lookup = "jdbc/lms")//For GlassFish

    @Resource(lookup = "java:/comp/env/jdbc/dep9_lms")
    private DataSource pool;

    /*@Override
    public void init() throws ServletException {
        try {
            InitialContext ctx = new InitialContext();
            pool = (DataSource) ctx.lookup("jdbc/lms");
            System.out.println(pool);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(req.getPathInfo()==null || req.getPathInfo().equals("/")){

            String query = req.getParameter("q");
            String size = req.getParameter("size");
            String page = req.getParameter("page");
            if(query!=null && size !=null && page !=null){
                if(!size.matches("\\d+")||!page.matches("\\d+")){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
                else{

                    searchMembersByPage(query,Integer.parseInt(size),Integer.parseInt(page),resp);
                }
            }else if(size!=null && page!=null){
                if(!size.matches("\\d+")||!page.matches("\\d+")){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
                else{

                    loadPaginatedMembers(Integer.parseInt(size),Integer.parseInt(page),resp);
                }

            }else if(query!=null){
                searchMembers(query,resp);

            }else{
                loadAllMembers(resp);
            }

        }
        else{
            Pattern compile = Pattern.compile("^/([A-Za-z0-9]{8}-([A-Za-z0-9]{4}-){3}[A-Za-z0-9]{12})/?$");
            Matcher matcher = compile.matcher(req.getPathInfo());
            if(matcher.matches()){
                String uid = matcher.group(1);
                getMember(uid,resp);

            }

        }
    }

    private void getMember(String uid,HttpServletResponse resp) throws IOException {
        try(Connection connection = pool.getConnection()) {

            PreparedStatement stm = connection.prepareStatement("SELECT * FROM member WHERE id=?");
            stm.setString(1,uid);
            ResultSet rst = stm.executeQuery();

            if(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MemberDTO memberDTO = new MemberDTO(id, name, address, contact);
                Jsonb jsonb = JsonbBuilder.create();
//                resp.setHeader("Access-Control-Allow-Origin","*");
                resp.setContentType("application/json");
                jsonb.toJson(memberDTO,resp.getWriter());


            }else{
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void loadAllMembers(HttpServletResponse response) throws  IOException {

        try(Connection connection = pool.getConnection()) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM member");
            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();

            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MemberDTO memberDTO = new MemberDTO(id, name, address, contact);
                memberDTOS.add(memberDTO);
            }

//            response.addHeader("Access-Control-Allow-Origin","http://localhost:5500");
//            response.addHeader("Access-Control-Allow-Origin","*");
            response.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(memberDTOS);
            response.getWriter().println(jsonMembers);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    protected void loadPaginatedMembers(int size,int page,HttpServletResponse resp) throws IOException {



        try(Connection connection = pool.getConnection()) {

//            resp.setHeader("Access-Control-Allow-Origin","*");
//            resp.setHeader("Access-Control-Allow-Headers","X-Total-Count");
//            resp.setHeader("Access-Control-Expose-Headers","X-Total-Count");
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM member LIMIT ? OFFSET ?");
            stm.setInt(1,size);
            stm.setInt(2,(page-1)*size);
            ResultSet rst = stm.executeQuery();

            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();

            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MemberDTO memberDTO = new MemberDTO(id, name, address, contact);
                memberDTOS.add(memberDTO);

            }
            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(memberDTOS);

            resp.setContentType("application/json");


            Statement stm2 = connection.createStatement();
            ResultSet rst2 = stm2.executeQuery("SELECT COUNT(id) AS count1 FROM member");
            rst2.next();
            String count = rst2.getString("count1");
            resp.setHeader("X-Total-Count",count);


            resp.getWriter().println(jsonMembers);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void searchMembers(String query,HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()){
            String sql = "SELECT * FROM member WHERE id LIKE ? OR name LIKE ? OR address LIKE ? OR contact LIKE ?";
            PreparedStatement stm = connection.prepareStatement(sql);
            query="%"+query+"%";
            for (int i = 0; i < sql.split("[?]").length; i++) {
                stm.setString(i+1,query);
            }
            ResultSet rst = stm.executeQuery();

            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();
            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MemberDTO dto = new MemberDTO(id, name, address, contact);

                memberDTOS.add(dto);
            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(memberDTOS);


            response.getWriter().println(jsonMembers);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void searchMembersByPage(String query, int size,int page,HttpServletResponse response) throws IOException {

//        response.setHeader("Access-Control-Allow-Origin","*");
//        response.setHeader("Access-Control-Allow-Headers","X-Total-Count");
//        response.setHeader("Access-Control-Expose-Headers","X-Total-Count");
        try(Connection connection = pool.getConnection()) {
            String sqlCount = "SELECT COUNT(id) AS count1 FROM member WHERE id LIKE ? OR name LIKE ? OR address LIKE ? OR contact LIKE ?";
            String sql = "SELECT * FROM member WHERE id LIKE ? OR name LIKE ? OR address LIKE ? OR contact LIKE ? LIMIT ? OFFSET ?";
            PreparedStatement stm1 = connection.prepareStatement(sqlCount);
            PreparedStatement stm = connection.prepareStatement(sql);
            query="%"+query+"%";
            for (int i = 0; i < sql.split("[?]").length-2; i++) {
                stm1.setString(i+1,query);
                stm.setString(i+1,query);
            }
            stm.setInt(5,size);
            stm.setInt(6,(page-1)*size);

            ResultSet rst = stm.executeQuery();

            ResultSet rst1 = stm1.executeQuery();
            rst1.next();
            String count = rst1.getString("count1");
            response.setHeader("X-Total-Count",count);


            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();
            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MemberDTO dto = new MemberDTO(id, name, address, contact);


                memberDTOS.add(dto);
            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(memberDTOS);


            response.getWriter().println(jsonMembers);


        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            try {
                if (request.getContentType() == null || !request.getContentType().startsWith("application/json")) {
                    throw new JsonbException("Invalid JSON");
                }

                MemberDTO member = JsonbBuilder.create().
                        fromJson(request.getReader(), MemberDTO.class);

                if (member.getName() == null ||
                        !member.getName().matches("[A-Za-z ]+")) {
                    throw new JsonbException("Name is empty or invalid");
                } else if (member.getContact() == null ||
                        !member.getContact().matches("\\d{3}-\\d{7}")) {
                    throw new JsonbException("Contact is empty or invalid");
                } else if (member.getAddress() == null ||
                        !member.getAddress().matches("^[A-Za-z0-9|,.:;#\\/\\\\ -]+$")) {
                    throw new JsonbException("Address is empty or invalid");
                }

                try (Connection connection = pool.getConnection()) {
                    member.setId(UUID.randomUUID().toString());
                    PreparedStatement stm = connection.
                            prepareStatement("INSERT INTO member (id, name, address, contact) VALUES (?, ?, ?, ?)");
                    stm.setString(1, member.getId());
                    stm.setString(2, member.getName());
                    stm.setString(3, member.getAddress());
                    stm.setString(4, member.getContact());

                    int affectedRows = stm.executeUpdate();
                    if (affectedRows == 1) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                        response.setContentType("application/json");
//                        response.setHeader("Access-Control-Allow-Origin", "*");
                        JsonbBuilder.create().toJson(member, response.getWriter());
                        System.out.println(JsonbBuilder.create().toJson(member));
                    } else {
                        throw new SQLException("Something went wrong");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } catch (JsonbException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }

//        response.addHeader("Access-Control-Allow-Origin","*");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if(req.getPathInfo()==null || req.getPathInfo().equals('/')){
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else if (req.getPathInfo().matches("^/([A-Fa-f0-9]{8}-([A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12})/?$")) {

            Pattern compile = Pattern.compile("^/([A-Fa-f0-9]{8}-([A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12})/?$");
            Matcher matcher = compile.matcher(req.getPathInfo());

            if(matcher.matches()){
                String id = matcher.group(1);
                deleteMember(id,resp);

            }

        }
//        resp.setHeader("Access-Control-Allow-Origin","*");
    }

    private void deleteMember(String id, HttpServletResponse response) throws IOException {

        try(Connection connection = pool.getConnection()) {

            PreparedStatement stm = connection.prepareStatement("DELETE FROM member WHERE id=?");
            stm.setString(1,id);

            int i = stm.executeUpdate();

            if(i==1){
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
            else{
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }



        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

//    @Override
//    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//
//        resp.setHeader("Access-Control-Allow-Origin","*");
//        resp.setHeader("Access-Control-Allow-Methods","POST, GET, PATCH, DELETE, HEAD, OPTIONS, PUT");
//
//        String headers = req.getHeader("Access-Control-Request-Headers");
//
//        if(headers!=null){
//            resp.setHeader("Access-Control-Allow-Headers",headers);
//            resp.setHeader("Access-Control-Expose-Headers",headers);
//        }
//
//    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {

        if(req.getPathInfo()==null || req.getPathInfo().equals("/")){
            res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }

        Pattern compile = Pattern.compile("^/([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})/?$");
        Matcher matcher = compile.matcher(req.getPathInfo());
        if(matcher.matches()){

            updateMember(matcher.group(1),req,res);
        }
    }
    
    
    private void updateMember(String memberId, HttpServletRequest request, HttpServletResponse response) throws IOException {




        try{
            if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                System.out.println(request.getContentType());
                throw new JsonbException("Invalid Json");
            }
            MemberDTO member = JsonbBuilder.create().fromJson(request.getReader(), MemberDTO.class);

            if(member.getId()==null || !memberId.equalsIgnoreCase(member.getId())){
                
                throw new JsonbException("Id is empty or Invalid");
            } else if (member.getName()==null || !member.getName().matches("[A-Za-z ]+")) {
                throw new JsonbException("Name is empty or invalid");
            } else if(member.getContact()==null || !member.getContact().matches("\\d{3}-\\d{7}")){
                throw new JsonbException("Contact is empty or Invalid");
            }else if(member.getAddress()==null || !member.getAddress().matches("[A-Za-z0-9,.:;/\\-]+")){
                throw new JsonbException("Address is empty or invalid");
            }

            try(Connection connection = pool.getConnection()){
                PreparedStatement stm = connection.prepareStatement("UPDATE member SET name=?, address=?, contact=? WHERE id=?");
                stm.setString(1,member.getName());
                stm.setString(2,member.getAddress());
                stm.setString(3,member.getContact());
                stm.setString(4,member.getId());


                if(stm.executeUpdate()==1){
//                    response.setHeader("Access-Control-Allow-Origin","*");
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
}
