package controleurs;

import java.io.IOException;
import java.util.Map;

import dao.UserDAO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                response.setStatus(404);
        }
    }

    public void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = getBody(request);
        Map<String, Object> map = jsonToMap(json);
        String login = (String) map.get("login");
        String password = (String) map.get("password");
        if (isValidString(login) && isValidString(password)) {
            if ((userDAO.authenticate(login, password)) != null) {
                response.setStatus(200);
                String token = JwtManager.createJWT();
                returnJSON("{\"token\": \"" + token + "\"}", response);
            } else {
                response.setStatus(401);
                returnJSON("Invalid login or password", response);
            }
        } else {
            response.setStatus(400);
            returnJSON("Missing parameters: login or password", response);
        }
    }

    public static boolean verifToken(HttpServletRequest req, HttpServletResponse res) {
        String token = req.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring("Bearer".length()).trim();
            try {
                if (token != null && token.length() > 0) {
                    Claims claims = JwtManager.decodeJWT(token);
                    return true;
                }
            } catch (Exception e) {
            }
        }
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private boolean isValidString(String s) {
        return s != null && !s.isEmpty();
    }
}
