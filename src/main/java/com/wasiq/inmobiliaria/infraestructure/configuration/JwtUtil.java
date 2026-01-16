package com.wasiq.inmobiliaria.infraestructure.configuration;

import com.wasiq.inmobiliaria.domain.model.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct; // Asegúrate de tener esta dependencia
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private SecretKey key;

    //* Este método se ejecuta automáticamente después de inyectar los valores
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        //* Map<Tipo, Valor>
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().toString());

        return Jwts.builder()
                //setClaims -> Añade información extra al token
                .setClaims(extraClaims)
                // setSubject -> Quién es el dueño del token
                .setSubject(user.getEmail())
                // setIssuedAt -> Cuándo se emitió el token
                .setIssuedAt(new Date())
                // setExpiration -> Cuándo expira el token
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // signWith -> Firma el token con el algoritmo y la clave secreta
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}