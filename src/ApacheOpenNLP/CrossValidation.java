package ApacheOpenNLP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderCrossValidator;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;

public class CrossValidation {
	 public static void main(String[] args) throws IOException {
			
			//Loading the model 
		    InputStream inputStream = new FileInputStream("ApacheNLPmodels\\ner-model.bin"); 
		    TokenNameFinderModel model = new TokenNameFinderModel(inputStream); 
			
		    //Instantiating the NameFinderME class 
		    NameFinderME nameFinder = new NameFinderME(model);
		    
			//import model
			//TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder);
			
			
			//data used to teach the model
			
			//FileInputStream sampleDataIn = new FileInputStream("Documents\\resources\\NamedEntityRecognition\\TaggedNames.txt");
			
			 InputStreamFactory in = null;
		     try {
		         in = new MarkableFileInputStreamFactory(new File("Documents\\resources\\NamedEntityRecognition\\TaggedNames.txt"));
		     } catch (FileNotFoundException e2) {
		         e2.printStackTrace();
		     }
		     
		     ObjectStream<NameSample> sampleStream = null;
		     try {
		         sampleStream = new NameSampleDataStream(
		             new PlainTextByLineStream(in, StandardCharsets.UTF_8));
		     } catch (IOException e1) {
		         e1.printStackTrace();
		     }
			
			//ObjectStream<NameSample> sampleStream = new PlainTextByLineStream(sampleDataIn.getChannel(), StandardCharsets.UTF_8);
			
//			evaluator.evaluate(sampleStream);
//
//			FMeasure result = evaluator.getFMeasure();
//
//			System.out.println(result.toString());
			
		    TrainingParameters params = new TrainingParameters();
		    params.put(TrainingParameters.ITERATIONS_PARAM, 70);
		    params.put(TrainingParameters.CUTOFF_PARAM, 1);
			
		    TokenNameFinderEvaluationMonitor listeners;
			TokenNameFinderCrossValidator evaluator = new TokenNameFinderCrossValidator("en", null, params, null, listeners, null);
			evaluator.evaluate(sampleStream, 10);
		
			FMeasure result = evaluator.getFMeasure();
		
			System.out.println(result.toString());
			}
}
