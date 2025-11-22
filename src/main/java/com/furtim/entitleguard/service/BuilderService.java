package com.furtim.entitleguard.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.furtim.entitleguard.controller.MailController;
import com.furtim.entitleguard.dto.BuilderCustomerDto;
import com.furtim.entitleguard.dto.BuilderCustomerItemMapDto;
import com.furtim.entitleguard.dto.BuilderItemCategoryDto;
import com.furtim.entitleguard.dto.BuilderItemDto;
import com.furtim.entitleguard.dto.BuilderOrderManualDto;
import com.furtim.entitleguard.dto.BuilderOrganizationDto;
import com.furtim.entitleguard.dto.CustomerDetailDto;
import com.furtim.entitleguard.dto.CustomerItemMapDto;
import com.furtim.entitleguard.dto.FileResponseDto;
import com.furtim.entitleguard.dto.LoginDto;
import com.furtim.entitleguard.dto.UserInfoDto;
import com.furtim.entitleguard.entity.Address;
import com.furtim.entitleguard.entity.BillOfMaterials;
import com.furtim.entitleguard.entity.BuilderCustomer;
import com.furtim.entitleguard.entity.BuilderCustomerItemFiles;
import com.furtim.entitleguard.entity.BuilderCustomerItemMap;
import com.furtim.entitleguard.entity.BuilderItem;
import com.furtim.entitleguard.entity.BuilderOrganization;
import com.furtim.entitleguard.entity.Category;
import com.furtim.entitleguard.entity.Catlog;
import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.Files;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Orders;
import com.furtim.entitleguard.entity.Source;
import com.furtim.entitleguard.entity.Status;
import com.furtim.entitleguard.entity.UserInfo;
import com.furtim.entitleguard.entity.UserJwtToken;
import com.furtim.entitleguard.entity.UserPassword;
import com.furtim.entitleguard.repository.AddressRepository;
import com.furtim.entitleguard.repository.BillOfMaterialRepository;
import com.furtim.entitleguard.repository.BuilderCustomerItemFilesRepository;
import com.furtim.entitleguard.repository.BuilderCustomerItemMapRepository;
import com.furtim.entitleguard.repository.BuilderCustomerRepository;
import com.furtim.entitleguard.repository.BuilderItemRepository;
import com.furtim.entitleguard.repository.BuilderOrganizationRepository;
import com.furtim.entitleguard.repository.CategoryRepository;
import com.furtim.entitleguard.repository.CatlogRepository;
import com.furtim.entitleguard.repository.CustomerRepository;
import com.furtim.entitleguard.repository.CustomerSourceMapRepository;
import com.furtim.entitleguard.repository.OrderItemRepository;
import com.furtim.entitleguard.repository.OrderRepository;
import com.furtim.entitleguard.repository.SourceRepository;
import com.furtim.entitleguard.repository.StatusRepository;
import com.furtim.entitleguard.repository.UserInfoRepository;
import com.furtim.entitleguard.repository.UserJwtTokenRepository;
import com.furtim.entitleguard.repository.UserPasswordRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.response.JwtResponse;
import com.furtim.entitleguard.utils.FilesType;
import com.furtim.entitleguard.utils.JwtUtil;
import com.furtim.entitleguard.utils.LoggedKey;
import com.furtim.entitleguard.utils.StatusConst;
import com.furtim.entitleguard.utils.StatusModule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class BuilderService {

	private final UserInfoRepository userInfoRepo;

	private final UserPasswordRepository userPasswordRepo;

	private final ModelMapper modelMapper;

	private final UserJwtTokenRepository userJwtTokenRepo;

	private final JwtUtil jwtUtil;

	private final MailController mailController;

	private final BuilderItemRepository builderItemRepo;

	private final BuilderCustomerRepository builderCustomerRepo;

	private final BuilderOrganizationRepository builderOrganizationRepo;

	private final BuilderCustomerItemMapRepository builderCustomerItemMapRepo;

	private final FilesService filesService;

	private final StatusRepository statusRepo;

	private final BuilderCustomerItemFilesRepository builderCustomerItemFilesRepo;

	private final SourceRepository sourceRepo;

	private final AddressRepository addressRepo;

	private final CustomerRepository customerRepo;

	private final CustomerSourceMapRepository customerSourceMapRepo;

	private final OrderRepository orderRepo;

	private final OrderItemRepository orderItemRepo;

	private final CatlogRepository catlogRepo;
	
	private final BillOfMaterialRepository billOfMaterialRepo;
	
	private final CategoryRepository categoryRepo;

	public JwtResponse builderLogin(LoginDto loginDto) {
		try {
			Optional<UserInfo> userOpt = userInfoRepo.findOneByIsActiveAndEmail(true, loginDto.getEmail());
			if (userOpt.isEmpty()) {
				return new JwtResponse(false, "Invalid User. Please Sign Up", null);
			}

			UserInfo user = userOpt.get();
			Optional<UserPassword> updOpt = userPasswordRepo.findOneByUserInfoAndIsActive(user, true);
			if (updOpt.isEmpty()) {
				return new JwtResponse(false, "Password not set.", null);
			}

			UserPassword upd = updOpt.get();
			if (upd.getPassword() == null || upd.getPassword().isEmpty()) {
				return new JwtResponse(false, "Password not set.", null);
			}

			return passwordVerification(loginDto.getPassword(), user, upd);

		} catch (Exception e) {
			return new JwtResponse(false, "Error during login", null);
		}
	}

	private JwtResponse passwordVerification(String password, UserInfo userInfo, UserPassword userPassword) {
		try {
			boolean passwordCheck = BCrypt.checkpw(password, userPassword.getPassword());
			if (passwordCheck) {

				long daysPassword = ChronoUnit.DAYS.between(userPassword.getCreatedAt().toLocalDate(), LocalDate.now());

				if (daysPassword > 90) {
					return new JwtResponse(false, "Password has been Expired.Please reset your passord", null);
				}
				UserJwtToken token = generateBuilderJwtToken(userInfo, userPassword);

				return new JwtResponse(true, "Login Successful", token);
			} else {
				return new JwtResponse(false, "Invalid password ", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new JwtResponse(false, "Something went wrong", null);
		}
	}

	private UserJwtToken generateBuilderJwtToken(UserInfo userInfo, UserPassword userPassword) {
		UserInfoDto u = convertToDto(userInfo);
		String jwt = jwtUtil.generateBuilderToken(u);

		UserJwtToken token = new UserJwtToken();
		token.setUserInfo(userInfo);
		token.setJwt(jwt);
		token.setLogged(LoggedKey.IN.toString());
		return userJwtTokenRepo.save(token);
	}

	private UserInfoDto convertToDto(UserInfo userInfo) {
		try {
			return modelMapper.map(userInfo, UserInfoDto.class);
		} catch (Exception e) {
			return null;
		}
	}

	public String userObjectManager(UserInfoDto user) {

		ObjectMapper mapper = new ObjectMapper();
		mapper = JsonMapper.builder().findAndAddModules().build();
		try {
			return mapper.writeValueAsString(user);
		} catch (Exception e) {
			e.printStackTrace();
			return LoggedKey.EMPTY.toString();
		}
	}

	public ApiResponse sendVerifyMail(String email) {
		try {
			Optional<UserInfo> user = userInfoRepo.findOneByIsActiveAndEmail(true, email);
			if (user.isPresent()) {
				mailController.sendVerifyMail(user.get());
				return new ApiResponse(true, "Verification mail sent Successfully");

			} else {
				return new ApiResponse(true, "Invalid Mail Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	

	public ApiResponse addItems(BuilderItemDto itemDto) {
		try {
			Optional<BuilderItem> item = builderItemRepo.findOneById(itemDto.getId());
			if (item.isPresent()) {
				addOrUpdateItem(item.get(), itemDto);
				return new ApiResponse(true, "Item updated Successfully");
			} else {
				addOrUpdateItem(new BuilderItem(), itemDto);
				return new ApiResponse(true, "Item Added Successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	private void addOrUpdateItem(BuilderItem builderItem, BuilderItemDto itemDto) {
		builderItem.setName(itemDto.getName());
		builderItem.setBrand(itemDto.getBrand());
		builderItem.setCategory(itemDto.getCategory());
		builderItem.setPrice(itemDto.getPrice());
		builderItem.setNote(itemDto.getNote());
		builderItem.setDocumentationUrl(itemDto.getDocumentationUrl());
		builderItem.setMake(itemDto.getMake());
		builderItem.setModel(itemDto.getModel());
		builderItem.setPuchaser(itemDto.getPurchaser());
		builderItem.setText(itemDto.getText());
		Optional<BuilderOrganization> builder = builderOrganizationRepo.findOneById(itemDto.getBuilderOrganizationId());
		builder.ifPresent(builderItem::setBuilderOrganization);
		builderItemRepo.save(builderItem);

	}

	public DefaultListResponse getBuilderItem(String builderId) {
		try {
			List<BuilderItem> items = builderItemRepo.findAllIsActiveAndSource(true, builderId);

			Map<String, List<BuilderItemDto>> groupedMap = new HashMap<>();
			for (BuilderItem item : items) {
				BuilderItemDto dto = new BuilderItemDto();
				dto.setId(item.getId().toString());
				dto.setName(item.getName());
				dto.setMake(item.getMake());
				dto.setBrand(item.getBrand());
				dto.setModel(item.getModel());
				dto.setPurchaser(item.getPuchaser());
				dto.setBuilderOrganizationId(item.getBuilderOrganization().getId());
				dto.setDocumentationUrl(item.getDocumentationUrl());
				dto.setText(item.getText());
				dto.setNote(item.getNote());
				dto.setPrice(item.getPrice());
				dto.setStatus(item.getStatus());
				groupedMap.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(dto);
			}

			List<BuilderItemCategoryDto> result = new ArrayList<>();
			for (Map.Entry<String, List<BuilderItemDto>> entry : groupedMap.entrySet()) {
				result.add(new BuilderItemCategoryDto(entry.getKey(), entry.getValue()));
			}

			return new DefaultListResponse(true, "Items fetched successfully", result);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(true, "Something went wrong", null);
		}
	}

	public ApiResponse deleteBuilderItem(String id) {
		try {
			Optional<BuilderItem> btOptional = builderItemRepo.findOneById(id);
			if (btOptional.isPresent()) {
				List<BuilderCustomerItemMap> maps = builderCustomerItemMapRepo
						.findAllByBuilderItemAndIsActive(btOptional.get(), true);
				if (maps.isEmpty()) {
					btOptional.get().setIsActive(false);
					builderItemRepo.save(btOptional.get());
					return new ApiResponse(true, "Builder Item Deleted Successfully");
				} else {
					return new ApiResponse(false, "Item mapped with customer");
				}
			} else {
				return new ApiResponse(false, "Invalid Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	public DefaultListResponse addBuilderCustomer(BuilderCustomerDto customerDto) {
		try {
			BuilderCustomer savedCustomer;

			Optional<BuilderCustomer> customer = builderCustomerRepo.findOneById(customerDto.getId());
			if (customer.isPresent()) {
				savedCustomer = addOrUpdateCustomer(customer.get(), customerDto);
				return new DefaultListResponse(true, "Customer updated Successfully", savedCustomer);
			} else {
				savedCustomer = addOrUpdateCustomer(new BuilderCustomer(), customerDto);
				return new DefaultListResponse(true, "Customer Added Successfully", savedCustomer);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

	private BuilderCustomer addOrUpdateCustomer(BuilderCustomer builderCustomer, BuilderCustomerDto customerDto) {
		Status draft = statusRepo.findOneByModuleAndName(StatusModule.BUILDER.toString(), StatusConst.DRAFT.toString());
		if (draft != null) {
			builderCustomer.setStatus(draft);
		}
		builderCustomer.setFirstName(customerDto.getFirstName());
		builderCustomer.setLastName(customerDto.getLastName());
		builderCustomer.setEmail(customerDto.getEmail());
		builderCustomer.setContact(customerDto.getContact());
		Optional<BuilderOrganization> builder = builderOrganizationRepo
				.findOneById(customerDto.getBuilderOrganizationId());
		builder.ifPresent(builderCustomer::setBuilderOrganization);
		builderCustomer.setAddress(customerDto.getAddress());
		builderCustomer.setCity(customerDto.getCity());
		builderCustomer.setState(customerDto.getState());
		builderCustomer.setZip(customerDto.getZip());
		builderCustomer.setCountry(customerDto.getCountry());
		builderCustomer.setProjectName(customerDto.getProjectName());
		builderCustomer.setNotes(customerDto.getNotes());
		builderCustomer.setSettlementDate(customerDto.getSettlementDate());
		return builderCustomerRepo.save(builderCustomer);

	}

	public ApiResponse deleteBuilderCustomer(String id) {
		try {
			Optional<BuilderCustomer> btOptional = builderCustomerRepo.findOneById(id);
			if (btOptional.isPresent()) {
				btOptional.get().setIsActive(false);
				builderCustomerRepo.save(btOptional.get());
				return new ApiResponse(true, "Customer Deleted Successfully");

			} else {
				return new ApiResponse(true, "Invalid Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");

		}
	}

	public ApiResponse addBuilderOrganization(BuilderOrganizationDto builderDto) {
		try {
			Optional<BuilderOrganization> customer = builderOrganizationRepo.findOneById(builderDto.getId());
			if (customer.isPresent()) {
				addOrUpdateBuilderOrganization(customer.get(), builderDto);
				return new ApiResponse(true, "Builder Organization updated Successfully");
			} else {
				addOrUpdateBuilderOrganization(new BuilderOrganization(), builderDto);
				return new ApiResponse(true, "Builder Organization Added Successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	private void addOrUpdateBuilderOrganization(BuilderOrganization builder, BuilderOrganizationDto builderDto) {
		builder.setEmail(builderDto.getEmail());
		builder.setAddress(builderDto.getAddress());
		builder.setAbn(builderDto.getAbn());
		builder.setDescription(builderDto.getDescription());
		builder.setContact(builderDto.getContact());
		builder.setName(builderDto.getName());
		builderOrganizationRepo.save(builder);

	}

	public ApiResponse addBuilderCustomerItem(CustomerItemMapDto customerDto) {
		try {
			Optional<BuilderCustomer> customerOpt = builderCustomerRepo.findById(customerDto.getCustomerId());
			if (customerOpt.isEmpty()) {
				return new ApiResponse(false, "Customer not found");
			}

			BuilderCustomer customer = customerOpt.get();

			List<String> selectedItemIds = customerDto.getItemIds();

			List<BuilderCustomerItemMap> map = builderCustomerItemMapRepo.findAllByBuilderCustomer(customer);
			builderCustomerItemMapRepo.deleteAll(map);

			List<BuilderCustomerItemMap> newMappings = new ArrayList<>();

			for (String itemId : selectedItemIds) {
				builderItemRepo.findById(itemId).ifPresent(item -> {
					BuilderCustomerItemMap mapping = new BuilderCustomerItemMap();
					mapping.setBuilderCustomer(customer);
					mapping.setBuilderItem(item);
					newMappings.add(mapping);
				});
			}

			builderCustomerItemMapRepo.saveAll(newMappings);

			return new ApiResponse(true, "Customer item selection updated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong while updating customer items: " + e.getMessage());
		}
	}

	public DefaultListResponse getBuilderOrganization(String builderId) {
		try {
			Optional<BuilderOrganization> builder = builderOrganizationRepo.findOneByIdAndIsActive(builderId, true);
			if (builder.isPresent()) {
				return new DefaultListResponse(true, "organization fertch successfully", builder.get());
			} else {
				return new DefaultListResponse(false, "Invalid Id", builder.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong while getting organization", null);
		}
	}

	public DefaultListResponse getCustomerDetails(String customerId, String builderId) {
		try {
			Optional<BuilderCustomer> optionalCustomer = builderCustomerRepo.findById(customerId);
			if (optionalCustomer.isEmpty()) {
				return new DefaultListResponse(false, "Customer not found", null);
			}

			BuilderCustomer customer = optionalCustomer.get();

			List<BuilderItem> items = builderItemRepo.findAllIsActiveAndSource(true, builderId);
			List<BuilderCustomerItemMap> customerItemMaps = builderCustomerItemMapRepo
					.findAllByByCustomerIdAndIsActive(customerId, true);

			Map<String, BuilderCustomerItemMap> itemToMap = customerItemMaps.stream()
					.collect(Collectors.toMap(m -> m.getBuilderItem().getId(), m -> m));

			Map<String, List<BuilderItemDto>> groupedMap = new HashMap<>();
			int totalDocuments = 0;
			int mappedItemCount = 0;

			for (BuilderItem item : items) {
				BuilderItemDto dto = new BuilderItemDto();
				dto.setId(item.getId().toString());
				dto.setName(item.getName());
				dto.setMake(item.getMake());
				dto.setBrand(item.getBrand());
				dto.setModel(item.getModel());
				dto.setPurchaser(item.getPuchaser());
				dto.setDocumentationUrl(item.getDocumentationUrl());
				dto.setText(item.getText());
				dto.setNote(item.getNote());
				dto.setPrice(item.getPrice());
				dto.setStatus(item.getStatus());
				BuilderCustomerItemMap map = itemToMap.get(item.getId());
				if (map != null) {
					mappedItemCount++;
					dto.setMapped(true);
					dto.setBuilderCustomerMapId(map.getId());
					dto.setSeller(map.getSeller());
					dto.setSerialNumber(map.getSerialNumber());
					List<FileResponseDto> files = new ArrayList<>();

					List<BuilderCustomerItemFiles> itemFiles = builderCustomerItemFilesRepo
							.findAllByBuilderItemmapAndIsActive(map, true);
					if (!itemFiles.isEmpty()) {
						for (BuilderCustomerItemFiles bcif : itemFiles) {
							FileResponseDto file = new FileResponseDto();
							file.setId(bcif.getId());
							file.setMapId(bcif.getFiles().getId());
							file.setFileUrl(bcif.getFiles().getFilePath());
							file.setFileName(bcif.getFiles().getName());
							files.add(file);
						}
						totalDocuments += itemFiles.size();
					}
					dto.setDocumentCount(itemFiles.size());
					dto.setFileResponseDto(files);

				} else {
					dto.setMapped(false);
					dto.setBuilderCustomerMapId(null);
					dto.setSeller(null);
					dto.setSerialNumber(null);
					dto.setFileResponseDto(null);
				}

				groupedMap.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(dto);
			}

			List<BuilderItemCategoryDto> categoryList = new ArrayList<>();
			for (Map.Entry<String, List<BuilderItemDto>> entry : groupedMap.entrySet()) {
				categoryList.add(new BuilderItemCategoryDto(entry.getKey(), entry.getValue()));
			}

			Integer totalCategories = builderCustomerItemMapRepo.countByCustomerIdAndIsActive(customerId, true);
			int totalItems = mappedItemCount;
			int mappedItems = (int) items.stream().filter(i -> itemToMap.containsKey(i.getId())).count();
			int completionPercent = totalItems == 0 ? 0 : (mappedItems * 100 / totalItems);

			CustomerDetailDto detailDto = new CustomerDetailDto();

			detailDto.setDtos(categoryList);
			detailDto.setTotalItems(totalItems);
			detailDto.setTotalDocuments(totalDocuments);
			detailDto.setTotalCategories(totalCategories);
			detailDto.setCompletionPercent(completionPercent);
			detailDto.setCustomer(customer);
			detailDto.setDtos(categoryList);
			return new DefaultListResponse(true, "Customer mapped items fetched successfully", detailDto);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

	public ApiResponse addOrUpdateUser(List<BuilderCustomerItemMapDto> builderMaps) {
		try {
			for (BuilderCustomerItemMapDto dto : builderMaps) {
				Optional<BuilderCustomerItemMap> mapOpt = builderCustomerItemMapRepo.findOneById(dto.getId());
				if (mapOpt.isPresent()) {
					BuilderCustomerItemMap map = mapOpt.get();
					map.setSeller(dto.getSeller());
					map.setSerialNumber(dto.getSerialNumber());

					if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
						for (MultipartFile file : dto.getFiles()) {
							BuilderCustomerItemFiles itemFiles = new BuilderCustomerItemFiles();
							itemFiles.setBuilderItemmap(map);
							Files uploadedFile = filesService.uploadFile(file, map.getId(), FilesType.DOCUMENT,
									dto.getId());
							itemFiles.setFiles(uploadedFile);
							builderCustomerItemFilesRepo.save(itemFiles);

						}

					}

					builderCustomerItemMapRepo.save(map);
				} else {
					return new ApiResponse(false, "Invalid ID: " + dto.getId());
				}
			}
			return new ApiResponse(true, "Updated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong: " + e.getMessage());
		}
	}

	public DefaultListResponse getDashBoardCount(String builderId) {
		try {
			Map<String, Integer> countMap = new HashMap<>();

			Status entitlement = statusRepo.findOneByModuleAndName(StatusModule.BUILDER.toString(),
					StatusConst.ENTITLEMENT.toString());
			Status draft = statusRepo.findOneByModuleAndName(StatusModule.BUILDER.toString(),
					StatusConst.DRAFT.toString());
			Status review = statusRepo.findOneByModuleAndName(StatusModule.BUILDER.toString(),
					StatusConst.READY_FOR_REVIEW.toString());
			Integer totalHomeowners = builderCustomerRepo.countByIsActiveAndBuilder(true, builderId);

			Integer entitlementsSent = builderCustomerRepo.countByIsActiveAndBuilderAndStatus(true, builderId,
					entitlement);

			Integer pending = builderCustomerRepo.countByIsActiveAndBuilderAndStatus(true, builderId, draft);

			Integer readyForReview = builderCustomerRepo.countByIsActiveAndBuilderAndStatus(true, builderId, review);

			countMap.put("totalHomeowners", totalHomeowners);
			countMap.put("entitlementsSent", entitlementsSent);
			countMap.put("pending", pending);
			countMap.put("readyForReview", readyForReview);

			return new DefaultListResponse(true, "Dashboard counts fetched successfully", countMap);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

	public DefaultListResponse getDashBoardCustomerList(String builderId) {
		try {
			List<BuilderCustomer> customers = builderCustomerRepo.findAllByIsActiveAndBuilderId(true, builderId);
			return new DefaultListResponse(true, "Customers fetched successfully", customers);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

	public ApiResponse createManualBuilderOrder(BuilderOrderManualDto dto) {
		try {

			Source source = sourceRepo.findByCode(dto.getBuilderName());
			if (source == null) {
				source = new Source();
				source.setName(dto.getBuilderName());
				source.setCode(dto.getBuilderName());
				source = sourceRepo.save(source);
			}

			Optional<Customer> customerOpt = customerRepo.findOneById(dto.getCustomerId());
			if (customerOpt.isEmpty()) {
				return new ApiResponse(false, "Customer not found");
			}

			Customer customer = customerOpt.get();
			String referenceId = UUID.randomUUID().toString();

			CustomerSourceMap customerSourceMap = customerSourceMapRepo
					.findByCustomerIdAndSourceIdAndReferenceId(customerOpt.get().getId(), source.getId(), referenceId)
					.orElseGet(null);
			if (customerSourceMap == null) {
				customerSourceMap = new CustomerSourceMap();
				customerSourceMap.setCustomer(customer);
				customerSourceMap.setSource(source);
				customerSourceMap.setReferenceId(UUID.randomUUID().toString());
				customerSourceMap = customerSourceMapRepo.save(customerSourceMap);
			}

			Orders order = new Orders();
			order.setCustomerSourceMap(customerSourceMap);
			order.setDate(dto.getSettlementDate());
			order.setStatus("paid");
			order.setFulfilmentStatus("fulfilled");
			order.setActualPrice(0.0);
			order.setTotalPrice(0.0);
			order.setTax(0.0);
			order.setQuantity((double) dto.getCatlogIds().size());
			order.setOrderId(UUID.randomUUID().toString());

			Address shipToAddress = new Address();
			shipToAddress.setStreet(dto.getAddress());
			shipToAddress.setCity(dto.getCity());
			shipToAddress.setState(dto.getState());
			shipToAddress.setZipCode(dto.getZip());
			shipToAddress.setCountry(dto.getCountry() != null ? dto.getCountry() : "India");
			shipToAddress = addressRepo.save(shipToAddress);

			order.setShipToAddress(shipToAddress);
			order = orderRepo.save(order);

			for (String catlogId : dto.getCatlogIds()) {
				Catlog item = catlogRepo.findById(catlogId)
						.orElseThrow(() -> new RuntimeException("Invalid catalog ID: " + catlogId));

				OrderItem orderItem = new OrderItem();
				orderItem.setOrder(order);
				orderItem.setProductId(item.getId());
				orderItem.setProductName(item.getName());
				orderItem.setBrand(item.getBrand());
				orderItem.setSku(item.getModel());
				orderItem.setUnit("unit");
				orderItem.setDiscount(0.0);
				orderItem.setProductImageUrl(item.getDocumentationUrl());
				orderItemRepo.save(orderItem);

			}

			orderRepo.save(order);

			return new ApiResponse(true, "Builder manual order created successfully");

		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong: " + e.getMessage());
		}
	}

	public ApiResponse deleteBuilderCustomerItemFile(String id) {
		try {
			Optional<BuilderCustomerItemFiles> fileOpt = builderCustomerItemFilesRepo.findOneByIdAndIsActive(id, true);
			if (fileOpt.isEmpty()) {
				return new ApiResponse(false, "File not found or already deleted");
			}
			BuilderCustomerItemFiles file = fileOpt.get();
			file.setIsActive(false);
			builderCustomerItemFilesRepo.save(file);
			return new ApiResponse(true, "File deleted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong while deleting file");
		}

	}

	public DefaultListResponse getBillOfMaterials() {
		try {
			List<BillOfMaterials> billMaterials = billOfMaterialRepo.findAllByIsActive(true);
			return new DefaultListResponse(true, "Bill Of Materials",
					billMaterials.isEmpty() ? List.of() : billMaterials);
		} catch (Exception e) {
			log.error("An error occured while the bill materials : ", e);
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

	public DefaultListResponse getBillMaterials(String billId) {
		try {
			List<BuilderItem> items = builderItemRepo.findByBillOfMaterialsAndIsActive(billId, true);
			return new DefaultListResponse(true, "Builder Items ", items.isEmpty() ? List.of() : items);
		} catch (Exception e) {
			log.error("An error occured while fecthing the bill materials : ", e);
			return new DefaultListResponse(false, "Something went wrong", null);

		}
	}

	public DefaultListResponse getCategorys() {
		try {
			List<Category> categorys = categoryRepo.findAllByIsActive(true);
			return new DefaultListResponse(true, "Categorys fetched", categorys.isEmpty() ? List.of() : categorys);
		} catch (Exception e) {
			log.error("An error occured while fecthing the categorys : ", e);
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}

}
