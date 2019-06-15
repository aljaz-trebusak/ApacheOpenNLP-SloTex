package ApacheOpenNLP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderCrossValidator;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.eval.CrossValidationPartitioner;


public class CrossValidation {
	 private static class DocumentSample implements Serializable {

		    private NameSample[] samples;

		    DocumentSample(NameSample[] samples) {
		      this.samples = samples;
		    }

		    private NameSample[] getSamples() {
		      return samples;
		    }
		  }

		  /**
		   * Reads Name Samples to group them as a document based on the clear adaptive data flag.
		   */
		  private static class NameToDocumentSampleStream extends FilterObjectStream<NameSample, DocumentSample> {

		    private NameSample beginSample;

		    protected NameToDocumentSampleStream(ObjectStream<NameSample> samples) {
		      super(samples);
		    }

		    public DocumentSample read() throws IOException {

		      List<NameSample> document = new ArrayList<>();

		      if (beginSample == null) {
		        // Assume that the clear flag is set
		        beginSample = samples.read();
		      }

		      // Underlying stream is exhausted!
		      if (beginSample == null) {
		        return null;
		      }

		      document.add(beginSample);

		      NameSample sample;
		      while ((sample = samples.read()) != null) {

		        if (sample.isClearAdaptiveDataSet()) {
		          beginSample = sample;
		          break;
		        }

		        document.add(sample);
		      }

		      // Underlying stream is exhausted,
		      // next call must return null
		      if (sample == null) {
		        beginSample = null;
		      }

		      return new DocumentSample(document.toArray(new NameSample[document.size()]));
		    }

		    @Override
		    public void reset() throws IOException, UnsupportedOperationException {
		      super.reset();

		      beginSample = null;
		    }
		  }

		  /**
		   * Splits DocumentSample into NameSamples.
		   */
		  private static class DocumentToNameSampleStream extends FilterObjectStream<DocumentSample, NameSample> {

		    protected DocumentToNameSampleStream(ObjectStream<DocumentSample> samples) {
		      super(samples);
		    }

		    private Iterator<NameSample> documentSamples = Collections.<NameSample>emptyList().iterator();

		    public NameSample read() throws IOException {

		      // Note: Empty document samples should be skipped

		      if (documentSamples.hasNext()) {
		        return documentSamples.next();
		      }
		      else {
		        DocumentSample docSample = samples.read();

		        if (docSample != null) {
		          documentSamples = Arrays.asList(docSample.getSamples()).iterator();

		          return read();
		        }
		        else {
		          return null;
		        }
		      }
		    }
		  }

		  private String languageCode;
		  private TrainingParameters params;
		  private String type;
		  private byte[] featureGeneratorBytes;
		  private Map<String, Object> resources;
		  private TokenNameFinderEvaluationMonitor[] listeners;

		  private FMeasure fmeasure = new FMeasure();
		  private TokenNameFinderFactory factory;

		  /**
		   * Name finder cross validator
		   *
		   * @param languageCode
		   *          the language of the training data
		   * @param type
		   *          null or an override type for all types in the training data
		   * @param trainParams
		   *          machine learning train parameters
		   * @param featureGeneratorBytes
		   *          descriptor to configure the feature generation or null
		   * @param listeners
		   *          a list of listeners
		   * @param resources
		   *          the resources for the name finder or null if none
		 * @return 
		   */
		  public void TokenNameFinderCrossValidator(String languageCode, String type,
		      TrainingParameters trainParams, byte[] featureGeneratorBytes,
		      Map<String, Object> resources, SequenceCodec<String> codec,
		      TokenNameFinderEvaluationMonitor... listeners) {

		    this.languageCode = languageCode;
		    this.type = type;
		    this.featureGeneratorBytes = featureGeneratorBytes;
		    this.resources = resources;
		    this.params = trainParams;
		    this.listeners = listeners;
		  }

		  public CrossValidation(String languageCode, String type,
		      TrainingParameters trainParams, byte[] featureGeneratorBytes,
		      Map<String, Object> resources,
		      TokenNameFinderEvaluationMonitor... listeners) {
		    this(languageCode, type, trainParams, featureGeneratorBytes, resources, new BioCodec(), listeners);
		  }

		  public CrossValidation(String languageCode, String type,
		      TrainingParameters trainParams, TokenNameFinderFactory factory,
		      TokenNameFinderEvaluationMonitor... listeners) {
		    this.languageCode = languageCode;
		    this.type = type;
		    this.params = trainParams;
		    this.factory = factory;
		    this.listeners = listeners;
		  }

		  public CrossValidation(String languageCode2, String type2, TrainingParameters trainParams,
				byte[] featureGeneratorBytes2, Map<String, Object> resources2, BioCodec bioCodec,
				TokenNameFinderEvaluationMonitor[] listeners2) {
			// TODO Auto-generated constructor stub
		}

		/**
		   * Starts the evaluation.
		   *
		   * @param samples
		   *          the data to train and test
		   * @param nFolds
		   *          number of folds
		   * @throws IOException
		   */
		  public void evaluate(ObjectStream<NameSample> samples, int nFolds)
		      throws IOException {

		    // Note: The name samples need to be grouped on a document basis.

		    CrossValidationPartitioner<DocumentSample> partitioner = new CrossValidationPartitioner<>(
		        new NameToDocumentSampleStream(samples), nFolds);

		    while (partitioner.hasNext()) {

		      CrossValidationPartitioner.TrainingSampleStream<DocumentSample> trainingSampleStream =
		          partitioner.next();

		      TokenNameFinderModel model;
		      if (factory != null) {
		        model = NameFinderME.train(languageCode, type, new DocumentToNameSampleStream(trainingSampleStream),
		            params, factory);
		      }
		      else {
		        model = NameFinderME.train(languageCode, type, new DocumentToNameSampleStream(trainingSampleStream),
		            params, TokenNameFinderFactory.create(null, featureGeneratorBytes, resources, new BioCodec()));
		      }

		      // do testing
		      TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(
		          new NameFinderME(model), listeners);

		      evaluator.evaluate(new DocumentToNameSampleStream(trainingSampleStream.getTestSampleStream()));

		      fmeasure.mergeInto(evaluator.getFMeasure());
		    }
		  }

		  public FMeasure getFMeasure() {
		    return fmeasure;		    
		  } 
		  
}
