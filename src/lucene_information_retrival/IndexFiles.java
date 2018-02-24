package lucene_information_retrival;


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexFiles 
{
	SearchFiles objSearchFiles = new SearchFiles();
 
  private IndexFiles() {}

  
  public static void main(String[] args) 
  {
    
    String indexPath = "/home/sujay/information_retrival/cran/index"; // Location where index files will be saved
    String docsPath = "/home/sujay/information_retrival/cran/data"; // data location
    
    boolean create = true;
   
    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir))
    {
      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    
    Date start = new Date();
    try {
      System.out.println("Indexing to directory '" + indexPath + "'...");

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new StandardAnalyzer(); // Declaring Analyzer 
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      IndexWriter writer = new IndexWriter(dir, iwc);
      indexDocs(writer, docDir);
      writer.close();

      Date end = new Date();
      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

    } catch (IOException e)
    {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
    SearchFiles objSearchFiles = new SearchFiles();
    objSearchFiles.SearchFilesCall();
  }

  
  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	          try {

	            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	          } 
	          catch (IOException ignore) {
	            // don't index files that can't be read.
	          }
	          return FileVisitResult.CONTINUE;
	        }
	      });
	    } 
	    else {
	      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	    }
	}
  

  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException
 {
    try (InputStream stream = Files.newInputStream(file))
    {
      // make a new, empty document
      
      Scanner sc = new Scanner(file); //sc.useDelimiter(Pattern.compile(".I");
      sc.useDelimiter(".I ");

      
      while(sc.hasNext())
      {
    	  Document doc = new Document();
    	  String content = sc.next();
    	  Field pathField = new StringField("path", file.toString(), Field.Store.YES);
    	  
    	  String[] result = content.split(".A|.T|.B|.W");
    	  doc.add(pathField);
    	  //result[0] = result[0];
    	  //System.out.print( result[0] + "**" + result[0].length());
    	  //int a = result[0].length();
    	  
    	  
    	  doc.add(new TextField("DocNumber",result[0].trim(), Field.Store.YES));
    	  doc.add(new TextField("title", result[1],Field.Store.YES));
    	  doc.add(new TextField("Author", result[2], Field.Store.YES));
    	  doc.add(new TextField("bib", result[3], Field.Store.YES));
    	  doc.add(new TextField("contents", result[4], Field.Store.YES));
    	  
    	  if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
    	  {	
    		  // New index, so we just add the document (no old document can be there):
    		  System.out.println("adding " + result[1]);
    		  writer.addDocument(doc);
    	  }
    	  else
    	  {
    		  // Existing index (an old copy of this document may have been indexed) so 
    		  // we use updateDocument instead to replace the old one matching the exact 
    		  // path, if present:
    		  System.out.println("updating " + file);
    		  writer.updateDocument(new Term("path", file.toString()), doc);
    	  }
      }
      sc.close();
    }
    
  }

}
