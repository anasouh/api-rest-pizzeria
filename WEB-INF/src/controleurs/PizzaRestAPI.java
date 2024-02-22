package controleurs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (method.equals("PATCH")) {
            this.doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Pizza pizza;

        try {
            if (parts.length > 1) {
                if ((pizza = getPizzaOr404(parts[1], response)) != null) {
                    if (parts.length > 2 && parts[2].equals("prixfinal")) {
                        returnPizzaPrice(pizza, response);
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] parts = splitPathInfo(req);
        Pizza pizza;

        if (parts.length <= 1) {
            createPizza(req, resp);
        } else if (parts.length == 2) {
            if ((pizza = getPizzaOr404(parts[1], resp)) != null) {
                Map<String, Object> map = jsonToMap(getBody(req));
                if (map.containsKey("ingredient")) {
                    Integer ingredientId;
                    try {
                        ingredientId = (Integer)map.get("ingredient");
                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        returnJSON("Ingredient must be an integer", resp);
                        return;
                    }
                    IngredientDAO ingredientDao = new IngredientDAO();
                    Ingredient ingredient = ingredientDao.findById(ingredientId);
                    if (ingredient != null) {
                        if (pizza.getIngredients().contains(ingredient)) {
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            returnJSON("Ingredient already exists in pizza", resp);
                            return;
                        }
                        pizza.addIngredient(ingredient);
                        dao.save(pizza);
                        returnPizza(pizza, resp);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        returnJSON("Ingredient not found", resp);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    returnJSON("Missing parameter: ingredient", resp);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void createPizza(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            pizza.addIngredients(idListToIngredientsArray(idList));
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

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Pizza pizza;
        
        if (parts.length == 2) {
            if ((pizza = getPizzaOr404(parts[1], response)) != null) {
                dao.delete(pizza);
                response.setStatus(204);
            }
        } else if (parts.length >= 3) {
            if ((pizza = getPizzaOr404(parts[1], response)) != null) {
                Integer ingredientId;
                try {
                    ingredientId = Integer.parseInt(parts[2]);
                } catch (Exception e) {
                    response.setStatus(400);
                    return;
                }
                IngredientDAO ingredientDao = new IngredientDAO();
                Ingredient ingredient = ingredientDao.findById(ingredientId);
                if (ingredient != null) {
                    if (pizza.getIngredients().contains(ingredient)) {
                        pizza.removeIngredient(ingredient);
                        dao.save(pizza);
                        response.setStatus(204);
                        return;
                    } else {
                        response.setStatus(400);
                        returnJSON("Ingredient not found in pizza", response);
                    }
                }
                response.setStatus(404);
            }
        } else {
            response.setStatus(400);
        }
    }

    @SuppressWarnings("unchecked")
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Pizza pizza;
        
        if (parts.length == 2) {
            if ((pizza = getPizzaOr404(parts[1], response)) != null) {
                try {
                    Map<String, Object> map = jsonToMap(getBody(request));
                    for (String key : map.keySet()) {
                        switch (key) {
                            case "name":
                                pizza.setName((String)map.get(key));
                                break;
                            case "basePrice":
                                pizza.setBasePrice((Integer)map.get(key));
                                break;
                            case "dough":
                                pizza.setDough((String)map.get(key));
                                break;
                            case "ingredientsIds":
                                List<Integer> idList = (ArrayList<Integer>)map.get(key);
                                pizza.setIngredients(Arrays.asList(idListToIngredientsArray(idList)));
                                break;
                        }
                    }
                    dao.save(pizza);
                    returnPizza(pizza, response);
                } catch (Exception e) {
                    response.setStatus(400);
                    return;
                }
            }
        } else {
            response.setStatus(400);
        }
    }

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

    private void returnPizzaPrice(Pizza pizza, HttpServletResponse response) throws IOException {
        if (pizza == null) {
            response.setStatus(404);
        }
        String json = mapper.writeValueAsString(pizza.getPrice());
        returnJSON(json, response);
    }

    private void returnPizzas(Pizza[] pizza, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(pizza);
        returnJSON(json, response);
    }

    private Ingredient[] idListToIngredientsArray(List<Integer> idList) {
        IngredientDAO ingredientDao = new IngredientDAO();
        return ingredientDao.findByIds(idList);
    }
}
