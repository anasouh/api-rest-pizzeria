package controleurs;

import static controleurs.utils.RestAPIUtils.returnJSON;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.OrderDAO;
import dto.Order;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/orders/*")
public class OderRestAPI extends HttpServlet {
    private OrderDAO dao = new OrderDAO();
    private ObjectMapper mapper = new ObjectMapper();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Order[] orders = dao.findAll();
        returnOrders(orders, response);        
    }

    private void returnOrders(Order[] orders, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(orders);
        returnJSON(json, response);
    }
}
