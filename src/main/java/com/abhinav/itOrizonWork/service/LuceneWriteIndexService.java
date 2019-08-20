package com.abhinav.itOrizonWork.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.abhinav.itOrizonWork.exception.StorageException;

@Service
public class LuceneWriteIndexService
{
	// Directory for storing/accessing lucene index files
	// This value is stored and read from application.properties file
	@Value("${lucene.index.path}")
	private String INDEX_DIR;

	// Path of directory for storing the file to be uploaded
	// This value is stored and read from application.properties file

	// This directory has to be manually created or proper existing directory should 
	// be given in the application.properties file

	@Value("${upload.path}")
	private String filePath;

	public void writeIndex(String fileName) {
		IndexWriter writer;
		List<Document> documents = new ArrayList<>();

		try {
			// Create the index writer
			writer = createWriter();

			// Read the stored file uploaded from the file upload service
			BufferedReader tsvReader = new BufferedReader(new FileReader(filePath + fileName));

			// This code block reads each line from the uploaded file
			String row;
			while ((row = tsvReader.readLine()) != null) {
				// if the read row does not contain delimiter /m/, we need to ignore it
				if (!row.contains("/m/"))
					continue;

				// Split the read row based on our pre-defined delimiter
				String[] data = row.split("/m/");

				// Create lucene document (here id is at 1st position nd song name at 0th)
				Document document = createDocument(data[1], data[0]);
				documents.add(document);
			}

			// Close the file reader
			tsvReader.close();

			//Let's clean everything first
			writer.deleteAll();

			// Write the documents (index files) and close the writer
			writer.addDocuments(documents);
			writer.commit();
			writer.close();

		} catch (IOException e) {
			String msg = String.format("Failed to write index : " + e.getMessage());

			throw new StorageException(msg, e);
		}
	}

	private Document createDocument(String id, String songName) {
		Document document = new Document();

		// We store id as String field and the song name as text
		document.add(new StringField("id", id , Field.Store.YES));
		document.add(new TextField("songName", songName , Field.Store.YES));

		return document;
	}

	private IndexWriter createWriter() throws IOException {
		// Open the directory path given for the index storage
		// Creates a directory path if not present
		FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));

		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(dir, config);
		return writer;
	}
}