package com.furtim.entitleguard.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.UserInfo;
import com.furtim.entitleguard.service.MailService;
import com.furtim.entitleguard.utils.JwtUtil;
import com.furtim.entitleguard.utils.MailConstant;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MailController {
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Value("${jwt.secret}")
    private String tokenSecret;
    @Value("${app.domain}")
    private String domainUrl;
    @Value("${app.name}")
    private String appName;
    @Value("${mail.header}")
    private String header;
    @Value("${app.domain.support}")
    private String supportMail;
    @Value("${mail.noreply}")
    private String noReply;
    
    
    
    public boolean sendLoginOtp(Customer customer) {

		log.info("Login OTP mail invoked {}", customer.getEmail());

		String[] to = { customer.getEmail() };
		String[] cc = {};
		String[] bcc = {};

		String body = "";
		String subject = "Verification Code";

		try {
			Resource resource = resourceLoader.getResource("classpath:mailTemplate/loginotp.html");

		    try (InputStream inputStream = resource.getInputStream()) {
		        // Parse HTML directly from InputStream
		        body = Jsoup.parse(inputStream, "UTF-8", "").outerHtml();
		    }

		    // Replace placeholders
		    body = body.replace("{EMAIL}", customer.getEmail());
		    body = body.replace("{OTP}", customer.getOtp());

		    
//			File file = resourceLoader.getResource("classpath:mailTemplate/loginotp.html").getFile();
//
//			body = Jsoup.parse(file, "UTF-8").outerHtml();
//			body = body.replace("{EMAIL}", customer.getEmail());
//			body = body.replace("{OTP}", customer.getOtp());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return mailService.sendMail(to, cc, bcc, subject, body);
	}


	public boolean sendWelcomeEmail(Customer customer) {
		log.info("Welcome email invoked for {}", customer.getEmail());

		String[] to = { customer.getEmail() };
		String[] cc = {};
		String[] bcc = {};

		String body = "";
		String subject = "Welcome to EntitleGuard – Protect Your Purchase!";

		try {
			Resource resource = resourceLoader.getResource("classpath:mailTemplate/welcomeMail.html");
			try (InputStream inputStream = resource.getInputStream()) {
				body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			}

			body = body.replace("{{customerName}}", customer.getName() != null ? customer.getName() : "Customer");
			body = body.replace("{{email}}", customer.getEmail() != null ? customer.getEmail() : "");

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error loading welcome email template", e);
			return false;
		}

		return mailService.sendMail(to, cc, bcc, subject, body);
	}


	public boolean sendVerifyMail(UserInfo user) {
	    log.info("Verification mail triggered for user: {}", user.getEmail());

	    String[] to = { user.getEmail() };
	    String[] cc = {};
	    String[] bcc = {};

	    String subject = "User Verification";
	    String body = "";

	    try {
	        // Prepare claims for JWT
	        Map<String, Object> claim = new HashMap<>();
	        claim.put("mail", user.getEmail());
	        claim.put("name", user.getFirstName());
	        claim.put("id", user.getId());

	        SecretKey key = Keys.hmacShaKeyFor(tokenSecret.getBytes(StandardCharsets.UTF_8));

	        String jwtToken = Jwts.builder()
	                .setSubject("verifyLink")
	                .setExpiration(DateUtils.addDays(new Date(), 1))
	                .addClaims(claim)
	                .setIssuedAt(new Date())
	                .signWith(key, SignatureAlgorithm.HS256)
	                .compact();

	        String link = (domainUrl.endsWith("/") ? domainUrl : domainUrl + "/") + "auth/resetPassword?token=" + jwtToken;
	        log.info("Verification link for {}: {}", user.getEmail(), link);

	        // Load HTML template
	        Resource resource = resourceLoader.getResource("classpath:mailTemplate/verified.html");
	        try (InputStream inputStream = resource.getInputStream()) {
	            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
	        }

	        // Replace placeholders
	        body = body.replace(MailConstant.NAME, user.getFirstName() != null ? user.getFirstName() : "User");
	        body = body.replace(MailConstant.LINK, link);
	        body = body.replace(MailConstant.SUPPORT, supportMail);

	    } catch (Exception e) {
	        log.error("Error constructing verification email body", e);
	        return false;
	    }

	    boolean sent = mailService.sendMail(to, cc, bcc, subject, body);
	    if (sent) {
	        log.info("Verification mail sent successfully to {}", user.getEmail());
	    } else {
	        log.warn("Failed to send verification mail to {}", user.getEmail());
	    }
	    return sent;
	}
	
	

}
