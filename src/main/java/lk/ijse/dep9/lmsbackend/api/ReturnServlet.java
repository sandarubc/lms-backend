package lk.ijse.dep9.lmsbackend.api;

import jakarta.annotation.Resource;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import lk.ijse.dep9.lmsbackend.dto.ReturnDTO;
import lk.ijse.dep9.lmsbackend.dto.ReturnItemDTO;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "ReturnsServlet", value = "/returns")
public class ReturnServlet extends HttpServlet {


    @Resource(lookup = "java:comp/env/jdbc/dep9-lms")
    DataSource pool;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.getWriter().println("Return Note: doPost()");

        if(request.getPathInfo()!=null  && !request.getPathInfo().equals("/")){
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }
        try {
            if(request.getContentType()==null || !request.getContentType().startsWith("application/json")){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid Json");
            }

            ReturnDTO returnDTO = JsonbBuilder.create().fromJson(request.getReader(), ReturnDTO.class);
            addReturnItem(returnDTO,response);

        } catch (JsonbException e) {

            response.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());

        }



    }

    private void addReturnItem(ReturnDTO returnDTO, HttpServletResponse response) throws IOException {

        if(returnDTO.getMemberID()==null ||
                !returnDTO.getMemberID().matches("([A-Fa-f0-9]{8}(-[A-Fa-f0-9]{4}){3}-[A-Fa-f0-9]{12})")){
            throw new JsonbException("The member is invalid or empty");
        }else if(returnDTO.getReturnItems().isEmpty()){
            throw new JsonbException("No items have been found");
        }
        else if(returnDTO.getReturnItems().stream().anyMatch(Objects::isNull)){
            throw new JsonbException("Null items have been found in the list");
        } else if(returnDTO.getReturnItems().stream().anyMatch(item->
                item.getIssuNoteId()==null || item.getIsbn().matches("([0-9][0-9\\\\-]*[0-9])*"))){
            throw new JsonbException("Some items are invalid");
        }
        Set<ReturnItemDTO> returnItems = returnDTO.getReturnItems().stream().collect(Collectors.toSet());

//        Business Validation

        try(Connection connection = pool.getConnection()) {




        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Failed to return Item");
        }


    }
}
