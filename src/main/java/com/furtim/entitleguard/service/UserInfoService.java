package com.furtim.entitleguard.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furtim.entitleguard.dto.UserInfoDto;
import com.furtim.entitleguard.entity.BuilderOrganization;
import com.furtim.entitleguard.entity.UserInfo;
import com.furtim.entitleguard.repository.BuilderOrganizationRepository;
import com.furtim.entitleguard.repository.UserInfoRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserInfoService {

	private final UserInfoRepository userInfoRepo;

	private final BuilderService builderService;
	
	private final BuilderOrganizationRepository builderOrganizationRepo;

	public ApiResponse addOrUpdateUserInfo(UserInfoDto userInfoDto) {
		try {
			Optional<UserInfo> user = userInfoRepo.findOneById(userInfoDto.getId());
			if (user.isPresent()) {
				addOrUpdateUserInfo(user.get(), userInfoDto);
				return new ApiResponse(true, "User Added Successfully");
			} else {
				addOrUpdateUserInfo(new UserInfo(), userInfoDto);
				builderService.sendVerifyMail(userInfoDto.getEmail());
				return new ApiResponse(true, "User Added Successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	private void addOrUpdateUserInfo(UserInfo userInfo, UserInfoDto userInfoDto) {
		userInfo.setFirstName(userInfoDto.getFirstName());
		userInfo.setLastName(userInfoDto.getLastName());
		userInfo.setEmail(userInfoDto.getEmail());
		userInfo.setRole(userInfoDto.getRole());
		userInfo.setContact(userInfoDto.getContact());
		Optional<BuilderOrganization> builder = builderOrganizationRepo.findOneById(userInfoDto.getBuilderOrganizationId());
		builder.ifPresent(userInfo::setBuilderOrganization);
		userInfoRepo.save(userInfo);
	}

	public DefaultListResponse getBuilderUser(String builderId) {
		try {
			List<UserInfo> user = userInfoRepo.findAllByIsActiveAndBuilder(true, builderId);
			return new DefaultListResponse(true, "User fetched successfully", user);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(true, "Something went wrong", null);
		}
	}

	public ApiResponse deleteUser(String id) {
		try {
			Optional<UserInfo> user = userInfoRepo.findById(id);
			if (user.isPresent()) {
				user.get().setIsActive(false);
				userInfoRepo.save(user.get());
				return new ApiResponse(true, "User fetched successfully");
			} else {
				return new ApiResponse(false, "Invalid Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(true, "Something went wrong");
		}
	}

}
