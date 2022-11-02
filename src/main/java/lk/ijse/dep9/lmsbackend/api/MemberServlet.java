package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.api.util.HttpServlet2;
import lk.ijse.dep9.lmsbackend.db.ConnectionPool;
import lk.ijse.dep9.lmsbackend.dto.MembersDTO;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Member;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

        if(req.getPathInfo()==null||req.getPathInfo().equals("/")){

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
                System.out.println("abc");
                searchMembers(query,resp);

            }else{
                loadAllMembers(resp);
            }

        }
        else{
            System.out.println("Execute else");
            Pattern compile = Pattern.compile("^/[A-Za-z0-9]{8}-([A-Za-z0-9]{4}-){3}-[A-Za-z0-9]{12}/?$");
            Matcher matcher = compile.matcher(req.getPathInfo());
            System.out.println(matcher.matches());
            if(matcher.matches()){
                String uid = matcher.group();


            }

        }
    }

    private void loadAllMembers(HttpServletResponse response) throws  IOException {

        try(Connection connection = pool.getConnection()) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM member");
            ArrayList<MembersDTO> membersDTOS = new ArrayList<>();

            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MembersDTO membersDTO = new MembersDTO(id, name, address, contact);
                membersDTOS.add(membersDTO);
            }

//            response.addHeader("Access-Control-Allow-Origin","http://localhost:5500");
            response.addHeader("Access-Control-Allow-Origin","*");
            response.setContentType("application/json");
            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(membersDTOS);
            response.getWriter().println(jsonMembers);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    protected void loadPaginatedMembers(int size,int page,HttpServletResponse resp) throws IOException {

        try(Connection connection = pool.getConnection()) {
            resp.setHeader("Access-Control-Allow-Origin","*");
            resp.setHeader("Access-Control-Allow-Headers","X-Total-Count");
            resp.setHeader("Access-Control-Expose-Headers","X-Total-Count");
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM member LIMIT ? OFFSET ?");
            stm.setInt(1,size);
            stm.setInt(2,(page-1)*size);
            ResultSet rst = stm.executeQuery();

            ArrayList<MembersDTO> membersDTOS = new ArrayList<>();

            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MembersDTO membersDTO = new MembersDTO(id, name, address, contact);
                membersDTOS.add(membersDTO);

            }
            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(membersDTOS);

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

            ArrayList<MembersDTO> membersDTOS = new ArrayList<>();
            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MembersDTO dto = new MembersDTO(id, name, address, contact);

                membersDTOS.add(dto);
            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(membersDTOS);


            response.getWriter().println(jsonMembers);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    private void searchMembersByPage(String query, int size,int page,HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Headers","X-Total-Count");
        response.setHeader("Access-Control-Expose-Headers","X-Total-Count");
        try(Connection connection = pool.getConnection()) {
            String sql = "SELECT * FROM member WHERE id LIKE ? OR name LIKE ? OR address LIKE ? OR contact LIKE ? LIMIT ? OFFSET ?";
            PreparedStatement stm = connection.prepareStatement(sql);
            query="%"+query+"%";
            for (int i = 0; i < sql.split("[?]").length-2; i++) {
                stm.setString(i+1,query);
            }
            stm.setInt(5,size);
            stm.setInt(6,size);

            ResultSet rst = stm.executeQuery();

            ArrayList<MembersDTO> membersDTOS = new ArrayList<>();
            while(rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                MembersDTO dto = new MembersDTO(id, name, address, contact);

                membersDTOS.add(dto);
            }

            Jsonb jsonb = JsonbBuilder.create();
            String jsonMembers = jsonb.toJson(membersDTOS);


            response.getWriter().println(jsonMembers);


        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Member:doPost()");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Members :doDelete()");
    }

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
    
    
    private void updateMember(String memberId, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Update member");


        try{
            if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                System.out.println(request.getContentType());
                throw new JsonbException("Invalid Json");
            }
            MembersDTO member = JsonbBuilder.create().fromJson(request.getReader(), MembersDTO.class);

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
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
