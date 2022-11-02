package lk.ijse.dep9.lmsbackend.api.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HttpServlet2 extends HttpServlet {

    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        if(req.getMethod().equalsIgnoreCase("PATCH")){
            doPatch(req,res);
        }
        else{
            super.service(req,res);
        }
    }
}
