package controleurs;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.IngredientDAO;
import dto.Ingredient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static controleurs.utils.RestAPIUtils.JSON_NOT_FOUND;
import static controleurs.utils.RestAPIUtils.returnJSON;
import static controleurs.utils.RestAPIUtils.splitPathInfo;

@WebServlet("/ingredients/*")
public class IngredientRestAPI extends HttpServlet {
    private ObjectMapper mapper = new ObjectMapper();
    private IngredientDAO daoList = new IngredientDAO();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Ingredient ingredient;

        try {
            if (parts.length > 1) {
                if ((ingredient = getIngredientOr404(parts[1], response)) != null) {
                    if (parts.length > 2) {
                        returnIngredientName(ingredient, response);
                    } else {
                        returnIngredient(ingredient, response);
                    }
                }
            } else {
                Ingredient[] ingredients = daoList.findAll();
                returnIngredients(ingredients, response);
            }
        } catch (Exception e) {
            response.setStatus(400);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Ingredient ingredient = mapper.readValue(req.getReader(), Ingredient.class);
        if (daoList.findById(ingredient.getId()) != null) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Ingredient already exists");
            return;
        }
        
        daoList.save(ingredient);
        resp.setStatus(201);
        returnIngredient(ingredient, resp);
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String[] parts = splitPathInfo(request);
        Ingredient ingredient;
        
        if (parts.length > 1) {
            if ((ingredient = getIngredientOr404(parts[1], response)) != null) {
                daoList.delete(ingredient);
                response.setStatus(204);
            }
        }
    }

    private Ingredient getIngredientOr404(String id, HttpServletResponse response) throws IOException {
        try {
            int idInt = Integer.parseInt(id);
            Ingredient ingredient = daoList.findById(idInt);
            if (ingredient == null) {
                response.setStatus(404);
                returnJSON(JSON_NOT_FOUND, response);
            }
            return ingredient;
        } catch (Exception e) {
            response.setStatus(400);
            return null;
        }
    }

    private void returnIngredient(Ingredient ingredient, HttpServletResponse response) throws IOException {
        if (ingredient == null) {
            response.setStatus(404);
        }
        String json = mapper.writeValueAsString(ingredient);
        returnJSON(json, response);
    }

    private void returnIngredientName(Ingredient ingredient, HttpServletResponse response) throws IOException {
        if (ingredient == null) {
            response.setStatus(404);
        }
        String json = mapper.writeValueAsString(ingredient.getName());
        returnJSON(json, response);
    }

    private void returnIngredients(Ingredient[] ingredients, HttpServletResponse response) throws IOException {
        String json = mapper.writeValueAsString(ingredients);
        returnJSON(json, response);
    }

    
}
