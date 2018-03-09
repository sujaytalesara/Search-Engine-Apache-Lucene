package information_retrival;

import java.io.File;

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
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
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

public class Indexer 
{
	 
	static String  indexPath ;
	static String  docsPath;
	static String  queryPath;
	
  private Indexer() {}

  
  public static void main(String[] args) 
  {
	  for(int i=0;i<args.length;i++)
	  {
	      if ("-indexPath".equals(args[i]))
	      {
	    	 
	        indexPath = args[i+1];
	        i++;
	      } 
	      else if ("-docs".equals(args[i]))
	      {
	        docsPath = args[i+1];
	        i++;
	      }
	      else if ("-query".equals(args[i]))
	      {
	    	  queryPath = args[i+1];
	        i++;
	      }
	  }
 
	  Indexer objIndexFiles = new Indexer();
	   
	  objIndexFiles.indexcall(indexPath,docsPath, queryPath);
  }

  public void indexcall(String indexPath,String docsPath,String queryPath) 
  {
	    
	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir))
	    {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    try
	    {
	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	     /* Analyzer analyzer = CustomAnalyzer.builder()
	    		  .withTokenizer(StandardTokenizerFactory.class)
	    		  .addTokenFilter(StandardFilterFactory.class)
	    		  .addTokenFilter(LowerCaseFilterFactory.class)
	    		  .addTokenFilter(PorterStemFilterFactory.class).build();
		*/
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	      iwc.setOpenMode(OpenMode.CREATE);
	      
	      IndexWriter writer = new IndexWriter(dir, iwc);
	      //System.out.println(docDir.toString());
	      indexDoc(writer, docDir.toString());
	      writer.close();

	    } 
	    catch (IOException e)
	    {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	    
	    Searcher objSearchFiles = new Searcher(indexPath,queryPath);
	    objSearchFiles.SearchFilesCall();
  }
    
  public static void indexDoc(IndexWriter writer, String file) throws IOException 
  {
	  try
	  {
	  Scanner scan = new Scanner(new File(file));
	  scan.useDelimiter(".I ");

	  while (scan.hasNext()) 
	  {
	      String content = scan.next();
	      
	      String[] result = content.split(".T|.A|.W|.B");   
	      // make a new, empty document
	      Document doc = new Document();
	    
	      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
	      doc.add(pathField);
	      doc.add(new StringField("DocNumber",result[0].trim(),Field.Store.YES));
    	  doc.add(new TextField("contents", result[4].trim().replaceAll("\n", ""), Field.Store.YES));
    	  
	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) 
	      {
	        writer.addDocument(doc);
	      } else {
	        writer.updateDocument(new Term("path", file.toString()), doc);
	      }
	  }
	  scan.close();
	  }
	  catch(Exception e){}
  	}
}

