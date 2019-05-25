package ApacheOpenNLP;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;
import opennlp.tools.util.StringList;

public class dict {

	 private static String buildCorpusDict(String dictionaryDir, String sentence) throws IOException
	    {
	        logger.info("Executing NameFinder method .buildCorpusDict");
	        Dictionary dictionary = new Dictionary();

	        try (Stream<String> stream = Files.lines(Paths.get(dictionaryDir + "/lexicons_stat.si_moska_imena.list")))
	        {
	            stream.forEach(line -> dictionary.put(new StringList(line)));
	        }

	        TokenNameFinder dictionaryNER = new DictionaryNameFinder(dictionary, "person");

	        String[] tokens = TokenTask.tokenize(sentence);

	        Span nerSpans[] = dictionaryNER.find(tokens);

	        return buildSentence(nerSpans, tokens, sentence);
	    }
	
}
