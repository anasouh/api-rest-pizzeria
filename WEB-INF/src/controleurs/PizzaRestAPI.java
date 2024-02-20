package controleurs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.IngredientDAO;
import dao.PizzaDAO;
import dto.Ingredient;
import dto.Pizza;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static controleurs.utils.RestAPIUtils.JSON_NOT_FOUND;
import static controleurs.utils.RestAPIUtils.returnJSON;
import static controleurs.utils.RestAPIUtils.splitPathInfo;
import static controleurs.utils.RestAPIUtils.hasMissingParameter;
import static controleurs.utils.RestAPIUtils.jsonToMap;
import static controleurs.utils.RestAPIUtils.getBody;

@WebServlet("/pizzas/*")
public class PizzaRestAPI extends HttpServlet {
    private ObjectMapper mapper = new ObjectMapper();
    private PizzaDAO dao = new PizzaDAO();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Pizza pizza;

        try {
            if (parts.length > 1) {
                if ((pizza = getPizzaOr404(parts[1], response)) != null) {
                    if (parts.length > 2) {
                        returnPizzaName(pizza, response);
                    } else {
                        returnPizza(pizza, response);
                    }
                }
            } else {
                Pizza[] ingredients = dao.findAll();
                returnPizzas(ingredients, response);
            }
        } catch (Exception e) {
            response.setStatus(400);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String jsonBody = getBody(req);
        List<String> missingParameters = hasMissingParameter(jsonBody, "id", "name", "basePrice", "dough", "ingredientsIds");
        if (!missingParameters.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON("Missing parameters: " + missingParameters, resp);
            return;
        }

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Pizza pizza = mapper.readValue(jsonBody, Pizza.class);
        if (dao.findById(pizza.getId()) != null) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            returnJSON("Pizza already exists", resp);
            return;
        }
        
        Map<String, Object> map = jsonToMap(jsonBody);
        try {
            List<Integer> idList = (ArrayList<Integer>)map.get("ingredientsIds");;
            pizza.addIngredients(idListToIngredientList(idList));
        } catch (ClassCastException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON("ingredientsIds must be an array of integers", resp);
            return;
        }

        if (pizza.getIngredients().size() == 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnJSON("At least one ingredient is required", resp);
            return;
        }

        dao.save(pizza);
        resp.setStatus(201);
        returnPizza(pizza, resp);
    }
/*

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Ingredient ingredient;
        
        if (parts.length > 1) {
            if ((ingredient = getIngredientOr404(parts[1], response)) != null) {
                dao.delete(ingredient);
                response.setStatus(204);
            }
        }
    }
*/

    private Pizza getPizzaOr404(String id, HttpServletResponse response) throws IOException {
        try {
            int idInt = Integer.parseInt(id);
            Pizza pizza = dao.findById(idInt);
            if (pizza == null) {
                response.setStatus(404);
                returnJSON(JSON_NOT_FOUND, response);
            }
            return pizza;
        } catch (Exception e) {
            response.setStatus(400);
            return null;
        }
    }
    private void returnPizza(Pizza pizza, HttpServletResponse response) throws IOException {
        if (pizza == null) {
            response.setStatus(404);
        }
        String json = mapper.writeValueAsString(pizza);
        returnJSON(json, response);
    }

    private void returnPizzaName(Pizza pizza, HttpServletResponse response) throws IOException {
        if (pizza == null) {
            response.setStatus(404);
        }
        String json = mapper.writeValueAsString(pizza.getName());
        returnJSON(json, response);
    }

    private void returnPizzas(Pizza[] pizza, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(pizza);
        returnJSON(json, response);
    }

    private Ingredient[] idListToIngredientList(List<Integer> idList) {
        IngredientDAO ingredientDao = new IngredientDAO();
        return ingredientDao.findByIds(idList);
    }
}
