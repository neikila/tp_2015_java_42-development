package main;

public class UserProfile {
    private String login;
    private String password;
    private String email;
    private String server;
    private boolean isSuperUser;

    public UserProfile(String login, String password, String email, String server) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.server = server;
        this.isSuperUser = false;
    }

    public void setAdmin(boolean value) {
        isSuperUser = value;
    }

    public boolean isAdmin() { return isSuperUser; }

    public String getRole() { return isSuperUser?"Admin":"User"; }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getServer() {
        return server;
    }
}
