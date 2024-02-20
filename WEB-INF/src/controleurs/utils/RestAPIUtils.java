package controleurs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RestAPIUtils {
    public static final String JSON_NOT_FOUND = "{\"error\": \"Not found\"}";
    
    public static void returnJSON(String json, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(json);
    }

    public static String[] splitPathInfo(HttpServletRequest request) {
        return request.getPathInfo().split("/");
    }

    public static List<String> hasMissingParameter(String jsonBody, String... parameters) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> missingParameters = new ArrayList<>();
        try {
            Map<String, Object> map = mapper.readValue(jsonBody, HashMap.class);
            for (String parameter : parameters) {
                if (!map.containsKey(parameter) || map.get(parameter) == null) {
                    missingParameters.add(parameter);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return missingParameters;
    }

    public static String getBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            reader.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
