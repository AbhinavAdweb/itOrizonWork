package com.abhinav.itOrizonWork.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abhinav.itOrizonWork.exception.StorageException;
import com.abhinav.itOrizonWork.model.Song;

@Service
public class LuceneReadIndexService
{
	// Directory for storing/accessing lucene index files
	// This value is stored and read from application.properties file
	@Value("${lucene.index.path}")
	private String INDEX_DIR;
	
	// Total results of the search
	public int totalResults = 0;

	// Method to return a list of songs (Song POJO) for the given search term and page number
	public List<Song> songSearch(String searchTerm, int pageNum) {
		List<Song> searchResults = new ArrayList<Song>();
		
		IndexSearcher searcher;
		try {
			
			// Create the searcher for the stored index files
			searcher = createSearcher();
			
			// find the songs by name
			TopDocs foundDocs = searchBySongName(searchTerm, searcher, pageNum);
			
			// Total hits by the searcher will be our total number of results
			totalResults = foundDocs.totalHits;
			
			// Create a list of Song objects from the result of the search
			for (ScoreDoc sd : foundDocs.scoreDocs) {
				Document d = searcher.doc(sd.doc);
				Song song = new Song(d.get("id"), d.get("songName"));
				searchResults.add(song);
			}
			
			return searchResults;
			
		} catch (IOException e) {
			String msg = String.format("Failed to find stored index file : " + e.getMessage());

            throw new StorageException(msg, e);
            
		} catch (Exception e) {
			String msg = String.format("Failed to search : " + e.getMessage());

            throw new StorageException(msg, e);
		}
	}

	private TopDocs searchBySongName(String songName, IndexSearcher searcher, int page) throws Exception {
		// Create a simple query parser for the field "songName" that we stored in the indexer
		QueryParser qp = new QueryParser("songName", new StandardAnalyzer());
		
		// Create a collector class to collect top 10 results starting from the given page
		TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
		
		// Calculate 0 based start index in the multiples of 10
		// 10 represents the number of results we want per page
		int startIndex = (page - 1) * 10;
		
		// Create the query for the given search term or song name
		Query songNameQuery = qp.parse(songName);
		
		// search the song and return the top 10 hits for the current page
		searcher.search(songNameQuery, collector);
		TopDocs hits = collector.topDocs(startIndex, 10);
		return hits;
	}

	private IndexSearcher createSearcher() throws IOException {
		// Open the index directory to create the index searcher
		
		// If index files not found at the directory, this will throw an exception 
		// that will be caught by our exception handler
		
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
}