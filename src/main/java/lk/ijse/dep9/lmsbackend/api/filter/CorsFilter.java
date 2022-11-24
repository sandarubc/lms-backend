package lk.ijse.dep9.lmsbackend.api.filter;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@WebFilter (urlPatterns = {"/members/*","/books/*"})
public class CorsFilter extends HttpFilter {


    private List<String> origins;

    @Override
    public void init() throws ServletException {


//        String origin = getFilterConfig().getInitParameter("origins").trim();
//        System.out.println(origin);
//        origins = Arrays.asList(origin.split(", "));
//        System.out.println(origins);
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        String requestedOrigin = req.getHeader("Origin");
        for (String origin : origins) {
            if(requestedOrigin.startsWith(origin.trim())){
                res.setHeader("Access-Control-Allow-Origin",requestedOrigin);

                break;
            }

        }

        if(req.getMethod().equalsIgnoreCase("OPTIONS")){

            res.setHeader("Access-Control-Allow-Methods","OPTIONS, GET, HEAD, POST, PATCH, DELETE");
            String requestedMethod = req.getHeader("Access-Control-Allow-Method");
            String requestedHeaders = req.getHeader("Access-Control-Allow-Headers");

//            System.out.println(requestedMethod);
//            System.out.println(requestedHeaders);

            if(requestedMethod.equalsIgnoreCase("POST") ||
            requestedMethod.equalsIgnoreCase("PATCH") &&
            requestedHeaders.contains("content-type")){

                res.setHeader("Access-Control-Allow-Headers","Content-Type");
            }else{

                if(req.getMethod().equalsIgnoreCase("GET") ||
                        req.getMethod().equalsIgnoreCase("HEAD")){

                    res.setHeader("Access-Control-Expose-Headers","X-Total-Count");

                }
            }

        }
        chain.doFilter(req,res);
    }
}
