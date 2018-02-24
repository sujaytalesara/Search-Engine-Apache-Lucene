
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
package lucene_information_retrival;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
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
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchFiles 
{
	private static String index = "/lucene/cran/index";
	private static String queryLocation = "/lucene//cran.qry";
	private String cranrel = "/lucene/cran/cranqrel";
	
	public IndexSearcher searcher;
	private Analyzer analyzer;
	public IndexReader reader;
	private BufferedReader br;
	private static String field = "contents";
	private static int hitsPerPage = 1000;
	private float Precision = 0;
	private float Recall = 0;
	private TopDocs resultsVectorSpace;
	private TopDocs resultsBM25;
	private ScoreDoc[] hitsVectorSpace;
	private ScoreDoc[] hitsBM25;
	HashMap<Integer,String> queryMap = new HashMap<Integer,String>(); 
	HashMap<String,String> cranrelMap = new HashMap<String,String>();
	String[] result;
	
  
  public static void SearchFilesCall() 
  {
	  
	  SearchFiles objSearchFiles = new SearchFiles();
	  try 
	  {
		  objSearchFiles.indexSearcher(index,queryLocation,field,hitsPerPage);

	  }
	  catch(Exception e)
	  {}
  }
  
  
  public void indexSearcher(String index, String queryLocation,String field, int hitsPerPage) throws Exception
  {	     
	    final Path queryPath = Paths.get(queryLocation);
	    reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    searcher = new IndexSearcher(reader);	    
	    try 
	    {
	    	analyzer = CustomAnalyzer.builder().withTokenizer(StandardTokenizerFactory.class).addTokenFilter(StandardFilterFactory.class).addTokenFilter(LowerCaseFilterFactory.class).addTokenFilter(PorterStemFilterFactory.class).build();
		}
	    catch (Exception e) 
	    {
			e.printStackTrace();
		}
	    QueryParser parser = new QueryParser(field, analyzer);
	    QueryRead(queryPath,parser);
	    reader.close();
  }
  
  public void QueryRead(Path queryPath,QueryParser parser)
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
	    readCranRel();
	    vectorSpaceCalculation(parser);
	    searchQueryBM25(parser);
	  }
	  catch(Exception e) 	
	  {
		  System.out.println(e);
	  }

  }
  
  public void vectorSpaceCalculation( QueryParser parser) throws Exception
  {
	  PrintWriter writer = new PrintWriter("vectorSpaceResult.txt", "UTF-8");
	  for(int queryID:queryMap.keySet())
	  {
		  String singleQuery  = queryMap.get(queryID);
		  Query query = parser.parse(singleQuery);	
		 // String queryID = queryMap.containsValue(query);
		  resultsVectorSpace = searcher.search(query, hitsPerPage);
		  hitsVectorSpace = resultsVectorSpace.scoreDocs; // VSP
		  calculateTrecResult(writer,searcher,hitsVectorSpace,queryID);

	  }
	  writer.close();
	  //calculatePrecisionRecall(resultsVectorSpace,hitsVectorSpace);
	  
  }
     
  public void searchQueryBM25(QueryParser parser) throws Exception
  {
	  PrintWriter writer = new PrintWriter("BM25Result.txt", "UTF-8");

	  for(int queryID:queryMap.keySet())
	  {
		  String singleQuery  = queryMap.get(queryID);
		  Query query = parser.parse(singleQuery);	
		  searcher.setSimilarity(new BM25Similarity());
		  resultsBM25 = searcher.search(query, hitsPerPage);
		  hitsBM25 = resultsBM25.scoreDocs; // BM25
		  calculateTrecResult(writer,searcher,hitsBM25,queryID);
	  }
	}
  
  public void calculateTrecResult(PrintWriter writer, IndexSearcher searcher, ScoreDoc[] Scoredoc,int queryID) throws Exception
  {
	  int counter = 0;
	  for(ScoreDoc hit : Scoredoc)
	  {
		  int doc = hit.doc;
		  counter++;
		  Document doct = searcher.doc(doc);
		  writer.println(queryID + " 0 " + doct.get("DocNumber") + " " + counter +" "+ hit.score +" " + "exp_0");
	  }
	  
  }
  
  public void readCranRel() throws Exception 
  {
 
    br = new BufferedReader(new FileReader(cranrel));
    PrintWriter writer = new PrintWriter("CranRelNew", "UTF-8");
    writer.print("");
     try {        
        String line = br.readLine();
        
        
        while(line != null)
        {
        	String[] result =  line.split(" ");
        	if(result[2].equals("1") ||result[2].equals("2")||result[2].equals("3"))
        	{   
        		cranrelMap.put(result[0], result[1]);
        	}
        	writer.println(result[0] +" 0 "+result[1]+" "+result[2]);
        	System.out.println(result[0] + " " + result[1] +" "+ result[2]);
        	line = br.readLine();
        }
    } 
    finally
    {
        br.close();
        writer.close();
    }
  }
    
  public void calculatePrecisionRecall(TopDocs results,ScoreDoc[] hits)
  {
		 int Scores = 0;
		 int recallCount = 0;

	 // new function
	 for(int i=0;i<hits.length;i++) //Hits
	 {
	    //System.out.println(hits[i]);
	    String hitdocs = Integer.toString(hits[i].doc);
	    
	    if(cranrelMap.values().contains(hitdocs))
	    {
	    	Scores++;
	    	
	    }
	    else
	    {
	    	recallCount++;
	    }
	   }
	    // Precision *********************
	    float x = ((float)Scores/hits.length);
	    Precision = Precision + x;
	    //System.out.println(hits.length);
	   //System.out.println(Scores +"**********************"+ Precision);
	    
	    float y = ((float)Scores/recallCount);
	    Recall = Recall + y;
	   //System.out.println(recallCount +"**********************"+ Recall);
  }

	
}
