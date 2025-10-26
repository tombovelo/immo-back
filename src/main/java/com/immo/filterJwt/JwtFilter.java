package com.immo.filterJwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.immo.config.JwtUtils;
import com.immo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                email = jwtUtils.extractEmail(jwt);
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                
                if (jwtUtils.validationToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Authentification JWT réussie pour: {}", email);
                }
            }
            
        } catch (ExpiredJwtException e) {
            log.warn("JWT expiré: {}", e.getMessage());
            request.setAttribute("expired", "Token expiré");
        } catch (MalformedJwtException e) {
            log.warn("JWT malformé: {}", e.getMessage());
            request.setAttribute("invalid", "Token invalide");
        } catch (SignatureException e) {
            log.warn("Signature JWT invalide: {}", e.getMessage());
            request.setAttribute("invalid", "Signature invalide");
        } catch (UsernameNotFoundException e) {
            log.warn("Utilisateur non trouvé: {}", e.getMessage());
            request.setAttribute("invalid", "Utilisateur non trouvé");
        } catch (Exception e) {
            log.error("Erreur lors du traitement JWT: {}", e.getMessage());
            request.setAttribute("error", "Erreur d'authentification");
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    @SuppressWarnings("null")
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Ne pas filtrer les endpoints publics
        return path.startsWith("/api/auth/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/manage/health");
    }
}
