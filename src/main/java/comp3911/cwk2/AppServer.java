package comp3911.cwk2;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

public class AppServer {
    public static void main(String[] args) throws Exception {
        Log.setLog(new StdErrLog());

        Server server = new Server(8080);
        
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/"); 
        contextHandler.setSessionHandler(new SessionHandler());
        contextHandler.addServlet(AppServlet.class, "/*");


        server.setHandler(contextHandler);

        // Start the server
        server.start();
        server.join();
    }
}
