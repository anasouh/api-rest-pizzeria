package controleurs;

import static controleurs.utils.RestAPIUtils.JSON_NOT_FOUND;
import static controleurs.utils.RestAPIUtils.getBody;
import static controleurs.utils.RestAPIUtils.hasMissingParameter;
import static controleurs.utils.RestAPIUtils.jsonToMap;
import static controleurs.utils.RestAPIUtils.returnJSON;
import static controleurs.utils.RestAPIUtils.splitPathInfo;

import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.OrderDAO;
import dao.PizzaDAO;
import dao.UserDAO;
import dto.Order;
import dto.Pizza;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/orders/*")
public class OderRestAPI extends HttpServlet {
    private OrderDAO dao = new OrderDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        try {
            if (parts.length > 1) {
                int id = Integer.parseInt(parts[1]);
                Order order = dao.findById(id);
                if (order == null) {
                    returnJSON(JSON_NOT_FOUND, response);
                    return;
                }
                if (parts.length > 2 && parts[2].equals("prixfinal")) {
                    returnJSON("" + order.getPrice(), response);
                    return;
                }
                returnOrder(order, response);
            } else {
                Order[] orders = dao.findAll();
                returnOrders(orders, response);
            }
        } catch (NumberFormatException e) {
            returnJSON(JSON_NOT_FOUND, response);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String json = getBody(request);
        List<String> missingParameters = hasMissingParameter(json, "id", "login", "pizzasIds", "date");
        if (!missingParameters.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON("Missing parameters: " + missingParameters, response);
            return;
        }
        try {
            PizzaDAO pizzaDAO = new PizzaDAO();
            UserDAO userDAO = new UserDAO();
            Map<String, Object> map = jsonToMap(json);
            int id = (Integer) map.get("id");
            if (dao.findById(id) != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                returnJSON("Order already exists [id=" + id + "]", response);
                return;
            }
            String login = ((String) map.get("login")).strip();
            if (userDAO.findByLogin(login) == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                returnJSON("User not found", response);
                throw new Exception(login);
            }
            String date = (String) map.get("date");
            Date.valueOf(date);
            List<Integer> pizzasIds = (List<Integer>) map.get("pizzasIds");
            Order order = new Order(id, login, date);
            for (int pizzaId : pizzasIds) {
                Pizza pizza = pizzaDAO.findById(pizzaId);
                if (pizza == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    returnJSON("Pizza not found [id=" + pizzaId + "]", response);
                    return;
                }
                order.addPizza(pizza);
            }
            dao.save(order);
            returnOrder(order, response);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON("Invalid date format", response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON(e.getMessage(), response);
        }
    }

    private void returnOrders(Order[] orders, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(orders);
        returnJSON(json, response);
    }

    private void returnOrder(Order order, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(order);
        returnJSON(json, response);
    }
}
