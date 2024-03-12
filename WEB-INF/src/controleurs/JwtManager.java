package controleurs;
// code pompé ici : https://developer.okta.com/blog/2018/10/31/jwts-with-java
// lui-même inspiré par : https://www.baeldung.com/java-json-web-tokens-jjwt
// et sinon la doc : https://github.com/jwtk/jjwt/blob/master/README.md

// et réadapté grace à https://www.appsdeveloperblog.com/add-and-validate-custom-claims-in-jwt/

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

public class JwtManager {
    // pour SHA256 : 256 bits mini
    private static final String SECRET_KEY = "bachibouzoukbachibouzoukbachibouzoukbachibouzouk";

    public static String createJWT() {

        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(1200); // 20 mn
        Date expDate = Date.from(expiration);

        // Let's set the JWT Claims
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(Date.from(now))
                .subject("Authentification pour tp333")
                .issuer("philippe.mathieu@univ-lille.fr")
                .expiration(expDate)
                .signWith(signingKey)
                .compact();

        return token;
    }

    public static Claims decodeJWT(String jwt) throws Exception {
        // This line will throw an exception if it is not a signed JWS (as expected)

        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

        JwtParser parser = Jwts.parser()
                .verifyWith(signingKey)
                .build();

        Claims claims = parser.parseSignedClaims(jwt).getPayload();
        return claims;
    }

    // Exemple de fonctionnement
    public static void main(String args[]) {
        System.out.println(JwtManager.SECRET_KEY);
        String token = JwtManager.createJWT();
        /*
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        System.out.println(token);

        Claims claims = null;
        try {
            claims = JwtManager.decodeJWT(token);
        } catch (Exception e) {
            System.out.println("jeton invalide " + e.getMessage());
            System.exit(1);
        }

        System.out.println(claims.toString());
    }
}
