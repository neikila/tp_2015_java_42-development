package frontend;

import Interface.AccountService;
import main.Context;
import main.user.UserProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import resource.LoggerMessages;
import resource.ResourceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TreeSet;

public class ScoreServlet extends HttpServlet {

    final private LoggerMessages loggerMessages = (LoggerMessages) ResourceFactory.instance().getResource("loggerMessages");
    final private Logger logger = LogManager.getLogger(ScoreServlet.class.getName());
    private AccountService accountService;

    public ScoreServlet(Context contextGlobal) {
        this.accountService = (AccountService)contextGlobal.get(AccountService.class);
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        logger.info(loggerMessages.doGetStart());

        short status = 200;
        String message = "";
        String limitInRequest = request.getParameter("limit");
        int limit;
        if (limitInRequest == null) {
            logger.info(loggerMessages.lackOfParam(), "limit");
            limit = 0;
            message = "WrongLimit";
            status = 400;
        } else {
            try {
                limit = Integer.parseInt(request.getParameter("limit"));
            } catch (Exception e) {
                logger.error(loggerMessages.paramHasWrongType(), "limit");
                limit = 0;
                message = "WrongLimit";
                status = 400;
            }
        }
        createResponse(response, status, message, accountService.getFirstPlayersByScore(limit));
        logger.info(loggerMessages.doGetFinish());
    }

    private void createResponse(HttpServletResponse response, short status, String errorMessage, TreeSet<UserProfile> FirstLimit) throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray scoreList = new JSONArray();
        JSONObject scoreItem;

        if (status != 200) {
            data.put("message", errorMessage);
        } else {
            while (!FirstLimit.isEmpty()) {
                scoreItem = new JSONObject();
                UserProfile temp = FirstLimit.pollFirst();
                scoreItem.put("login", temp.getLogin());
                scoreItem.put("score", temp.getScore());
                scoreList.add(scoreItem);
            }
            data.put("scoreList", scoreList);
        }
        obj.put("data", data);
        obj.put("status", status);

        response.getWriter().write(obj.toString());
    }
}