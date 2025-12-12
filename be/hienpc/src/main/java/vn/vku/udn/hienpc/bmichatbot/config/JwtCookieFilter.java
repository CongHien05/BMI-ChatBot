package vn.vku.udn.hienpc.bmichatbot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.vku.udn.hienpc.bmichatbot.service.JwtService;

import java.io.IOException;

/**
 * Filter để đọc JWT token từ cookie và thêm vào SecurityContext
 * Hỗ trợ admin portal sử dụng cookie thay vì header
 */
@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtCookieFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Bỏ qua static resources và login page
        if (requestPath.startsWith("/css/") || 
            requestPath.startsWith("/js/") || 
            requestPath.startsWith("/images/") ||
            requestPath.equals("/favicon.ico") ||
            requestPath.equals("/admin/login") ||
            !requestPath.startsWith("/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Ưu tiên đọc từ cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("adminToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Nếu không có trong cookie, thử đọc từ header (cho API calls)
        if (token == null) {
            final String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null) {
            try {
                String username = jwtService.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Token không hợp lệ, xóa cookie nếu có
                Cookie invalidCookie = new Cookie("adminToken", "");
                invalidCookie.setMaxAge(0);
                invalidCookie.setPath("/");
                response.addCookie(invalidCookie);
            }
        }

        filterChain.doFilter(request, response);
    }
}

