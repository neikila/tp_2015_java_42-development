package main;

import Interface.AccountService;
import base.GameMechanics;
import base.WebSocketService;
import frontend.*;
import frontend.game.*;
import mechanics.GameMechanicsImpl;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.apache.logging.log4j.Logger;

public class AppServer {

    static final Logger logger = LogManager.getLogger(AppServer.class.getName());

    private GameMechanics gameMechanics;
    private Server server;

    public AppServer(Context contextGlobal, int port) {

        server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        String  apiVersion = "v1";
        String url = "/api/" + apiVersion;

        context.addServlet(new ServletHolder(new SignInServlet(contextGlobal)), url + "/auth/signin");
        context.addServlet(new ServletHolder(new SignUpServlet(contextGlobal)), url + "/auth/signup");
        context.addServlet(new ServletHolder(new ProfileServlet(contextGlobal)), url + "/auth/check");
        context.addServlet(new ServletHolder(new AdminServlet(contextGlobal)), url + "/auth/admin");
        context.addServlet(new ServletHolder(new SignOutServlet(contextGlobal)), url + "/auth/signout");
        context.addServlet(new ServletHolder(new ScoreServlet(contextGlobal)), url + "/score");

        WebSocketService webSocketService = new WebSocketServiceImpl();
        gameMechanics = new GameMechanicsImpl(webSocketService);

        context.addServlet(new ServletHolder(new WebSocketGameServlet((AccountService)contextGlobal.get(AccountService.class), gameMechanics, webSocketService)), "/gameplay");
        context.addServlet(new ServletHolder(new GameServlet((AccountService)contextGlobal.get(AccountService.class))), "/game.html");

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase("public_html");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler, context});
        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Start");
        gameMechanics.run();
        logger.info("Finish");
    }

    public void start(){
        try {
            server.start();
            logger.info("Start");
            gameMechanics.run();
            //server.join();
        } catch (Exception e) {
            logger.fatal("There is an error in Server.Start()");
            System.exit(1);
        }
    }

    public void stop(){
        try {
            System.exit(0);
        } catch (Exception e) {
            logger.fatal("There is an error in Server.Stop()");
            System.exit(1);
        }
    }
}
