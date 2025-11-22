package com.furtim.entitleguard.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import com.furtim.entitleguard.configuration.FileStorageConfiguration;
import com.furtim.entitleguard.entity.Files;
import com.furtim.entitleguard.repository.FilesRepository;
import com.furtim.entitleguard.utils.FilesType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilesService {

	final Path fileStorageLocation;
	
	@Autowired
	FilesRepository filesRepo;

	
	public FilesService(FileStorageConfiguration fileStorageConfiguration) {
		this.fileStorageLocation = Paths.get(fileStorageConfiguration.getUploadDir()).toAbsolutePath().normalize();

		try {
			java.nio.file.Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public Files saveFile(Files newFile) {
		return filesRepo.save(newFile);
	}
	
	public Path storeFile(MultipartFile file, String referenceId) {
		log.info("Store file");

		Path targetLocation = null;

		String fileName = file.getOriginalFilename();

		try {
			if (fileName != null) {
				if (fileName.contains("..")) {
					log.info("Sorry! Filename contains invalid path sequence {}", fileName);
				}

				if (fileName.contains(".")) {
					String[] str = fileName.split("\\.");
					String newFileName = str[0] + "_" + referenceId + "." + str[1];

					targetLocation = this.fileStorageLocation.resolve(newFileName);

					java.nio.file.Files.copy(file.getInputStream(), targetLocation,
							StandardCopyOption.REPLACE_EXISTING);
					return targetLocation;
				}

				String s = fileName + referenceId;
				targetLocation = this.fileStorageLocation.resolve(s);

				java.nio.file.Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			}

			return targetLocation;

		} catch (IOException ex) {
			ex.printStackTrace();
			return targetLocation;
		}
	}
	
	public Files uploadFile(MultipartFile file, String referenceId, FilesType type, String updatedBy) {
		log.info("FILES POST");

		Path path = storeFile(file, referenceId);

		String fileName = file.getOriginalFilename();

		Files newFile = new Files();

		newFile.setType(type.toString());
		newFile.setFileType(file.getContentType());
		newFile.setFilePath(path.toString());
		newFile = saveFile(newFile);
		String newFileName = getOriginalFileName(fileName, newFile.getId());
		newFile.setName(newFileName);

		return saveFile(newFile);

	}

	public String getOriginalFileName(String fileName, String referenceId) {

		if (fileName != null) {
			if (fileName.contains(".")) {
				String[] nameSplit = fileName.split("\\.");
				return nameSplit[0] + "_" + referenceId + "." + nameSplit[1];
			}
		}

		return fileName + referenceId;
	}

	public void updateFile(String imageId, MultipartFile file) throws IOException {
		Files source = getOneById(imageId);

		String fileName = file.getOriginalFilename();
		String referenceId = source.getId();
		String newFileName;
		String originalFileName = getOriginalFileName(fileName, referenceId);

		Path path = Paths.get(source.getFilePath()).getParent();
		Path fileLocation = null;

		if (fileName != null) {
			if (fileName.contains("..")) {
				log.info("Sorry! Filename contains invalid path sequence {}", fileName);
			}

			if (fileName.contains(".")) {
				String[] str = fileName.split("\\.");
				newFileName = str[0] + "_" + referenceId + "." + str[1];

			} else {
				newFileName = fileName + referenceId;

			}
			fileLocation = path.resolve(newFileName);
			java.nio.file.Files.copy(file.getInputStream(), fileLocation, StandardCopyOption.REPLACE_EXISTING);
		}

		if (fileLocation != null)
			source.setFilePath(fileLocation.toString());

		source.setName(originalFileName);
		source.setFileType(file.getContentType());

		saveFile(source);

	}

	public Files getOneById(String id) {
		return filesRepo.findOneById(id);
	}

	public Resource loadFileAsResource(Files file) throws FileNotFoundException, MalformedURLException {

		Resource resource;
		if (file != null && file.getFilePath() != null) {
			Path filePath = this.fileStorageLocation.resolve(file.getFilePath()).normalize();
			resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			}
		}

		return defaultImage();

	}

	public Resource defaultImage() throws FileNotFoundException, MalformedURLException {
		Resource resource;

		File file = ResourceUtils.getFile("classpath:default.jpg");
		resource = new UrlResource(file.toURI());

		return resource;
	}


}
