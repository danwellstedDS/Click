package api.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import domain.AuthClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String token = extractCookie(request, "auth_token");
    if (token != null) {
      try {
        AuthClaims claims = jwtService.verifyAndExtract(token);
        UserPrincipal principal = new UserPrincipal(
            claims.userId(),
            claims.tenantId(),
            claims.email(),
            claims.role()
        );
        var authentication = new UsernamePasswordAuthenticationToken(
            principal,
            token,
            principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JWTVerificationException ignored) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private static String extractCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
