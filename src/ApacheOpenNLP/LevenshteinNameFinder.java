package ApacheOpenNLP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import opennlp.tools.tokenize.TokenizerME; 
import opennlp.tools.tokenize.TokenizerModel;

public class LevenshteinNameFinder {
	public static void main(String args[]) throws Exception{     	   
		//ALGORITHM SETTINGS
		String comparisonType="match"; //options: match, normal_L, weighted_L, custom_L
		String custom_L_Type="right"; //options: left, right, up, down
		String ascendingWeights = "0.25,0.5,0.75";
		String descendingWeights = "0.75,0.5,0.25";
		int mainWeights = 1;
		int minMainWeightLength = 3;
		int maxLehvensteinCost = 1;
		
		//Convert Weights to integer array
		String[] parts = ascendingWeights.split(",");
		int[] aWeights = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			aWeights[i] = Integer.parseInt(parts[i]);
		}
		parts = descendingWeights.split(",");
		int[] dWeights = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			dWeights[i] = Integer.parseInt(parts[i]);
		}
		
		//OPTIMISATION SETTINGS
		int maxStrLenComDist = 3;
		boolean matchInitial = true;
		
	    //OUTPUT FILE SETTINGS
		//Create output file
	    Date date = new Date();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH-mm-ss");
	    String relativePath = "Documents\\output\\Tagged Names " + dateFormat.format(date) + ".txt";
	    File file = new File(relativePath);
	    if(file.createNewFile()){
	    	System.out.println(relativePath+" File Created in Project root directory");
	    }else System.out.println("File "+relativePath+" already exists in the project root directory");
	    //Customize output
	    String outputSpacing = " ";
	      
	    //LOAD TOKENIZER MODEL
	    InputStream inputStream = new FileInputStream("ApacheNLPmodels\\slo-token.bin"); 
	    TokenizerModel model = new TokenizerModel(inputStream); 
	      
	    //IMPORT FILES TO ARRAY
	    //Define input file locations
	    String fileEncoding = "UTF-8";
	    File inputFileDir = new File("Documents\\input\\testLeposlovje.txt");
	    File dictionaryDir = new File("Documents\\resources\\Levenshtein\\Names.txt");
	    File weightsReplaceDir = new File("Documents\\resources\\Levenshtein\\WeightsReplace.csv");
	    //Import input text
	    BufferedReader input_text = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileDir), fileEncoding));
	    String result = input_text.lines().collect(Collectors.joining("\n"));;
	    input_text.close();
	    //Import replace weight
	    BufferedReader input_replaceWeights = new BufferedReader(new InputStreamReader(new FileInputStream(weightsReplaceDir), fileEncoding));
	    String line = "";
	    String[] characterList = null;
	    int[][] replaceWeights = null;
	    int lineNumber = 0;
	    int j = 0;
	    while ((line = input_replaceWeights.readLine()) != null) {
	    	if (lineNumber==0) {
	    		characterList = line.split(",");
	    	}else {
	    		parts = line.split(",");
	    		replaceWeights = new int[parts.length][parts.length];
	    		for (int i = 0; i < parts.length; i++) {
	    			replaceWeights[j][i] = Integer.parseInt(parts[i]);
	    		}
	    		j = j + 1;
	    	}
	    }
	    input_replaceWeights.close();
	    //CONFIGURE TOKENIZER
	    //Instantiating the TokenizerME class 
	    TokenizerME tokenizer = new TokenizerME(model); 
	    //Tokenizing the given raw text 
	    String tokens[] = tokenizer.tokenize(result);      
	   //tokens -> array
	    String[] tokeni = tokens;
	    boolean isMatch;
	    boolean charMatch;
	    int wordCounter = 1;
	    int foundCounter = 0;
	      
	    //WRITE TO OUTPUT     
	    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Documents\\output\\Tagged Names " + dateFormat.format(date) + ".txt"), fileEncoding));
	    	for(int i=0;i<tokeni.length;i++) {
	    		isMatch=false;
	    		charMatch=true;
	    		BufferedReader input_dictionary = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryDir), fileEncoding));
	    		while( (line=input_dictionary.readLine()) != null )
	    		{
	    			if (Math.abs(tokeni[i].length() - line.length())>maxStrLenComDist) {
	    				if (matchInitial) {
	    					if (tokeni[i].charAt(0) != line.charAt(0)) {
	    						charMatch = false;
	    					}
	    				}
	    				if (charMatch) {
			    			if (comparisonType == "match") {
				    			if (tokeni[i] == line) {
				    				isMatch=true;
				    			}
			    			}
			    			else if(comparisonType == "normal_L"){
				    			if (normalLevenshtein(tokeni[i], line)<maxLehvensteinCost) {
				    				isMatch=true;
				    			}
			    			}
			    			else if(comparisonType == "weighted_L"){
				    			if (weightedLevenshtein(tokeni[i], line, characterList, replaceWeights)<maxLehvensteinCost) {
				    				isMatch=true;
				    			}
			    			}
			    			else if(comparisonType == "custom_L"){
				    			if (customLevenshtein(tokeni[i], line)<maxLehvensteinCost) {
				    				isMatch=true;
				    			}
			    			}
	    				}
	    			}
	    		}
	    		if (isMatch==true) {
		    			out.write("<START:Name>" + tokeni[i] + "<END>" + outputSpacing);
			      		foundCounter=foundCounter+1;
	    			} else { 
	        			out.write(tokeni[i] + outputSpacing);
	    			}
	    		out.flush();
	    		System.out.println(wordCounter + " / " + tokeni.length);
	    		wordCounter=wordCounter+1;
	    		input_dictionary.close();
	    	}
	    	out.close();
	    	System.out.println("Completed, found items: " + foundCounter);
		}
	
	public static int normalLevenshtein (CharSequence lhs, CharSequence rhs) {                          
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}
	
	public static int weightedLevenshtein (CharSequence lhs, CharSequence rhs, String[] charList, int[][] Weights) {   

	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings
	        	int lhsPos = Arrays.asList(charList).indexOf(String.valueOf(lhs.charAt(i - 1)));
	        	int rhsPos = Arrays.asList(charList).indexOf(String.valueOf(rhs.charAt(i - 1)));
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : Weights[lhsPos][rhsPos];
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}

	public static int customLevenshtein (CharSequence lhs, CharSequence rhs) {   

	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	                                                                                    
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1; //DODAJ LOOKUP ZA UTE� mno�en z menjavo namesto 1
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; cost = newcost; newcost = swap;                          
	    }                                                                               
	                                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1];                                                          
	}
}