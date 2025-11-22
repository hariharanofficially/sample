package com.furtim.entitleguard.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.furtim.entitleguard.dto.CustomerDto;
import com.furtim.entitleguard.service.CustomUserDetailsService;
import com.furtim.entitleguard.utils.JwtUtil;
import com.furtim.entitleguard.utils.UserSessionUtil;
import com.sun.istack.NotNull;

import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

	private final CustomUserDetailsService customUserService;

	public static final String AUTHORIZATION = "Authorization";

	private final JwtUtil jwtUtil;

	protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
			@NotNull FilterChain filterChain) throws ServletException, IOException {

		String authorizationHeader = request.getHeader(AUTHORIZATION);
		String token = null;
		String userName = null;

		if (request.getRequestURL().toString().contains("unsecure/")) {
			authorizationHeader = null;
		}

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
			token = authorizationHeader.substring(7);
			userName = authorizationToken(authorizationHeader, response);
		}

		if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			log.info("userName {}", userName);
			UserDetails userDetails = customUserService.loadUserByUsername(userName);

			if (jwtUtil.validateToken(token, userDetails)) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userName, null, userDetails.getAuthorities());

				usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

			} else {
				throw new JwtException("Invalid Token");
			}

		}

		filterChain.doFilter(request, response);

		UserSession.getInstance().clear();

	}

	// authorize token and return userName from given token

	private String authorizationToken(String authorizationHeader, HttpServletResponse response) {

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
			String token = authorizationHeader.substring(7);

			// Optional<UserJwtToken> jwt = userJwtTokenRepo.findOneByJwtAndLogged(token,
			// LoggedKey.IN.toString());

			try {
				CustomerDto userDto = jwtUtil.parseToken(token);
				if (userDto != null) {
					UserSession.getInstance().setCurrentUser(userDto);
				}
			} catch (Exception ex) {
				throw new JwtException("Invalid token");
			}

			return UserSessionUtil.getUserName();
		}
		setUnauthorized(response);
		return null;

	}

	private void setUnauthorized(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

}
