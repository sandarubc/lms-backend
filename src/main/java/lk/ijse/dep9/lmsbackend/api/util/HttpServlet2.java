package lk.ijse.dep9.lmsbackend.api.util;

import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.dep9.lmsbackend.dto.ResponseStatusDTO;
import lk.ijse.dep9.lmsbackend.exception.ResponseStatusException;

import java.io.IOException;
import java.util.Date;

public class HttpServlet2 extends HttpServlet {

    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {


        try {
            if(req.getMethod().equalsIgnoreCase("PATCH")){
                doPatch(req,res);
            }
            else{
                super.service(req,res);
            }
        } catch (Throwable t) {

            ResponseStatusException r=t instanceof ResponseStatusException ?(ResponseStatusException) t:null;
            if(r==null || r.getStatus()>=500){
                t.printStackTrace();
            }
            ResponseStatusDTO statusDTO = new ResponseStatusDTO(r==null? 500:r.getStatus(),
                    r==null? t.getMessage():r.getMessage(),
                    req.getRequestURI(),
                    new Date().getTime());
            JsonbBuilder.create().toJson(statusDTO, res.getWriter());
        }
    }
}
