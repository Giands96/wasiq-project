package com.wasiq.inmobiliaria.shared.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //* Extraer todos las firmas (claims) del token */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //* Extraer una firma (claim) especifica del token */
    public <T> T  extractClaim (String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //* Generar token
    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .claim("role", getPrimaryRole(userDetails))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .signWith(getSigningKey())
                .issuedAt(new Date(currentTimeMillis()))
                .expiration(new Date(currentTimeMillis()+jwtExpiration))
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .signWith(getSigningKey())
                .issuedAt(new Date(currentTimeMillis()))
                .expiration(new Date(currentTimeMillis() + refreshExpiration))
                .compact();
    }

    private String getPrimaryRole(UserDetails userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(Object::toString)
                .findFirst()
                .orElse(null);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);

    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, REFRESH_TOKEN_TYPE);
    }

    //* A token is only valid for its own type and while the user remains enabled */
    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        final Claims claims = extractAllClaims(token);

        return userDetails.isEnabled()
                && userDetails.getUsername().equals(claims.getSubject())
                && expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                && !claims.getExpiration().before(new Date());
    }


}

