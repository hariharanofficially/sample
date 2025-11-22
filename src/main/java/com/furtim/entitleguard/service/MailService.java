package com.furtim.entitleguard.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.furtim.entitleguard.dto.LoginDto;
import com.furtim.entitleguard.entity.UserInfo;
import com.furtim.entitleguard.entity.UserPassword;
import com.furtim.entitleguard.repository.UserInfoRepository;
import com.furtim.entitleguard.repository.UserPasswordRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailService {
	
	    @Value("${sendgrid.api.key}")
	    private String sendGridApiKey;

	    @Value("${mail.header}")
	    private String header;
	    
		@Value("${jwt.secret}")
		private String secret;
		
		private final UserPasswordRepository userPasswordRepo;
		
		private final UserInfoRepository userInfoRepo;

		private Key getSigningKey() {
			return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
		
		public MailService(UserPasswordRepository userPasswordRepo, UserInfoRepository userInfoRepo) {
			this.userInfoRepo = userInfoRepo;
			this.userPasswordRepo = userPasswordRepo;
		
		}
	 
	 
	    public boolean sendMail(String[] to, String[] cc, String[] bcc, String subject, String body) {
	        Email from = new Email(header);
	        Content content = new Content("text/html", body);
	        Mail mail = new Mail();
	        mail.setFrom(from);
	        mail.setSubject(subject);
	        mail.addContent(content);
	        
	        System.out.println("SendGrid Key: " + sendGridApiKey); 


	        // Create personalization
	        Personalization personalization = new Personalization();

	        // Add "To"
	        for (String toEmail : to) {
	            personalization.addTo(new Email(toEmail));
	        }

	        // Add "Cc"
	        if (cc != null) {
	            for (String ccEmail : cc) {
	                personalization.addCc(new Email(ccEmail));
	            }
	        }

	        // Add "Bcc"
	        if (bcc != null) {
	            for (String bccEmail : bcc) {
	                personalization.addBcc(new Email(bccEmail));
	            }
	        }

	        // Attach personalization to mail
	        mail.addPersonalization(personalization);

	        SendGrid sg = new SendGrid(sendGridApiKey);
	        Request request = new Request();

	        try {
	            request.setMethod(Method.POST);
	            request.setEndpoint("mail/send");
	            request.setBody(mail.build());
	            Response response = sg.api(request);

	            System.out.println("Status Code: " + response.getStatusCode());
	            return response.getStatusCode() == 202;
	        } catch (IOException ex) {
	            ex.printStackTrace();
	            return false;
	        }
	    }
	    
	    public ApiResponse setPasswordForUser(LoginDto loginDto) {
			try {
				if ((loginDto.getEmail() != null && !loginDto.getEmail().isEmpty())
						|| (loginDto.getToken() != null && !loginDto.getToken().isEmpty())) {

					String email = null;
					if (loginDto.getToken() != null && !loginDto.getToken().isEmpty()) {
//						Claims claims = Jwts.parserBuilder()
//								.setSigningKey(Keys.hmacShaKeyFor(
//										"Ftis4win@entitlementguardversion2025".getBytes(StandardCharsets.UTF_8)))
//								.build().parseClaimsJws(loginDto.getToken()).getBody();
						
						 Claims claims = Jwts.parserBuilder()
			                        .setSigningKey(getSigningKey())
			                        .build()
			                        .parseClaimsJws(loginDto.getToken())
			                        .getBody();

						email = claims.get("mail", String.class);
						log.info("email {}", email);
					} else {
						email = loginDto.getEmail();
					}

					Optional<UserInfo> user = userInfoRepo.findOneByEmailAndIsActive(email, true);

					if (user.isPresent()) {
						return updateUserPassword(user.get(), loginDto.getPassword());
					} else {
						return new ApiResponse(false, "Invalid User or Supplier");
					}
				} else {
					return new ApiResponse(false, "Invalid Request. Either email or token must be provided.");
				}
			} catch (Exception e) {
				log.error("Error in setPasswordForUser", e);
				return new ApiResponse(false, "Something went wrong");
			}
		}

		private ApiResponse updateUserPassword(UserInfo userInfo, String password) {
			Optional<UserPassword> upd = userPasswordRepo.findOneByUserInfoAndIsActive(userInfo, true);
			log.info("upd {}", upd);
			String pwd = generatePassword(password);

			if (upd.isPresent()) {
				upd.get().setPassword(pwd);
				upd.get().setCreatedAt(LocalDateTime.now());
				userPasswordRepo.save(upd.get());
			} else {
				UserPassword userpd = new UserPassword();
				userpd.setPassword(pwd);
				userpd.setUserInfo(userInfo);
				userPasswordRepo.save(userpd);
			}

			return new ApiResponse(true, "Password Updated Successfully");
		}

		public String generatePassword(String password) {
			return BCrypt.hashpw(password, BCrypt.gensalt(12));
		}


}
