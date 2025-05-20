package org.demointernetshop45efs.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.lifeTime}")
    private long jwtLifeTime;

    public String createToken(String username) {

        Date now = new Date();

        log.warn("Время жизни токена установленно: {}", jwtLifeTime);
        Date expiryDate = new Date(now.getTime() + jwtLifeTime);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {

        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException e) {
            throw new InvalidJwtException("Invalid JWT token: " + e.getMessage());
        }

//        } catch (SignatureException e) {
//            // Invalid JWT signature
//            throw new InvalidJwtException("Invalid JWT signature");
//        } catch (MalformedJwtException e){
//            // Invalid JWT token
//            throw new InvalidJwtException("Invalid JWT token");
//        }catch (ExpiredJwtException e){
//            // Expired JWT token
//            throw new InvalidJwtException("Expired JWT token");
//        } catch (UnsupportedJwtException e){
//            // Unsupported JWT token
//            throw new InvalidJwtException("Unsupported JWT token");
//        } catch (IllegalArgumentException e){
//            // JWT claims is empty
//            throw new InvalidJwtException("JWT claims is empty");
//        }

        return true;
    }

    public String getUsernameFromJwt(String token){
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // вытаскиваем из claims (из части payload нашего JWT)
        // из них берем содержимое поля subject

        return claims.getSubject();
    }
}
