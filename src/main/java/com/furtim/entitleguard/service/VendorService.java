package com.furtim.entitleguard.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furtim.entitleguard.dto.VendorDto;
import com.furtim.entitleguard.entity.BuilderOrganization;
import com.furtim.entitleguard.entity.Vendor;
import com.furtim.entitleguard.repository.BuilderOrganizationRepository;
import com.furtim.entitleguard.repository.VendorRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VendorService {

	private final VendorRepository vendorRepo;
	
	private final BuilderOrganizationRepository builderOrganizationRepo;

	public ApiResponse addVendor(VendorDto vendorDto) {
		try {
			Optional<Vendor> vendor = vendorRepo.findOneById(vendorDto.getId());
			if (vendor.isPresent()) {
				addOrUpdateVendor(vendor.get(), vendorDto);
				return new ApiResponse(true, "Vendor updated Successfully");
			} else {
				addOrUpdateVendor(new Vendor(), vendorDto);
				return new ApiResponse(true, "Vendor Added Successfully");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	private void addOrUpdateVendor(Vendor vendor, VendorDto vendorDto) {
		vendor.setName(vendorDto.getName());
		vendor.setContact(vendorDto.getContact());
		vendor.setEmail(vendorDto.getEmail());
		vendor.setDescription(vendorDto.getDescription());
		vendor.setType(vendorDto.getType());
		Optional<BuilderOrganization> builder = builderOrganizationRepo.findOneById(vendorDto.getBuilderOrganizationId());
		builder.ifPresent(vendor::setBuilderOrganization);
		vendorRepo.save(vendor);

	}

	public ApiResponse deleteBuilderVendor(String id) {
		try {
			Optional<Vendor> vendor = vendorRepo.findOneById(id);
			if (vendor.isPresent()) {
				vendor.get().setIsActive(false);
				vendorRepo.save(vendor.get());
				return new ApiResponse(true, "Vendor Deleted Successfully");
			} else {
				return new ApiResponse(false, "Invalid Id ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong");
		}
	}

	public DefaultListResponse getBuilderVendor(String builderId) {
		try {
			List<Vendor> vendors = vendorRepo.findAllByIsActiveAndBuilderOrganization(true,builderId);
			return new DefaultListResponse(true, "Vendor Fertch Successfully",vendors);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong",null);
		}
	}

}
