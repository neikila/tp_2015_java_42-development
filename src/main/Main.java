package main;

import Interface.AccountService;
import MBean.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import resource.ResourceFactory;
import resource.ServerSettings;

public class Main {

    static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        ResourceFactory resourceFactory = ResourceFactory.instance();
        Context context = new Context();

        ServerSettings serverSettings = (ServerSettings)resourceFactory.getResource("serverSettings");
        int port = serverSettings.getPort();

        AccountService accountService = new AccountServiceImpl();
        accountService.createAdmin();
        context.add(AccountService.class, accountService);

        startMBean(context);

        logger.info("Starting at port: " + (String.valueOf(port)) + "\n");

        AppServer server = new AppServer(context, port);
        server.start();
    }

    private static void startMBean(Context context) throws Exception{
        AccountServiceControllerMBean serverStatistics = new AccountServiceController(context);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ServerManager:type=AccountServiceController");
        mbs.registerMBean(serverStatistics, name);
    }
}