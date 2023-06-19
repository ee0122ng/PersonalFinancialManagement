package iss.ibf.pfm_expenses_server.authentication;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenUtil {

    private Logger logger = Logger.getLogger(JwtTokenUtil.class.getName());
    private static final Integer EXPIRATION = 86400000;

    private String secretKey = ""; // cached, not able to differentiate the user

    public JwtTokenUtil() {}

    private void setSecretKey() {
        this.secretKey = BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt(14, new SecureRandom())).replaceAll("[!@#$%^&*()_+-={}:;''<>,.?/~`|\\\"]", "e");
    }

    public String generateJwtToken(String username) {
        this.setSecretKey();

        return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(new Date().getTime() + EXPIRATION))
                    .signWith(key(), SignatureAlgorithm.HS256)
                    .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secretKey));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {

        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.log(Level.WARNING, e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.log(Level.WARNING, "JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.log(Level.WARNING, "JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
