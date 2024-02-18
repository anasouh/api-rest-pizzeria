package controleurs.utils;

import java.io.IOException;
import java.io.PrintWriter;

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
}
