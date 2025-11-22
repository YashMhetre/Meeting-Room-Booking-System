// It Runs on every request

package com.meetingroom.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter 
{
    @Autowired private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException 
    {

        final String authHeader = request.getHeader("Authorization");
        
        String email = null, token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) 
        {
            token = authHeader.substring(7);
            try 
            { 
            	email = jwtUtil.extractEmail(token); 
            } 
            catch (Exception e) 
            { 
            	// invalid token 
            }
        }

        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) 
        {
        	// if the token is validated
            if (jwtUtil.validateToken(token)) 
            {
                String role = jwtUtil.extractRole(token);
                
                // creating an Authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
