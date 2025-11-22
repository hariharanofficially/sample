package com.furtim.entitleguard.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.entity.Files;
import com.furtim.entitleguard.repository.FilesRepository;
import com.furtim.entitleguard.service.FilesService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
public class FileController {
	
	private final FilesRepository filesRepo;
	
	private final FilesService filesService;
	
	@Value("${file.uploadDir}")
	private String fileDir;
	
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	
	public FileController(FilesRepository filesRepo, FilesService filesService) {
        this.filesRepo = filesRepo;
        this.filesService = filesService;
    }
	
	@GetMapping(value = "/unsecure/view/{fId}")
	public ResponseEntity<Resource> getFileResourceById(HttpServletRequest request, @PathVariable String fId)
	        throws IOException {
	    log.info("File Id {}", fId);

	    Files file = filesRepo.findOneById(fId);
	    Resource resource = filesService.loadFileAsResource(file);

	    String fileName = (file != null && file.getName() != null)
	            ? file.getName()
	            : "default.jpg";

	    String contentType = getContentType(request, resource);

	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(contentType))
	            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
	            .body(resource);
	}

	
	@GetMapping(value = "/unsecure/download/{fId}")
	public ResponseEntity<Resource> downloadFileResourceById(HttpServletRequest request, @PathVariable String fId)
	        throws IOException {
	    log.info("File Id {}", fId);

	    Files file = filesRepo.findOneById(fId);
	    Resource resource = filesService.loadFileAsResource(file);

	    String fileName = (file != null && file.getName() != null)
	            ? file.getName()
	            : "default.jpg";

	    String contentType = getContentType(request, resource);

	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(contentType))
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
	            .body(resource);
	}

	
	
	
	public String getContentType(HttpServletRequest request, Resource resource) {
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (contentType == null) {
			contentType = DEFAULT_CONTENT_TYPE;
		}
		return contentType;
	}

}
