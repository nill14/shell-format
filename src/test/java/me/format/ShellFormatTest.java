package me.format;

import static org.junit.Assert.*;
import me.format.shellformat.ShellDictionary;
import me.format.shellformat.ShellFormat;

import org.junit.Test;

public class ShellFormatTest {

    @Test
    public void test1replace() {
	ShellFormat shellFormat = ShellFormat.dictionary("pom", "World!").compile();

	String result = shellFormat.format("Hello $pom");
	assertEquals("Hello World!", result);
    }

    @Test
    public void test1aNestedReplace() {
	ShellFormat shellFormat = ShellFormat.dictionary(
		"pom",		"World${exm}",
		"exm", 		"!!!"
		).compile();
        
        String result = shellFormat.format("Hello $pom");
	assertEquals("Hello World!!!", result);
    }
    
    @Test
    public void test2multiline() {
	String multiline = ShellFormat.multiline(
		"abc",
		"cde"
		);

	assertEquals("abc\ncde", multiline);
    }
    
    @Test
    public void test3html() {
	String body = ShellFormat.multiline(
		"<html>",
		"    <body>",
		"	${line1}",
		"	${line2}",
		"    </body>",
		"<html>"		
		);

	String value = "5";
	ShellDictionary dictionary = ShellFormat.dictionary();
	
	if (value != null) {
	    dictionary
	    	.append("line1", "<p>Weight: ${weight}kg</p>")
	    	.append("weight", value);
	}
	
	String result = dictionary.compile().format(body);
	String expected = ShellFormat.multiline(
		"<html>",
		"    <body>",
		"	<p>Weight: 5kg</p>",
		"    </body>",
		"<html>"		
		);
	
	assertEquals(expected, result);
    }    
    
    @Test
    public void test4hql() {
	String filterSelect = ShellFormat.multiline(
            "select r.$res_no", "from $ResourceTable r $joinStatements",
            "where r.${res_no} = (",
            "	select max(s.$res_no) from $ResourceTable s",
            "	where r.$res_root_no = s.$res_root_no )",
            "$whereConditions $orderBy1");

	String querySelect = ShellFormat.multiline(
		"select m from $ResourceTable m", 
        	"  $whereConditions  ",
        	"$whereX", 
        	"x $ddd $wfef x",
        	"left join fetch vals.mdParameter",
        	"where m.${res_no} in ($filterSelect) $orderBy2");

	String hqlString = ShellFormat.dictionary()
		.append("filterSelect", filterSelect)
		.append("ResourceTable", "ResTable")
		.append("res_root_no", "id")
		.append("res_no", "rid")
		.format(querySelect);

	System.out.println(hqlString);
	
	String patternStart = ShellFormat.multiline(
		"select m from ResTable m",
		"x x",
		"left join fetch vals.mdParameter");
	
	assertTrue(hqlString.startsWith(patternStart));
    }

}
