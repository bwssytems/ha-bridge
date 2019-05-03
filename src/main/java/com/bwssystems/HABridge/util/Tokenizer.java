package com.bwssystems.HABridge.util;


/**
 * This class perform similar functionality to StringTokenizer class but faster.
 * The difference is StringTokenizer doesn't count empty token, but this class does.
 *
 * @author Henry Zheng
 * @url http://www.ireasoning.com
 */
public class Tokenizer
{
    private Tokenizer()
    {
    }

    /**
     * It's different from the other parse method in that it checks left and right string first, which take higer
     * priority than the delimiter. For example, if left and right is ", for string a:b:1"c:d"2:3 ,
     * it returns { a, b, 1"c:d"2, 3 }
     * @param left the openning tag of higher priority token
     * @param right the closing tag of higher priority token
     * @trimEachToken if true, each token will be trim by calling String.trim()
     */
    public static String[] parse(String text, char delimiter, boolean trimEachToken, String left, String right)
    {
        if(text == null) return null;
        StringArray tokens = new StringArray();
        int pos1 = -1;
        int pos2 = -1;
        int firstPos = -1;
        while(true)
        {
            pos2 = text.indexOf(delimiter, firstPos + 1);
            if(pos2 < 0 )
            {
                String str = text.substring(pos1 + 1);
                if(trimEachToken )
                {
                    str = str.trim();
                }
                tokens.add(str);
                break;
            }
            if(pos2 == pos1 + 1)
            {
                tokens.add("");
            }
            else
            {
                int tagPos1 = text.indexOf(left, firstPos + 1);
                if(tagPos1 > 0 && tagPos1 < pos2 )
                {
                    int tagPos2 = text.indexOf(right, tagPos1 + 1);
                    if(tagPos2 > 0)
                    {
                        firstPos = tagPos2;
                        continue;
                    }
                }
                String str = text.substring(pos1 + 1, pos2);
                if(trimEachToken )
                {
                    str = str.trim();
                }
                tokens.add(str);
            }
            pos1 = pos2;
            firstPos = pos1;
        }
        String[] ret = tokens.toArray();
        return ret;
    }

    /**
     * @trimEachToken if true, each token will be trim by calling String.trim()
     */
    public static String[] parse(String text, char delimiter, boolean trimEachToken)
    {
        return parse(text, delimiter, trimEachToken, false);
    }
    
    /**
     * @trimEachToken if true, each token will be trim by calling String.trim()
     */
    public static String[] parse(String text, char delimiter, boolean trimEachToken, boolean ignoreEmptyToken)
    {
        if(text == null) return null;
        StringArray tokens = new StringArray();
        int pos1 = -1;
        int pos2 = -1;
        while(true)
        {
            pos2 = text.indexOf(delimiter, pos1 + 1);
            if(pos2 < 0 )
            {
                String str = text.substring(pos1 + 1);
                if(trimEachToken )
                {
                    str = str.trim();
                }
                if(ignoreEmptyToken)
                {
                    if(str.length() != 0) tokens.add(str);
                }
                else
                {
                    tokens.add(str);
                }
                break;
            }
            if(pos2 == pos1 + 1)
            {
                if(!ignoreEmptyToken) { tokens.add(""); }
            }
            else
            {
                String str = text.substring(pos1 + 1, pos2);
                if(trimEachToken )
                {
                    str = str.trim();
                }
                if(ignoreEmptyToken)
                {
                    if(str.length() != 0) tokens.add(str);
                }
                else
                {
                    tokens.add(str);
                }
            }
            pos1 = pos2;
        }
        String[] ret = tokens.toArray();
        return ret;
    }
    
    /**
     * Does not trim each token.
     * @see #parse(String, char, boolean)
     */ 
    public static String[] parse(String text, char delimiter)
    {
        return parse(text, delimiter, false);
    }

//    public static void main(String[] args)
//    {
//        String str = "1,\"2,\"ab\",ttt1,\"3,,a\"222\",4";
//        if(args.length > 0)
//        {
//            str = args[0];
//        }
        // String [] tokens = Tokenizer.parse(str, ',');
        //
        //// System.out.println( "Text = (" + str + ")");
        // // System.out.println( "------------------------------------------");
        // for (int i = 0; i < tokens.length ; i++)
        // {
        //     // System.out.println( "(" + tokens[i] + ")");
        // }
        // // System.out.println( "------------------------------------------");

        // String [] tokens = Tokenizer.parse(str, ',', new String[]{"("},  new String[]{")"});
        //
        // // System.out.println( "Text = [" + str + "]");
        // // System.out.println( "------------------------------------------");
        // for (int i = 0; i < tokens.length ; i++)
        // {
        //     // System.out.println( "[" + tokens[i] + "]");
        // }
        // // System.out.println( "------------------------------------------");

 //   }
}//end of class Tokenizer