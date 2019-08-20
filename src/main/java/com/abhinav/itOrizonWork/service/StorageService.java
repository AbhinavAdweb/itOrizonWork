package com.abhinav.itOrizonWork.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.abhinav.itOrizonWork.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

	// Path of directory for storing the file to be uploaded
	// This value is stored and read from application.properties file

	// This directory has to be manually created or proper existing directory should 
	// be given in the application.properties file
	
	@Value("${upload.path}")
	private String path;

	public String uploadFile(MultipartFile file) {

		if (file.isEmpty()) {
			throw new StorageException("Failed to store empty file");
		}

		try {
			String fileName = file.getOriginalFilename();

			// Only store the file if it is a CSV or TSV file
			if (fileName.endsWith(".csv") || fileName.endsWith(".tsv")) {


				InputStream is = file.getInputStream();

				// Replaces/stores the CSV/TSV file in the given directory
				Files.copy(is, Paths.get(path + fileName), StandardCopyOption.REPLACE_EXISTING);

				return fileName;
			}

			// Exception thrown when the uploaded file is not a CSV or TSV file
			throw new StorageException("File uploaded should only have a CSV or TSV extension");

		} catch (IOException e) {

			String msg = String.format("Failed to store file : " + e.getMessage() + ". Check if the directory exists!", file.getName());

			throw new StorageException(msg, e);
		}

	}
}