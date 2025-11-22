package com.furtim.entitleguard.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.furtim.entitleguard.controller.MailController;
import com.furtim.entitleguard.controller.SmsController;
import com.furtim.entitleguard.dto.AddressDto;
import com.furtim.entitleguard.dto.CustomerDto;
import com.furtim.entitleguard.dto.LoginDto;
import com.furtim.entitleguard.entity.Address;
import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.UserJwtToken;
import com.furtim.entitleguard.repository.AddressRepository;
import com.furtim.entitleguard.repository.CustomerRepository;
import com.furtim.entitleguard.repository.UserJwtTokenRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.response.JwtResponse;
import com.furtim.entitleguard.utils.JwtUtil;
import com.furtim.entitleguard.utils.LoggedKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepo;

	private final ModelMapper modelMapper;

	private final MailController mailController;

	private final SmsController smsController;

	private final UserJwtTokenRepository userJwtTokenRepo;

	private final JwtUtil jwtUtil;

	private final AddressRepository addressRepo;
	
	@Value("${jwt.secret}")
	private String secret;
	
	 private Key getSigningKey() {
	        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	    }
    
//    public CustomerService(CustomerRepository customerRepo, ModelMapper modelMapper,MailController mailController,SmsController smsController,
//    		UserJwtTokenRepository userJwtTokenRepo,JwtUtil jwtUtil,AddressRepository addressRepo) {
//		this.customerRepo = customerRepo;
//		this.modelMapper = modelMapper;
//		this.mailController = mailController;
//		this.smsController = smsController;
//		this.userJwtTokenRepo = userJwtTokenRepo;
//		this.jwtUtil = jwtUtil;
//		this.addressRepo = addressRepo;
//	}


//	
//	 private Key getSigningKey() {
//	        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//	    }

	public JwtResponse basicLogin(LoginDto loginDto) {
		try {
			String loginType = loginDto.getLoginType();

			if ("phone".equalsIgnoreCase(loginType)) {
				return handlePhoneLogin(loginDto);
			} else if ("email".equalsIgnoreCase(loginType)) {
				return handleEmailLogin(loginDto);
			} else if ("google".equalsIgnoreCase(loginType)) {
				return handleGoogleLogin(loginDto);
			} else {
				return new JwtResponse(false, "Invalid login type", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Something went wrong during login", null);
		}
	}

	private JwtResponse handlePhoneLogin(LoginDto dto) {
		try {
			if (dto.getContact() == null || dto.getContact().isEmpty()) {
				return new JwtResponse(false, "Phone number is required", null);
			}

			if (dto.getOtp() == null || dto.getOtp().isEmpty()) {
				return sendOtpToPhone(dto.getContact());
			} else {
				return verifyPhoneOtpAndLogin(dto.getContact(), dto.getOtp());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Error during phone login", null);
		}
	}

	private JwtResponse handleEmailLogin(LoginDto dto) {
		try {
			if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
				return new JwtResponse(false, "Email is required", null);
			}

			if (dto.getOtp() == null || dto.getOtp().isEmpty()) {
				return sendOtpToEmail(dto.getEmail());
			} else {
				return verifyEmailOtpAndLogin(dto.getEmail(), dto.getOtp());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Error during email login", null);
		}
	}

	private JwtResponse handleGoogleLogin(LoginDto dto) {
		try {
			if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
				return new JwtResponse(false, "Email is required for Google login", null);
			}

			Optional<Customer> userOpt = customerRepo.findByEmailAndIsActiveAndIsRegistered(dto.getEmail(), true, true);

			Customer customer;
			if (userOpt.isPresent()) {
				customer = userOpt.get();
			} else {

				customer = new Customer();
				customer.setEmail(dto.getEmail());
				customer.setIsRegistered(true);
				customer.setIsActive(true);
				customer = customerRepo.save(customer);
			}

			return createLoginResponse(customer);

		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Error during Google login", null);
		}
	}


	public JwtResponse sendOtpToPhone(String phone) {
		try {
			Optional<Customer> user = customerRepo.findByContactAndIsActiveAndIsRegistered(phone, true, true);
			if (user.isEmpty())
				return new JwtResponse(false, "Phone not registered, Please Sign Up", null);

			String otp = String.format("%06d", new Random().nextInt(1000000));
			user.get().setOtp(otp);
			customerRepo.save(user.get());
			smsController.sendOtp(phone, otp);
			return new JwtResponse(true, "OTP sent to phone", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to send OTP to phone", null);
		}
	}
	
	public JwtResponse sendOtpToPhoneForNonRegister(String phone) {
		try {
			Optional<Customer> user = customerRepo.findByContactAndIsActive(phone, true);
			if (user.isEmpty())
				return new JwtResponse(false, "Phone not registered, Please Sign Up", null);

			String otp = String.format("%06d", new Random().nextInt(1000000));
			user.get().setOtp(otp);
			customerRepo.save(user.get());
			smsController.sendOtp(phone, otp);
			return new JwtResponse(true, "OTP sent to phone", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to send OTP to phone", null);
		}
	}

	public JwtResponse verifyPhoneOtpAndLogin(String phone, String otp) {
		try {
			Optional<Customer> customerOpt = customerRepo.findByContactAndIsActive(phone, true);

			if (customerOpt.isEmpty()) {
				log.warn("Attempt to login with non-existent or inactive phone: {}", phone);
				return new JwtResponse(false, "Invalid User", null);
			}

			Customer customer = customerOpt.get();

			if (otp != null && otp.equalsIgnoreCase(customer.getOtp())) {
				customer.setIsRegistered(true);
				customerRepo.save(customer);
				log.info("OTP verified successfully for phone: {}", phone);
				return createLoginResponse(customer);
			} else {
				log.warn("Invalid OTP for phone: {}", phone);
				return new JwtResponse(false, "Verification code entered is incorrect. Please enter a valid OTP.",
						null);
			}

		} catch (Exception e) {
			log.error("Exception during OTP verification for phone: {}", phone, e);
			return new JwtResponse(false, "Something went wrong during OTP verification", null);
		}
	}

	public JwtResponse sendOtpToEmail(String email) {
		try {
			Optional<Customer> customer = customerRepo.findByEmailAndIsActiveAndIsRegistered(email, true, true);
			if (customer.isEmpty())
				return new JwtResponse(false, "Email not registered, Please Sign Up", null);
			String otp;
			if ("demo-user@entitleguard.com".equals(customer.get().getEmail())) {
	            otp = "111111";
	        } else {
	        	otp = String.format("%06d", new Random().nextInt(1000000));
	        }
			customer.get().setOtp(otp);
			customerRepo.save(customer.get());
			mailController.sendLoginOtp(customer.get());
			return new JwtResponse(true, "OTP sent to email", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to send OTP to email", null);
		}
	}
	
	public JwtResponse sendOtpToEmailForNonRegister(String email) {
		try {
			Optional<Customer> customer = customerRepo.findByEmailAndIsActive(email, true);
			if (customer.isEmpty())
				return new JwtResponse(false, "Email not registered, Please Sign Up", null);
			String otp = String.format("%06d", new Random().nextInt(1000000));
			customer.get().setOtp(otp);
			customerRepo.save(customer.get());
			mailController.sendLoginOtp(customer.get());
			log.info("non register ",customer.get().getIsRegistered());
			return new JwtResponse(true, "OTP sent to email", null);
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to send OTP to email", null);
		}
	}

	public JwtResponse verifyEmailOtpAndLogin(String email, String otp) {
		try {
			Optional<Customer> customer = customerRepo.findOneByEmailAndIsActive(email, true);

			if (customer.isPresent()) {
				Customer cus = customer.get();

				if (otp != null && otp.equalsIgnoreCase(cus.getOtp())) {
					log.info("OTP matched for email: {}", email);
					cus.setIsRegistered(true);
					customerRepo.save(cus);
					return createLoginResponse(cus);
				} else {
					log.warn("Invalid OTP for email: {}", email);
					return new JwtResponse(false, "Verification Code Entered Wrong. Please enter the valid OTP", null);
				}
			} else {
				return new JwtResponse(false, "Invalid User", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Something went wrong during OTP verification", null);
		}
	}

	private JwtResponse createLoginResponse(Customer customer) {
		try {
			UserJwtToken token = generateUserJwtToken(customer);
			return new JwtResponse(true, "Login Successful", token);
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to create login token", null);
		}
	}

	private UserJwtToken generateUserJwtToken(Customer customer) {
		CustomerDto u = convertToDto(customer);
		String jwt = jwtUtil.generateToken(u);

		UserJwtToken token = new UserJwtToken();
		token.setCustomer(customer);
		token.setJwt(jwt);
		token.setLogged(LoggedKey.IN.toString());
		return userJwtTokenRepo.save(token);
	}

	public CustomerDto convertToDto(Customer customer) {
		try {
			return modelMapper.map(customer, CustomerDto.class);
		} catch (Exception e) {
			return null;
		}
	}

	public JwtResponse sendOtp(LoginDto loginDto) {
		try {
			String loginType = loginDto.getLoginType();
			String contact = loginDto.getContact();
			String email = loginDto.getEmail();

			Optional<Customer> customer = Optional.empty();
			if ("phone".equalsIgnoreCase(loginType) && contact != null) {
				customer = customerRepo.findByContactAndIsActive(contact, true);
			} else if ("email".equalsIgnoreCase(loginType) && email != null) {
				customer = customerRepo.findByEmailAndIsActive(email, true);
			} else {
				return new JwtResponse(false, "Invalid login type or missing contact/email", null);
			}

			if (customer.isPresent()) {
				if (Boolean.TRUE.equals(customer.get().getIsRegistered())) {
					return new JwtResponse(false, "Your account is already registered", null);
				} else {
					customerRepo.save(customer.get());
					if ("phone".equalsIgnoreCase(loginType)) {
						return sendOtpToPhoneForNonRegister(customer.get().getContact());
					} else if ("email".equalsIgnoreCase(loginType)) {
						return sendOtpToEmailForNonRegister(customer.get().getEmail());
					}
				}
			} else {
				Customer customers = new Customer();
				if ("phone".equalsIgnoreCase(loginType)) {
					customers.setContact(contact);
				} else if ("email".equalsIgnoreCase(loginType)) {
					customers.setEmail(email);
				}
				customers.setIsActive(true);

				customers = customerRepo.save(customers);

				if ("phone".equalsIgnoreCase(loginType)) {
					return sendOtpToPhoneForNonRegister(customers.getContact());
				} else if ("email".equalsIgnoreCase(loginType)) {
					return sendOtpToEmailForNonRegister(customers.getEmail());
				}
			}

			return new JwtResponse(false, "Invalid request - OTP not sent", null);

		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Failed to send Otp", null);
		}
	}

	public JwtResponse otpVerify(LoginDto loginDto) {
		try {
			
			String loginType = loginDto.getLoginType();
			if ("email".equalsIgnoreCase(loginType) && loginDto.getOtp() != null && loginDto.getEmail() != null) {
				return verifyEmailOtpAndLogin(loginDto.getEmail(), loginDto.getOtp());
			} else if ("phone".equalsIgnoreCase(loginType) && loginDto.getContact() != null && loginDto.getOtp() != null) {
				return verifyPhoneOtpAndLogin(loginDto.getContact(), loginDto.getOtp());
			} else {
				return new JwtResponse(false, "OTP and Email/Phone required", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Something went wrong during OTP verification", null);
		}
	}

	public ApiResponse updateCustomer(CustomerDto customerDto) {
		try {
			Optional<Customer> customer = customerRepo.findOneById(customerDto.getId());
			if (customer.isPresent()) {
				createOrUpdateCustomer(customer.get(), customerDto);
				return new ApiResponse(true, "Customer Updated Successfully");
			} else {
				return new ApiResponse(false, "Invalid Customer Id ");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	private void createOrUpdateCustomer(Customer customer, CustomerDto customerDto) {
		if (customerDto.getName() != null) {
			customer.setName(customerDto.getName());
		}

		if (customerDto.getDob() != null) {
			customer.setDob(customerDto.getDob());
		}

		if (customerDto.getContact() != null) {
			customer.setContact(customerDto.getContact());
		}

		if (customerDto.getEmail() != null) {
			customer.setEmail(customerDto.getEmail());
		}

		if (customerDto.getAddressDto() != null) {
			createOrUpdateAddress(customer, customerDto);
		}

		customerRepo.save(customer);

	}

	private void createOrUpdateAddress(Customer customer, CustomerDto customerDto) {
		Address address;
		Optional<Address> addrOpt = addressRepo.findOneById(customerDto.getAddressDto().getId());

		if (addrOpt.isPresent()) {
			address = addrOpt.get();

		} else {
			address = new Address();

		}

		AddressDto addressDto = customerDto.getAddressDto();

		if (addressDto.getApt() != null) {
			address.setApt(addressDto.getApt());
		}
		
		if (addressDto.getCity() != null) {
			address.setCity(addressDto.getCity());
		}
		
		if (addressDto.getCountry() != null) {
			address.setCountry(addressDto.getCountry());
		}
		
		if (addressDto.getState() != null) {
			address.setState(addressDto.getState());
		}
		
		if (addressDto.getStreet() != null) {
			address.setStreet(addressDto.getStreet());
		}
		if (addressDto.getZipCode() != null) {
			address.setZipCode(addressDto.getZipCode());
		}

		addressRepo.save(address);
		customer.setAddress(address);
		customerRepo.save(customer);

	}

	public DefaultListResponse presentCustomer(String phone, String email) {
	    try {
	        if (phone != null && !phone.isEmpty()) {
	            Optional<Customer> num = customerRepo.findByContactAndIsActiveAndIsRegistered(phone, true,true);
	            if (num.isPresent()) {
	                return new DefaultListResponse(false, "Contact Already Exists", null);
	            }
	        }

	        if (email != null && !email.isEmpty()) {
	            Optional<Customer> emails = customerRepo.findByEmailAndIsActiveAndIsRegistered(email, true,true);
	            if (emails.isPresent()) {
	                return new DefaultListResponse(false, "Email Already Exists", null);
	            }
	        }

	        return new DefaultListResponse(true, "Customer not present", null);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new DefaultListResponse(false, "Something went wrong", null);
	    }
	}

	public DefaultListResponse checkValidToken(String token) {
		Map<String, Object> response = new HashMap<>();
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();

			boolean isExpired = claims.getExpiration().before(new Date());

			response.put("expired", isExpired);

			return new DefaultListResponse(true, "Success", response);

		} catch (Exception e) {
			e.printStackTrace();

			response.put("expired", true);
	        return new DefaultListResponse(true, "Token is invalid or expired", response);
		}
	}


}
