package ApacheOpenNLP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream; 
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import opennlp.tools.namefind.NameFinderME; 
import opennlp.tools.namefind.TokenNameFinderModel; 
import opennlp.tools.util.Span;  

public class NamedEntityRecognizer { 
   public static void main(String args[]) throws Exception{ 
      InputStream inputStream = new FileInputStream("ApacheNLPmodels/en-ner-person.bin"); 
      TokenNameFinderModel model = new TokenNameFinderModel(inputStream);
      
      //Instantiating the NameFinder class 
      NameFinderME nameFinder = new NameFinderME(model); 
    
      //Getting the sentence in the form of String array
	  //IMPORT FILES TO ARRAY
	  //Define input file locations
	  String fileEncoding = "UTF-8";
	  File inputFileDir = new File("Documents\\input\\testLeposlovje.txt");
	  //Import input text
	  BufferedReader input_text = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileDir), fileEncoding));
	  String inputLine = input_text.lines().collect(Collectors.joining(" "));
	  String editedLine1 = inputLine.replaceAll("\\s","_");
	  String editedLine2 = editedLine1.replaceAll("[\\W]","");
	  String editedLine3 = editedLine2.replaceAll("_"," ");
      String [] inputArray = editedLine3.split(" ");
      input_text.close();
      
      System.out.println(Arrays.toString(inputArray));
      //Finding the names in the sentence 
      Span nameSpans[] = nameFinder.find(inputArray);
       
      //Printing the spans of the names in the sentence 
      for(Span s: nameSpans) 
         System.out.println(s.toString());
   }    
}