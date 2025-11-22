package com.furtim.entitleguard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furtim.entitleguard.dto.QueryDetailDto;
import com.furtim.entitleguard.dto.QueryDto;
import com.furtim.entitleguard.dto.QueryFileMapDto;
import com.furtim.entitleguard.entity.Files;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Query;
import com.furtim.entitleguard.entity.QueryFileMap;
import com.furtim.entitleguard.entity.Status;
import com.furtim.entitleguard.entity.Vendor;
import com.furtim.entitleguard.repository.OrderItemRepository;
import com.furtim.entitleguard.repository.QueryFileMapRepository;
import com.furtim.entitleguard.repository.QueryRepository;
import com.furtim.entitleguard.repository.StatusRepository;
import com.furtim.entitleguard.repository.VendorRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.utils.FilesType;
import com.furtim.entitleguard.utils.StatusConst;
import com.furtim.entitleguard.utils.StatusModule;
import com.furtim.entitleguard.utils.UserSessionUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class QueryService {
	
	private final QueryRepository queryRepo;
	
	private final QueryFileMapRepository queryFileMapRepo;
	
	private final VendorRepository vendorRepo;
	
	private final OrderItemRepository orderItemRepo;
	
	private final FilesService filesService;
	
	private final StatusRepository statusRepo;


	public ApiResponse addOrUpdateQuery(QueryDto queryDto) {
	    try {
	        Optional<Query> existing = queryRepo.findOneById(queryDto.getId());

	        if (existing.isPresent()) {
	            createOrUpdateQuery(existing.get(), queryDto, false);
	            return new ApiResponse(true, "Query Updated Successfully");
	        } else {

	            Query newQuery = new Query();

	            Status created = statusRepo.findOneByModuleAndName(
	                StatusModule.QUERY.toString(),
	                StatusConst.CREATED.toString()
	            );
	            newQuery.setStatus(created);

	            createOrUpdateQuery(newQuery, queryDto, true);
	            return new ApiResponse(true, "Query Added Successfully");
	        }

	    } catch (Exception e) {
	        return new ApiResponse(false, "Something went wrong");
	    }
	}


	private void createOrUpdateQuery(Query query, QueryDto queryDto, boolean isNew) {
		Optional<OrderItem> item = orderItemRepo.findOneById(queryDto.getOrderItemId());
		item.ifPresent(query::setOrderItem);
		if (!isNew) {
	        Optional<Status> status = statusRepo.findOneById(queryDto.getStatusId());
	        status.ifPresent(query::setStatus);
	    }
		Optional<Status> status = statusRepo.findOneById(queryDto.getStatusId());
		status.ifPresent(query::setStatus);
		Optional<Vendor> vendor = vendorRepo.findOneById(queryDto.getVendorId());
		vendor.ifPresent(query::setVendor);
		query.setDueDate(queryDto.getDueDate());
		query.setTitle(queryDto.getTitle());
		query.setDescription(queryDto.getDescription());
		query.setPriorityLevel(queryDto.getPriorityLevel());
		queryRepo.save(query);
		
		if (queryDto.getQueryFileMapDto() != null) {
			for(QueryFileMapDto dto:queryDto.getQueryFileMapDto()) {
				QueryFileMap fileMap = new QueryFileMap();
				fileMap.setQuery(query);
				fileMap.setType(dto.getType());

				// Save to generate ID
				fileMap = queryFileMapRepo.save(fileMap);

				// Now upload file
				Files uploadedFile = filesService.uploadFile(dto.getFiles(), fileMap.getId(), FilesType.DOCUMENT, dto.getId());
				fileMap.setFiles(uploadedFile);

				queryFileMapRepo.save(fileMap);

		
			}
			
			
		}
	}

	public DefaultListResponse getQueryList() {
		try {
			String customer = UserSessionUtil.getUserInfo().getId();
			List<Query> queries = queryRepo.findAllByIsActiveAndCustomer(true, customer);
			List<QueryDetailDto> list = new ArrayList<>();
			for (Query query : queries) {
				QueryDetailDto dto = new QueryDetailDto();
				dto.setId(query.getId());
				dto.setDate(query.getCreatedAt().toLocalDate());
				dto.setStatusName(query.getStatus().getName());
				dto.setTitle(query.getTitle());
				list.add(dto);
			}

			return new DefaultListResponse(false, "Query fertch successfully", list);
		} catch (Exception e) {
			return new DefaultListResponse(false, "Something went wrong", null);
		}
	}
	
	

}
