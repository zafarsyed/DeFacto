/**
 * 
 */
package org.aksw.defacto.search.time;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.apache.commons.lang3.StringUtils;

import com.github.gerbsen.math.Frequency;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class TimePeriodSearcher {

	public static Set<Pattern> timePatterns = new LinkedHashSet<>();
	public static Frequency patFreq = new Frequency();
	
	static {
		
		timePatterns.add(Pattern.compile("-LRB-\\s*[0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}\\s*-RRB-"));
		timePatterns.add(Pattern.compile("\\(\\s*[0-9]{4}\\s*(-|--|–)}\\s*[0-9]{4}\\s*\\)"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(/|-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4}\\s*(/|-|--|–)\\s*[0-9]{1,2}\\s*[A-z]*\\s*[0-9]{4}")); // - 20 November 
		
		timePatterns.add(Pattern.compile("[bB]etween [0-9]{4} and [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} to [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} to [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} through [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4} until [0-9]{4}"));
		timePatterns.add(Pattern.compile("[Ff]rom [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[bB]etween the years [0-9]{4} and [0-9]{4}"));
		
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} à [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} à [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]ans les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]ntre [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[aA]nnées [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]urant la période [0-9]{4} - [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]n [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]ntre les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]urant les années [0-9]{4} et [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} jusqu'en [0-9]{4}"));
		timePatterns.add(Pattern.compile("[dD]e [0-9]{4} et à celui de [0-9]{4}"));
		timePatterns.add(Pattern.compile("[eE]n [0-9]{4} et se termina en [0-9]{4}"));
		
		timePatterns.add(Pattern.compile("[vV]on [0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[vV]on [0-9]{4}\\s*(-|--|–)\\s*[0-9]{4}"));
		timePatterns.add(Pattern.compile("[zZ]wischen [0-9]{4} und [0-9]{4}"));
		timePatterns.add(Pattern.compile("[zZ]wischen den Jahren [0-9]{4} und [0-9]{4}"));
		timePatterns.add(Pattern.compile("[iI]n den Jahren [0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} bis [0-9]{4}"));
		timePatterns.add(Pattern.compile("[0-9]{4} bis einschließlich [0-9]{4}"));
		timePatterns.add(Pattern.compile("[aA]us den Jahren [0-9]{4} und [0-9]{4}"));
	}
	
	/**
	 * 
	 * @param evidence
	 * @return 
	 */
	public static DefactoTimePeriod findTimePeriod(Evidence evidence) {
		
		Set<String> sentences = new HashSet<>();
		for ( ComplexProof proof : evidence.getComplexProofs() ) {
			sentences.add(proof.getMediumContext().trim());
		}
		
		return findTimePeriod(timePatterns, sentences);
	}
	
	private static void test(Evidence evidence) {
		
		for ( WebSite ws : evidence.getAllWebSites() ) {
			
			String[] matches = StringUtils.substringsBetween(ws.getText(), evidence.getModel().timePeriod.from + "", evidence.getModel().timePeriod.to+ "");
			if ( matches == null ) continue;
			
			for ( String substring : matches) {
				
				if ( substring.length() < 100 )
					System.out.println(substring);
			}
		}
	}

	public static DefactoTimePeriod findTimePeriod(Set<Pattern> patterns, Set<String> sentences) {
		
		Frequency firstFreq = new Frequency();
		Frequency secondFreq = new Frequency();
		
		for ( String sentence : sentences ) {
			
			for ( Pattern pat : patterns ) {
				
				Matcher matcher = pat.matcher(sentence);
				while (matcher.find()) {
					
					patFreq.addValue(pat.pattern());
					
					String both = matcher.group();
					Matcher yearMatcher = Pattern.compile("[0-9]{4}").matcher(both);
					List<String> matches = new ArrayList<>();
					
					// get first and second year
					while (yearMatcher.find()) {
						
						String match = yearMatcher.group();
						matches.add(match);
					}
					if ( matches.size() == 2) {
						
						Integer first = Integer.valueOf(matches.get(0));
						Integer second = Integer.valueOf(matches.get(1));
						
						if ( first <= 2013 && first > 1850 && second <= 2013 && second > 1850) {
							
							firstFreq.addValue(matches.get(0));
							secondFreq.addValue(matches.get(1));
						}
					}
					else System.err.println("YEAR MATCHES WENT WRONG: " + matches);
				}
			}
		}
		List<Entry<Comparable<?>, Long>> first = firstFreq.sortByValue();
		List<Entry<Comparable<?>, Long>> second = secondFreq.sortByValue();
		
		if ( first.isEmpty() || second.isEmpty() ) return null; 
		return new DefactoTimePeriod(Integer.valueOf((String)first.get(0).getKey()), Integer.valueOf((String)second.get(0).getKey()));
	}
	
	public static void main(String[] args) {
		
		String s = "iven to every candidate. Katie Holmes and Tom Cruise were married for 7 years (from 2005 - 20 November 2012). Holmes began dating actor Tom C";
		Set<String> asd = new HashSet<String>();
		asd.add(s);
		System.out.println(TimePeriodSearcher.findTimePeriod(timePatterns, asd));
	}
}
