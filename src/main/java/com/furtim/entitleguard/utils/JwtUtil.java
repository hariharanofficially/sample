package com.furtim.entitleguard.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.furtim.entitleguard.dto.CustomerDto;
import com.furtim.entitleguard.dto.UserInfoDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtUtil {
	
	@Value("${jwt.secret}")
	private String secret;
	
	 private Key getSigningKey() {
	        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	    }
	
	
	public CustomerDto parseToken(String token) throws Exception  {
		try {
			
			Claims body = Jwts.parserBuilder()
	                  .setSigningKey(getSigningKey())
	                  .build()
	                  .parseClaimsJws(token)
	                  .getBody();

			    CustomerDto u = new CustomerDto();

				String json = body.getSubject();
				ObjectMapper objectMapper = new ObjectMapper();

				JsonNode jsonNode = objectMapper.readTree(json);
				u.setEmail(jsonNode.hasNonNull("email") ? jsonNode.get("email").asText() : null);
				u.setContact(jsonNode.hasNonNull("contact") ? jsonNode.get("contact").asText() : null);
				u.setId(jsonNode.hasNonNull("id") ? jsonNode.get("id").asText() : null);
			

				return u;

			} catch (JwtException | ClassCastException | JsonProcessingException e) {
				log.info("exception {} ", e.getMessage());
				throw e;
			}
		}

 	public String generateToken(CustomerDto user) {
 		String mapperStr = userObjectManager(user);
 		Claims claims = Jwts.claims().setSubject(mapperStr)
 				.setExpiration(new Date(System.currentTimeMillis() + 86400000 ))
 				.setIssuedAt(new Date());



 		return Jwts.builder()
 	           .setClaims(claims)
 	           .signWith(getSigningKey(), SignatureAlgorithm.HS256)
 	           .compact();

 	}
 	
 	public String generateBuilderToken(UserInfoDto user) {
 		String mapperStr = builderObjectManager(user);
 		Claims claims = Jwts.claims().setSubject(mapperStr)
 				.setExpiration(new Date(System.currentTimeMillis() + 86400000 ))
 				.setIssuedAt(new Date());



 		return Jwts.builder()
 	           .setClaims(claims)
 	           .signWith(getSigningKey(), SignatureAlgorithm.HS256)
 	           .compact();

 	}
 	
 	public String builderObjectManager(UserInfoDto user) {

 		ObjectMapper mapper = new ObjectMapper();
 		mapper = JsonMapper.builder()
	    .findAndAddModules()
	    .build();
 		try {
 			return mapper.writeValueAsString(user);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return LoggedKey.EMPTY.toString();
 		}
 	}
 	
//	public String generateToken(CustomerDto user) {
//	    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//		String mapperStr = userObjectManager(user);
//
//	    return Jwts.builder()
//	        .setSubject(mapperStr)  
//	        .claim("name", user.getName())
//	        .claim("email", user.getEmail())
//	        .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365))
//	        .signWith(key, SignatureAlgorithm.HS256)
//	        .compact();
//	}


 	public boolean validateToken(String token, UserDetails userDetails) throws JwtException{
 		try {
	 		CustomerDto user = parseToken(token);
	 		log.info("user {}", userDetails.getUsername());
	 		
	        return (userDetails.getUsername() != null &&
	                (userDetails.getUsername().equals(user.getEmail()) || userDetails.getUsername().equals(user.getContact())));

 		} catch (Exception ex) {
 			throw new JwtException("Invaid token");
 		}
 	}

 	public String userObjectManager(CustomerDto user) {

 		ObjectMapper mapper = new ObjectMapper();
 		mapper = JsonMapper.builder()
	    .findAndAddModules()
	    .build();
 		try {
 			return mapper.writeValueAsString(user);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return LoggedKey.EMPTY.toString();
 		}
 	}

}
