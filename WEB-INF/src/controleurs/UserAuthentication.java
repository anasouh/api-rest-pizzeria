package controleurs;

import java.io.IOException;
import java.util.Map;

import dao.UserDAO;
import dto.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static controleurs.utils.RestAPIUtils.*;

@WebServlet("/users/*")
public class UserAuthentication extends HttpServlet {
    UserDAO userDAO = new UserDAO();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        switch (parts[1]) {
            case "token":
                authenticateUser(request, response);
                break;
            default:
                response.setStatus(400);
        }
    }

    public void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = getBody(request);
        Map<String, Object> map = jsonToMap(json);
        String login = (String) map.get("login");
        String password = (String) map.get("password");
        User user;
        if (isValidString(login) && isValidString(password)) {
            if ((user = userDAO.authenticate(login, password)) != null) {
                response.setStatus(200);
                HttpSession session = request.getSession(true);
                String token = getToken(user);
                session.setAttribute("token", token);
                returnJSON("Authenticated", response);
            } else {
                response.setStatus(401);
                returnJSON("Invalid login or password", response);
            }
        } else {
            response.setStatus(400);
            returnJSON("Missing parameters: login or password", response);
        }
    }

    private String getToken(User user) {
        if (isValidString(user.getToken())) {
            return user.getToken();
        }
        String token = ""; // TODO: Générer le token
        userDAO.saveToken(user, token);
        return token;
    }
 
    private boolean isValidString(String s) {
        return s != null && !s.isEmpty();
    }
}
