package dto;

public class User {
    private String login, token;

    public User(String login, String token) {
        this.login = login;
        this.token = token;
    }

    public User() {
    }

    public String getLogin() {
        return login;
    }

    public String getToken() {
        return token;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setToken(String password) {
        this.token = password;
    }

    @Override
    public String toString() {
        return "User (" + login + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        User other = (User) obj;
        return other.login.equals(this.login);
    }
}
