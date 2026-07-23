package it.def.prolocobe.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.def.prolocobe.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordChangeGuardFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String PATTERN_CAMBIA_PASSWORD = "/api/utenti/*/password";

    private final ObjectMapper objectMapper;

    public PasswordChangeGuardFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UtenteAutenticato utenteAutenticato
                && utenteAutenticato.deveCambiarePassword()
                && !PATH_MATCHER.match(PATTERN_CAMBIA_PASSWORD, request.getRequestURI())) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    new ErrorResponse("Devi cambiare la password prima di continuare"));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
