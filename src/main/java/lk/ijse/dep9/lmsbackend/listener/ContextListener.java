package lk.ijse.dep9.lmsbackend.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lk.ijse.dep9.lmsbackend.db.ConnectionPool;
import org.apache.commons.dbcp2.BasicDataSource;

//@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {


        //Custom Implemented connection Pool
        /*ConnectionPool dbPool = new ConnectionPool(2);
        sce.getServletContext().setAttribute("pool",dbPool);*/

        BasicDataSource dbPool = new BasicDataSource();
        dbPool.setUrl("jdbc:mysql://localhost:3306/dep9_lms");
        dbPool.setUsername("root");
        dbPool.setPassword("2012");
        dbPool.setDriverClassName("com.mysql.cj.jdbc.Driver");


        dbPool.setInitialSize(10);
        dbPool.setMaxTotal(20);
        sce.getServletContext().setAttribute("pool",dbPool);

    }

    
}
