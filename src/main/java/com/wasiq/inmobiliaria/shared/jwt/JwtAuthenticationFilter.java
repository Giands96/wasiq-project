package com.wasiq.inmobiliaria.shared.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String jwt = null;

        //* Extraer jwt desde las cookies
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies){
                if("auth-token".equals(cookie.getName())){
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if(cookies == null || jwt == null){
            filterChain.doFilter(request, response);
            return;
        }

        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        //*  Si hay email y el usuario No eststá autenticado aun en el contexto
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication()==null){
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if(jwtService.isTokenValid(jwt, userDetails)){
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                //* Si el token no es valido, limpiar el contexto de seguridad y eliminar la cookie
               SecurityContextHolder.clearContext();
               //* Eliminar la cookie del token
                Cookie cookie = new Cookie("auth-token", null);
                cookie.setHttpOnly(true);
                cookie.setSecure(false); // Cambiar a true cuando uses HTTPS en producción
                cookie.setPath("/");
                cookie.setMaxAge(0);
               response.addCookie(cookie);
               //* Enviar un error de autenticación al cliente
               response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Ha ocurrido un error con el usuario");
               return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
