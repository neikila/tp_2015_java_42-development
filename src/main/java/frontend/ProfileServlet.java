package frontend;

import main.AccountService;
import main.UserProfile;
import templater.PageGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author v.chibrikov
 */
public class ProfileServlet extends HttpServlet {

    private AccountService accountService;

    public ProfileServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_OK);

        String pageToReturn;

        Map<String, Object> pageVariables = new HashMap<>();

        HttpSession session = request.getSession();

        UserProfile user = accountService.getSessions(session.getId());

        if (user == null)
        {
            pageVariables.put("loginStatus", "You haven't Logged In:");
            pageToReturn = "signInForm.html";
        } else {
            pageToReturn = "profile.html";
            pageVariables.put("login", user.getLogin());
            pageVariables.put("password", user.getPassword());
            pageVariables.put("email", user.getEmail());
            pageVariables.put("server", user.getServer());
        }

        response.getWriter().println(PageGenerator.getPage(pageToReturn, pageVariables));
    }
/* //Исходник
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");

        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> pageVariables = new HashMap<>();
        UserProfile profile = accountService.getUser(name);
        if (profile != null && profile.getPassword().equals(password)) {
            pageVariables.put("loginStatus", "Login passed");
        } else {
            pageVariables.put("loginStatus", "Wrong login/password");
        }

        response.getWriter().println(PageGenerator.getPage("authstatus.html", pageVariables));
    }
*/

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        response.setStatus(HttpServletResponse.SC_OK);

        String pageToReturn = "authstatus.html";

        Map<String, Object> pageVariables = new HashMap<>();

        HttpSession session = request.getSession();

        UserProfile profile = accountService.getUser(login);
        String message;
        if (profile != null && profile.getPassword().equals(password)) {
            accountService.addSessions(session.getId(), profile);
            message = "Login passed";
        } else {
            message = "Wrong login/password! Try again.";
            pageToReturn = "signInForm.html";
        }
        pageVariables.put("loginStatus", message);

        response.getWriter().println(PageGenerator.getPage(pageToReturn, pageVariables));
    }
/* //Исходик
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> pageVariables = new HashMap<>();
        pageVariables.put("email", email == null ? "" : email);
        pageVariables.put("password", password == null ? "" : password);

        response.getWriter().println(PageGenerator.getPage("authresponse.txt", pageVariables));
    }
*/
}