package com.bwssystems.HABridge.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public final class TextStringFormatter {
	private TextStringFormatter() {
		// empty - prevent construction
	}

	  /**
	   Escapes characters for text appearing as data in the 
	   <a href='http://www.json.org/'>Javascript Object Notation</a>
	   (JSON) data interchange format.
	   
	   <P>The following commonly used control characters are escaped :
	   <table border='1' cellpadding='3' cellspacing='0'>
	   <tr><th> Character </th><th> Escaped As </th></tr>
	   <tr><td> " </td><td> \" </td></tr>
	   <tr><td> \ </td><td> \\ </td></tr>
	   <tr><td> / </td><td> \/ </td></tr>
	   <tr><td> back space </td><td> \b </td></tr> 
	   <tr><td> form feed </td><td> \f </td></tr>
	   <tr><td> line feed </td><td> \n </td></tr>
	   <tr><td> carriage return </td><td> \r </td></tr>
	   <tr><td> tab </td><td> \t </td></tr>
	   </table>
	   
	   <P>See <a href='http://www.ietf.org/rfc/rfc4627.txt'>RFC 4627</a> for more information.
	  */
	public static String forJSON(String aText) {
		final StringBuilder result = new StringBuilder();
		StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != StringCharacterIterator.DONE) {
			if (character == '\"') {
				result.append("\\\"");
			} else if (character == '\\') {
				result.append("\\\\");
			} else if (character == '/') {
				result.append("\\/");
			} else if (character == '\b') {
				result.append("\\b");
			} else if (character == '\f') {
				result.append("\\f");
			} else if (character == '\n') {
				result.append("\\n");
			} else if (character == '\r') {
				result.append("\\r");
			} else if (character == '\t') {
				result.append("\\t");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	  /**
    Escape characters for text appearing in HTML markup.
    
    <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
    The idea is to neutralize control characters commonly used by scripts, such that
    they will not be executed by the browser. This is done by replacing the control
    characters with their escaped equivalents.  
    See {@link hirondelle.web4j.security.SafeText} as well.
    
    <P>The following characters are replaced with corresponding 
    HTML character entities :
    <table border='1' cellpadding='3' cellspacing='0'>
    <tr><th> Character </th><th>Replacement</th></tr>
    <tr><td> < </td><td> &lt; </td></tr>
    <tr><td> > </td><td> &gt; </td></tr>
    <tr><td> & </td><td> &amp; </td></tr>
    <tr><td> " </td><td> &quot;</td></tr>
    <tr><td> \t </td><td> &#009;</td></tr>
    <tr><td> ! </td><td> &#033;</td></tr>
    <tr><td> # </td><td> &#035;</td></tr>
    <tr><td> $ </td><td> &#036;</td></tr>
    <tr><td> % </td><td> &#037;</td></tr>
    <tr><td> ' </td><td> &#039;</td></tr>
    <tr><td> ( </td><td> &#040;</td></tr> 
    <tr><td> ) </td><td> &#041;</td></tr>
    <tr><td> * </td><td> &#042;</td></tr>
    <tr><td> + </td><td> &#043; </td></tr>
    <tr><td> , </td><td> &#044; </td></tr>
    <tr><td> - </td><td> &#045; </td></tr>
    <tr><td> . </td><td> &#046; </td></tr>
    <tr><td> / </td><td> &#047; </td></tr>
    <tr><td> : </td><td> &#058;</td></tr>
    <tr><td> ; </td><td> &#059;</td></tr>
    <tr><td> = </td><td> &#061;</td></tr>
    <tr><td> ? </td><td> &#063;</td></tr>
    <tr><td> @ </td><td> &#064;</td></tr>
    <tr><td> [ </td><td> &#091;</td></tr>
    <tr><td> \ </td><td> &#092;</td></tr>
    <tr><td> ] </td><td> &#093;</td></tr>
    <tr><td> ^ </td><td> &#094;</td></tr>
    <tr><td> _ </td><td> &#095;</td></tr>
    <tr><td> ` </td><td> &#096;</td></tr>
    <tr><td> { </td><td> &#123;</td></tr>
    <tr><td> | </td><td> &#124;</td></tr>
    <tr><td> } </td><td> &#125;</td></tr>
    <tr><td> ~ </td><td> &#126;</td></tr>
    </table>
    
    <P>Note that JSTL's {@code <c:out>} escapes <em>only the first 
    five</em> of the above characters.
   */

	public static String forHTML(String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '&') {
				result.append("&amp;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\t') {
				addCharEntity(9, result);
			} else if (character == '!') {
				addCharEntity(33, result);
			} else if (character == '#') {
				addCharEntity(35, result);
			} else if (character == '$') {
				addCharEntity(36, result);
			} else if (character == '%') {
				addCharEntity(37, result);
			} else if (character == '\'') {
				addCharEntity(39, result);
			} else if (character == '(') {
				addCharEntity(40, result);
			} else if (character == ')') {
				addCharEntity(41, result);
			} else if (character == '*') {
				addCharEntity(42, result);
			} else if (character == '+') {
				addCharEntity(43, result);
			} else if (character == ',') {
				addCharEntity(44, result);
			} else if (character == '-') {
				addCharEntity(45, result);
			} else if (character == '.') {
				addCharEntity(46, result);
			} else if (character == '/') {
				addCharEntity(47, result);
			} else if (character == ':') {
				addCharEntity(58, result);
			} else if (character == ';') {
				addCharEntity(59, result);
			} else if (character == '=') {
				addCharEntity(61, result);
			} else if (character == '?') {
				addCharEntity(63, result);
			} else if (character == '@') {
				addCharEntity(64, result);
			} else if (character == '[') {
				addCharEntity(91, result);
			} else if (character == '\\') {
				addCharEntity(92, result);
			} else if (character == ']') {
				addCharEntity(93, result);
			} else if (character == '^') {
				addCharEntity(94, result);
			} else if (character == '_') {
				addCharEntity(95, result);
			} else if (character == '`') {
				addCharEntity(96, result);
			} else if (character == '{') {
				addCharEntity(123, result);
			} else if (character == '|') {
				addCharEntity(124, result);
			} else if (character == '}') {
				addCharEntity(125, result);
			} else if (character == '~') {
				addCharEntity(126, result);
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Escape all ampersand characters in a URL.
	 * 
	 * <P>
	 * Replaces all <tt>'&'</tt> characters with <tt>'&amp;'</tt>.
	 * 
	 * <P>
	 * An ampersand character may appear in the query string of a URL. The
	 * ampersand character is indeed valid in a URL.
	 * <em>However, URLs usually appear as an <tt>HREF</tt> attribute, and such
	 * attributes have the additional constraint that ampersands must be
	 * escaped.</em>
	 * 
	 * <P>
	 * The JSTL <c:url> tag does indeed perform proper URL encoding of query
	 * parameters. But it does not, in general, produce text which is valid as
	 * an <tt>HREF</tt> attribute, simply because it does not escape the
	 * ampersand character. This is a nuisance when multiple query parameters
	 * appear in the URL, since it requires a little extra work.
	 */
	public static String forHrefAmpersand(String aURL) {
		return aURL.replace("&", "&amp;");
	}

	public static String forQuerySpace(String aURL) {
		return aURL.replace(" ", "\u0020");
	}
	public static String forQuerySpaceUrl(String aURL) {
		return aURL.replace(" ", "%20");
	}
	/**
	 * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
	 * 
	 * <P>
	 * Used to ensure that HTTP query strings are in proper form, by escaping
	 * special characters such as spaces.
	 * 
	 * <P>
	 * It is important to note that if a query string appears in an
	 * <tt>HREF</tt> attribute, then there are two issues - ensuring the query
	 * string is valid HTTP (it is URL-encoded), and ensuring it is valid HTML
	 * (ensuring the ampersand is escaped).
	 */
	public static String forURL(String aURLFragment) {
		String result = null;
		try {
			result = URLEncoder.encode(aURLFragment, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 not supported", ex);
		}
		return result;
	}

	private static void addCharEntity(Integer aIdx, StringBuilder aBuilder) {
		String padding = "";
		if (aIdx <= 9) {
			padding = "00";
		} else if (aIdx <= 99) {
			padding = "0";
		} else {
			// no prefix
		}
		String number = padding + aIdx.toString();
		aBuilder.append("&#" + number + ";");
	}
}
