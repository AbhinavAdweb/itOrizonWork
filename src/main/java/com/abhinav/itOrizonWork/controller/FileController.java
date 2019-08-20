package com.abhinav.itOrizonWork.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.abhinav.itOrizonWork.exception.StorageException;
import com.abhinav.itOrizonWork.model.Song;
import com.abhinav.itOrizonWork.service.LuceneReadIndexService;
import com.abhinav.itOrizonWork.service.LuceneWriteIndexService;
import com.abhinav.itOrizonWork.service.StorageService;

// This controller will communicate with our file upload and search UI
@Controller
public class FileController {

	// This service lets you upload and store a tsv/csv file for songs
	@Autowired
	private StorageService storageService;

	// This service is run after a file has been uploaded to generate lucene indexes
	@Autowired
	private LuceneWriteIndexService luceneWriteIndexService;

	// This service will provide ways to search songs based on the stored lucene indexes
	@Autowired
	private LuceneReadIndexService luceneReadIndexService;

	// This post request is to upload and store a multipart file (tsv/csv file in our case)
	@RequestMapping(value = "/doUpload", method = RequestMethod.POST, consumes = {"multipart/form-data"})
	public String upload(@RequestParam MultipartFile file, Model model) {

		String fileName = storageService.uploadFile(file);

		// Successful file uploaded will return a file name to be used for indexing the file
		luceneWriteIndexService.writeIndex(fileName);
		
		// Initially when search page is loaded, not found error should not be shown
		// since search page requires search results and if null shows not found message
		model.addAttribute("shouldShowNotFound", false);
		
		// Load the search template after successful file upload and index generation
		return "search.html";
	}

	// This GET method takes search term and page num and returns the searched songs from lucene
	@RequestMapping(value = "/doSearch", method = RequestMethod.GET)
	public String search(@RequestParam String searchTerm, @RequestParam int pageNum, Model model) {

		if (searchTerm == null || searchTerm.isEmpty()) {
			model.addAttribute("error", "Search field should not be empty");
			return "failure.html";
		}
		
		// Read service will return a list of Songs (Song model class) for..
		// given search term and the page number
		List<Song> searchResults = luceneReadIndexService.songSearch(searchTerm, pageNum);

		// not found message should be shown when this get request is fired and no results returned
		model.addAttribute("shouldShowNotFound", true);

		// Only return results when read service returns a non null/non empty value
		if (searchResults != null && !searchResults.isEmpty())
			model.addAttribute("searchResults", searchResults);
		
		// Get the total number of results to calculate number of pages (10 results/page) required
		int totalResults = luceneReadIndexService.totalResults;
		
		Double totalPages = 1D;
		
		// Total pages which will display max 10 results per page
		if (totalResults > 1)
			totalPages = Math.ceil(totalResults/ 10D);
		
		// Thymeleaf attributes to generate proper view
		model.addAttribute("totalResults", totalResults);
		model.addAttribute("totalPages", totalPages.intValue());
		model.addAttribute("searchTerm", searchTerm);
		model.addAttribute("pageNum", pageNum);
		
		return "search.html";
	}

	// Load the search page directly when called in case a file has been uploaded and index generated previously
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String search(Model model) {
		model.addAttribute("shouldShowNotFound", false);
		return "search.html";
	}

	// Handles our custom exceptions to generate failure message page
	@ExceptionHandler(StorageException.class)
	public String handleStorageFileNotFound(StorageException e, Model model) {
		model.addAttribute("error", e.getLocalizedMessage());
		return "failure.html";
	}
}
