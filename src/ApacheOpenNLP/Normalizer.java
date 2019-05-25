package ApacheOpenNLP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class Normalizer {
	public static void main(String args[]) throws Exception{     		
   
	//Create output file
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH-mm-ss");
        
    //Define input files and dictionaries
    String fileEncoding = "UTF-8";
    File inputFileDir = new File("Documents\\input\\Leposlovje.txt");
    File dictionaryDir = new File("Documents\\resources\\Normalizer\\abbreviations.txt");

    //Import input files
    BufferedReader input_text = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileDir), fileEncoding));
    String result = input_text.lines().collect(Collectors.joining("\n"));
    input_text.close();
    
  //replace accent symbols & double punctuation...
    result = result.replaceAll("[ÁÀ]","A")
  		  .replaceAll("[áà]","a")
  		  .replaceAll("[ÉÊÈ]","E")
  		  .replaceAll("[éêè]","e")
  		  .replaceAll("[ÍÌ]","I")
  		  .replaceAll("[íì]","i")
  		  .replaceAll("[ÓÔÒ]","O")
  		  .replaceAll("[óôò]","o")
  		  .replaceAll("[ÚÙ]","U")
  		  .replaceAll("[úù]","u")
  		  .replaceAll("[?][!]","?")
  		  .replaceAll("[!][?]","!")
  		  .replaceAll("[-][-]","");
    	   
    //remove irregular symbols except...
    System.out.println("Removing irregular symbols...");
    for(int k=1;k<result.length();k++) {	    	 
  	  if(result.substring(k,k+1).matches("[\\W_]") && result.substring(k,k+1).matches("[^ČŠŽčšž .,?!-]") ) {
  		  result=result.substring(0,k) + " " + result.substring(k+1);
  		System.out.println(k+"/"+inputFileDir.length());
  	  }
    }
            
    //remove punctuation from abbreviations
    BufferedReader input_dictionary = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryDir), fileEncoding));
    
    String abbreviations = input_dictionary.lines().collect(Collectors.joining("\n"));
    String abbreviationsES = abbreviations.replaceAll("\\.","\\\\\\.");		//Escape Sequence . --> \.
    String abbreviationsWDB = abbreviations.replaceAll("[\\.\\\\]","");		//Without Dots and Backslashes
    String [] line_arr = abbreviations.split("\n");
    String [] lineES_arr = abbreviationsES.split("\n");
    String [] lineWDB_arr = abbreviationsWDB.split("\n");
    
    int st1=0;
    int st2=0;
    System.out.println("Removing punctuation...");
    for(int i=0;i<line_arr.length;i++) {    
    	st1=st1+1;
    	System.out.println(st1+"/"+line_arr.length);
    	if(result.contains(" "+line_arr[i]+" ") && line_arr[i].matches("^(?!.*(itd.|ipd.|itn.|etc.)).*$")) {    		
    		result = result.replaceAll(" "+lineES_arr[i]+" " , " "+lineWDB_arr[i]+" ");
    		st2=st2+1;
    	}
    }

	input_dictionary.close();
   	
    //Writing to output file	
    Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Documents\\output\\Normalized " + dateFormat.format(date) + ".txt"), fileEncoding));	
    out.write(result);
    out.flush();
	out.close();
	System.out.println(st2+" different abbreviation instances changed.");
	System.out.println("Normalized!");
	}
}
