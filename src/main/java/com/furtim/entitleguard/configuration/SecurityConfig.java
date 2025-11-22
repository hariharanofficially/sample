package com.furtim.entitleguard.configuration;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.furtim.entitleguard.service.CustomUserDetailsService;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
	
	private final JwtFilter jwtFilter;
	
	 @Bean
	    public AuthenticationProvider authProvider(CustomUserDetailsService userDetailsService) {
	    	DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
	    	daoProvider.setUserDetailsService(userDetailsService);
	    	daoProvider.setPasswordEncoder(encoder());
	    	return daoProvider;
	    }

	    @Bean
	    public PasswordEncoder encoder() {
	        return new BCryptPasswordEncoder();
	    }
	    
	    @Bean
		public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
			return authenticationConfiguration.getAuthenticationManager();
		}
	    
//	    @Bean
//		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//			http.csrf().disable()
//			.authorizeRequests().antMatchers("/unsecure/**").permitAll()
//			.and()
//			.authorizeRequests().antMatchers("/api/**").authenticated()
//			.anyRequest().permitAll()
//			.and()
//			.exceptionHandling()
//			.and()
//			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//			http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//			return http.build();
//		}
//	    
//	    @Bean
//		 CorsConfigurationSource corsConfigurationSource() {
//		        CorsConfiguration configuration = new CorsConfiguration();
//		        configuration.setAllowedOrigins(Collections.singletonList("*"));
//		        configuration.setAllowedMethods(Collections.singletonList("*"));
//		        UrlBasedCorsConfigurationSource source = new     UrlBasedCorsConfigurationSource();
//		        source.registerCorsConfiguration("/**", configuration);
//				return source;
//		 }
	    
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	            .cors().configurationSource(corsConfigurationSource()) // ✅ enable CORS for secure APIs
	            .and()
	            .csrf().disable()
	            .authorizeRequests()
	                .antMatchers("/unsecure/**").permitAll()
	                .antMatchers("/api/**").authenticated()
	                .anyRequest().permitAll()
	            .and()
	            .exceptionHandling()
	            .and()
	            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
	        return http.build();
	    }
	    
	    @Bean
	    CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration configuration = new CorsConfiguration();
	        configuration.setAllowedOriginPatterns(List.of(
	            "https://entitleguard.flutterflow.app",
	            "https://builders.entitleguard.com",
	            "https://builders-staging.entitleguard.com",
	            "https://app2.entitleguard.com",
	            "https://app2-staging.entitleguard.com",
	            "https://cdn.shopify.com"
	        ));
	        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
	        configuration.setAllowedHeaders(List.of("*"));
	        configuration.setAllowCredentials(true); // ✅ Needed if using cookies/JWT in headers

	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", configuration);
	        return source;
	    }



}
