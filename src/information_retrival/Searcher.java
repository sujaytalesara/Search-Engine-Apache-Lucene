
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
package information_retrival;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class Searcher 
{
	private static String index ;
	private static String queryLocation;

	private Analyzer analyzer = null;
	public IndexReader reader;
	private static String field = "contents";
	private static int hitsPerPage = 1000;
	private TopDocs resultsVectorSpace;
	private TopDocs resultsBM25;
	private ScoreDoc[] hitsVectorSpace;
	private ScoreDoc[] hitsBM25;
	HashMap<Integer,String> queryMap = new HashMap<Integer,String>(); 
	HashMap<String,String> cranrelMap = new HashMap<String,String>();
	String[] result;
	

  public Searcher(String indexPath, String queryPath) {
		// TODO Auto-generated constructor stub
	  this.index= indexPath;
	  this.queryLocation = queryPath;
	}

public static void SearchFilesCall() 
  {
  
	  Searcher objSearchFiles = new Searcher(index,queryLocation);
	  try 
	  {
		  objSearchFiles.indexSearcher(index,queryLocation,field,hitsPerPage);
		  System.out.println("Execution Completed");

	  }
	  catch(Exception e)
	  {
		  System.out.println(e);
		  e.printStackTrace();
	  }
  }
  


  public void indexSearcher(String index, String queryLocation,String field, int hitsPerPage) throws Exception
  {	     
	    final Path queryPath = Paths.get(queryLocation);
	    reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);	
	    
	    try 
	    {
	    /*	analyzer = CustomAnalyzer.builder()
		    		  .withTokenizer(StandardTokenizerFactory.class)
		    		  .addTokenFilter(StandardFilterFactory.class)
		    		  .addTokenFilter(LowerCaseFilterFactory.class)
		    		  .addTokenFilter(PorterStemFilterFactory.class).build();
		 */
	    	analyzer = new StandardAnalyzer(); // Standard Analyzer

	    	}
	    catch (Exception e) 
	    {
			e.printStackTrace();
		}
	    QueryParser parser = new QueryParser(field, analyzer);
	    QueryRead(queryPath,parser,searcher);
	    reader.close();
  }
  
  
  
  public void QueryRead(Path queryPath,QueryParser parser,IndexSearcher searcher)
  {
	  
	  try 
	  {
	  Scanner sc = new Scanner(queryPath);
	    sc.useDelimiter(".I ");
	    while(sc.hasNext())
	    {
	    	String content = sc.next(); 
	    	int length = content.length() - 1;
	    	int id = Integer.parseInt(content.substring(0,3));
	    	String queryValue = content.substring(8,length);
	    	queryValue = queryValue.replace("?", " ");
	    	queryMap.put(id, queryValue);
	      }
	    sc.close();
	    //readCranRel();
	    vectorSpaceCalculation(parser,searcher);
	    searchQueryBM25(parser,searcher);
	    sc.close();
	  }
	  catch(Exception e) 	
	  {
		  System.out.println(e);
		  e.printStackTrace();
	  }
	  

  }
  
  
  
  public void vectorSpaceCalculation( QueryParser parser,IndexSearcher searcher) throws Exception
  {
	  int Count = 0;
	  try
	  {
	  BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File("ResultVector")));
	  for(int queryID:queryMap.keySet())
	  {
		  Count++;
		  String singleQuery  = queryMap.get(queryID);
		  singleQuery = singleQuery.replaceAll("\n", "");
		  Query query = parser.parse(singleQuery);	
		  resultsVectorSpace = searcher.search(query, hitsPerPage);
		  hitsVectorSpace = resultsVectorSpace.scoreDocs; // VSP
		  
		  calculateTrecResult(writer1,searcher,hitsVectorSpace,Count);
		  
		  }
	  writer1.close();
	  System.out.println("ResultVector for Vector Space Model Created ---------");
	  }
	  catch(Exception e)
	  {
		  System.out.println(e);
		  e.printStackTrace();
	  }
	  
  }
     
  
  
  public void searchQueryBM25(QueryParser parser,IndexSearcher searcher) throws Exception
  {
	  try
	  { 

	  BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("BM25Result")));
	  int Count = 0;
	  for(int queryID:queryMap.keySet())
	  {
		  Count ++;
		  String singleQuery  = queryMap.get(queryID);
		  Query query = parser.parse(singleQuery);	
		  Similarity similarity[] = {
	                new BM25Similarity(2, (float) 0.89),
	                new DFRSimilarity(new BasicModelIn(), new AfterEffectB(), new NormalizationH1()),
	                new LMDirichletSimilarity(1500)
	        };
		  searcher.setSimilarity(new MultiSimilarity(similarity));
		  resultsBM25 = searcher.search(query, hitsPerPage);
		  hitsBM25 = resultsBM25.scoreDocs; // BM25
		 calculateTrecResult(writer2,searcher,hitsBM25,Count);
		 
	  }
	  writer2.close();
	  System.out.println("BM25Result for BM25 Model Created ---------");
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  }
	}
  
  
  
  public void calculateTrecResult(BufferedWriter writer, IndexSearcher searcher, ScoreDoc[] Scoredoc,int queryID) throws Exception
  {
	  int counter = 1;
	  for(ScoreDoc hit : Scoredoc)
	  {
		Document hitDoc = searcher.doc(hit.doc);
		String docId = hitDoc.get("DocNumber");
		
		writer.write(queryID + " 0 " + docId + " " + counter +" "+ hit.score +" " + "exp_0\n");
		counter++;
	  }
	  
  }	
}
