package com.derbysoft.click.bootstrap.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Assigns a unique request ID to each incoming HTTP request, exposes it as a request attribute
 * and response header, and populates the actor context from the security context for downstream use.
 */
@Component
public class RequestContextFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_ATTR = "requestId";
  public static final String ACTOR_ID_ATTR = "actorId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String requestId = UUID.randomUUID().toString();
    request.setAttribute(REQUEST_ID_ATTR, requestId);
    response.setHeader("X-Request-Id", requestId);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      request.setAttribute(ACTOR_ID_ATTR, auth.getName());
    }

    filterChain.doFilter(request, response);
  }
}
