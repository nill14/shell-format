package me.format.shellformat;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Published in terms of BSD or Apache2 license. <br/>
 *
 * ShellFormat supports variable replace. Variables are in form ${beer} or $milk.
 * Multiline formatting is preserved. Variable could expand into string with variables.
 * Missing variables are erased.
 *
 *
 */
public class ShellFormat {

    private static Logger LOG = LoggerFactory.getLogger(ShellFormat.class);

    private static String CR = String.valueOf('\n');

    private static Pattern VAR_PATTERN = Pattern.compile("(\\s*)(\\$\\{?)(\\w+)(\\}?)(\\s*)");
    private static Pattern CR_PATTERN = Pattern.compile("\\n");
    private static Pattern LINE_PATTERN = Pattern.compile("[^\\n]+");


    private static int SPACE1_GROUP = 1;
    private static int START_GROUP = 2;
    private static int VAR_GROUP = 3;
    private static int END_GROUP = 4;
    private static int SPACE2_GROUP = 5;

    private Map<String, Object> dictionary;

    /*package*/ ShellFormat(Map<String, Object> dictionary) {
	this.dictionary = dictionary;
    }
    
    
    /**
     * @return a new ShellDictionary builder
     */
    public static ShellDictionary dictionary() {
	return ShellDictionary.dictionary();
    }
    
    /**
     * @param arguments the pairs of (key, value)
     * @return a new ShellDictionary builder
     */
    public static ShellDictionary dictionary(String ... arguments) {
	return ShellDictionary.dictionary(arguments);
    }
    
    
    /**
     * @return the actual dictionary (immutable)
     */
    public Map<String, Object> getDictionary() {
        return dictionary;
    }
    
    /**
     * Composes a multi-line string from input lines.
     * 
     * @param lines the parts of final string
     * @return compiled string concatenated by newlines
     */
    public static String multiline(String ... lines) {
        StringBuilder b = new StringBuilder();
        if(lines != null && lines.length > 0) {
            for (int i = 0; i < lines.length; i++) {
                if(i > 0) {
                    b.append(CR);
                }
                b.append(lines[i]);
            }
        }
        return b.toString();
    }


    /**
     * @param pattern a pattern to format
     * @return the formatted string
     */
    public String format(String pattern) {
	StringBuilder b = new StringBuilder(pattern);
	format0(b);
	return b.toString();
    }
    
    /**
     * @param lines the pattern lines to format
     * @return the formatted string
     */
    public String format(String ... lines) {
	return format(multiline(lines));
    }
    

    private String getIndent(String prefix, int indent) {
        StringBuilder b = new StringBuilder(prefix.length() + indent);
        b.append(prefix);

        for (int i = 0; i < indent; i++) {
            b.append(" ");
        }
        return b.toString();
    }

    private StringBuilder format0(StringBuilder result) {

        Matcher lineMatcher = LINE_PATTERN.matcher(result);
        Matcher matcher = VAR_PATTERN.matcher(result);
        debug(result);

        int startLine = 0;
        int endLine = 0;
        int index = 0;

        while(lineMatcher.find(startLine)) {

            startLine = lineMatcher.start();
            endLine = lineMatcher.end();
            index = startLine;
            matcher.region(index, endLine);

            while(matcher.find()) {

                String startSpace = matcher.group(SPACE1_GROUP);
                String startTag = matcher.group(START_GROUP);
                String name = matcher.group(VAR_GROUP);
                String endTag = matcher.group(END_GROUP);
                String endSpace = matcher.group(SPACE2_GROUP);

                int startReplace = matcher.start(START_GROUP);
                int endReplace = matcher.end(END_GROUP);

                int startS = matcher.start(SPACE1_GROUP);
                int endS = matcher.end(SPACE2_GROUP);

                Object value = dictionary.get(name);

                if (startTag.length()-1 != endTag.length()) {//e.g. $name} or ${name
                    throw new RuntimeException(String.format("Unpaired {} - %s%s%s", startTag, name, endTag));
                }

                boolean isMultiline = false;

                if(value != null && !"".equals(value)) {
                    String replacement = String.valueOf(value);

                    if( startReplace > startLine ) { //if do indent
                        Matcher m2 = CR_PATTERN.matcher(replacement);
                        if(m2.find()) {
                            isMultiline = true;
                            boolean isContinue = endS < endLine;
                            int indent = startReplace - startLine;
                            replacement = m2.replaceAll(getIndent(CR, indent));

                            if(isContinue) { 
                        	//if line continues after multiline variable then do break
                                //replacement = replacement.concat(CR);
                                replacement = replacement.concat(getIndent(CR, indent-1));
                            }
                        }
                    }


                    result.replace(startReplace, endReplace, replacement);

                    if(isMultiline) {
                        endLine = result.indexOf(CR, index);
                        debug(result);

                    } else {
                        endLine += -(endReplace - startReplace) + replacement.length();
                    }

                } else {
                    if(!startSpace.isEmpty() && !endSpace.isEmpty()) { //if space is before and after
                        endReplace = endS; // replace space after too
                    }

                    if( startS == startLine && endS == endLine ) { //if empty line remains
                        boolean isEof = endS == result.length()-1;

                        startReplace = startS;// remove empty line
                        endReplace = endS + (isEof? 0:1); //\n char
                        endLine += -(endS - startReplace);//special flow to prevent endline == index-1 - for range <index, endline)

                    } else {
                        endLine += -(endReplace - startReplace);
                    }

                    result.delete(startReplace, endReplace);
                }

                index = startS; //next search will have space before too
                //index = startReplace;//operate recursivelly
                matcher.region(index, endLine);
            }

            startLine = endLine;
        }


        debug(result);

        return result;
    }

    private void debug(StringBuilder result) {
        if(LOG.isTraceEnabled()) {
            LOG.trace("=========================================");
            LOG.trace(result.toString());
        }
    }



}
