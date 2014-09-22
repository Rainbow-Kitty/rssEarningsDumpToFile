/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssearningsdumptofile;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Poppa
 */
public class UtilityFunctions
{
    static String ReadADocument( String url )
    {
        StringBuilder webPageBuffer = new StringBuilder( 512 * 1024 );

        Integer reTry;

        for( reTry = 0; reTry < 3; reTry++ )
        {
            //
            try
            {
                BufferedInputStream input = new BufferedInputStream( new URL( url ).openStream() );
//                BufferedInputStream input = new BufferedInputStream( new FileInputStream( url ) );
                byte buffer[] = new byte [10240];
                Integer bytesRead;

                webPageBuffer.setLength( 0 );
                // above setLength() replaces delete() shown below
                // webPageBuffer.delete( 0, webPageBuffer.length() );

                while( (bytesRead = input.read( buffer, 0, buffer.length ) ) > 0 )
                {
                    webPageBuffer.append( new String( buffer, 0, bytesRead ) );
                }
                input.close();

                break;
            }
            catch( java.net.MalformedURLException ex )
            {
                System.out.println( "URL Exception: " + ex );
                break;
            }
            catch( java.io.FileNotFoundException ex )
            {
                System.out.println( "File not found exception: " + ex);
            }
            catch( java.io.IOException ex )
            {
                System.out.println( "IO Exception: " + ex + ". Retry #" + (reTry + 1) + " of 3" );
            }
            catch( java.lang.IllegalArgumentException ex )
            {
                System.out.println( "Illegal argument exception for BufferedInputStream: " + ex);
            }
        }   // end-for( try ... )

        // attempt to strip (most) html tags from webPage
        return webPageBuffer.toString()
                            .replaceAll( "&nbsp[;]?", " " ) // convert no-break spaces to spaces
                            .replace( "&#39;", "'" )        // covert #39 to single quote
                            .replace( "&#147;", "\"" )      // convert #147 to double quote
                            .replace( "&#148;", "\"" )      // convert #148 to double quote
                            .replace( "&#160;", " " )       // convert no-break number to spaces
                            .replaceAll( "\\xA0", " " )     // (also: convert no-break number to spaces)
                            .replace( "&quot;", "\"" )      // convert &quot to double quote
                            .replace( "&ldquo;", "\"" )     // convert &ldquo to double quote
                            .replace( "&rdquo;", "\"" )     // convert &rdquo to double quote
                            .replace( "&#8208;", "-" )      // convert &#8208; to hyphen
                            .replace( "&#8209;", "-" )      // convert &#8209; to hyphen
                            .replace( "&#8211;", "-" )      // convert &#8211; to hyphen
                            .replace( "&#8212;", "-" )      // convert &#8212; to double quote
                            .replace( "&#8220;", "\"" )     // convert &#8220; to double quote
                            .replace( "&#8221;", "\"" )     // convert &#8221; to double quote
                            .replace( "&amp;", "&" )        //
                            .replaceAll( "\\s+", " " )      // strip excessive spaces between words
                            .replaceAll("\\P{InBasic_Latin}", ""); // remove all non 
    }
    
    //
    //  parse chunk of rss feed (<item>...</item>) into tags
    //  if detecting french, will set language tag to french
    //
    static Map getTags( String table )
    {
        Map<String,String> tagList = new HashMap<>();

        Pattern patternTag = Pattern.compile( "</(\\w+\\s*:?\\s*\\w*)>" );
        Matcher matcherTag = patternTag.matcher( table );

        // if detecting french, set language to french
        Pattern patternFrench = Pattern.compile( "((\\sde[s]?\\s)|(\\sun[e]?\\s)|(trimestre))" );
        Matcher matcherFrench = patternFrench.matcher( table );

        if( matcherFrench.find() )
        {
            tagList.put( "language", "french" );
        }

        int    iLastTagPosition = 0;
        
        while( matcherTag.find() )
        {
            String sTag = matcherTag.group( 1 );

            // ignore html tags
            if( "|a|p|img|".indexOf( "|" + sTag + "|" ) >= 0 )
            {
                continue;
            }
            
            int     iStartPosition = table.indexOf( "<" + sTag, iLastTagPosition ),
                    iStopPosition = 0;

            if( iStartPosition > 0 )
            {
                iStartPosition = table.indexOf( ">", iStartPosition ) + 1;

                iLastTagPosition = iStartPosition;
                iStopPosition = table.indexOf( "</" + sTag + ">", iLastTagPosition );
            }
            if( iStartPosition > 0 && iStopPosition > 0 )
            {
                String value = table.substring( iStartPosition, iStopPosition );

                if( value.startsWith( "<![CDATA[" ) )
                {
                    value = value.substring( "<![CDATA[".length() );
                    if( value.endsWith( "]]>" ) )
                    {
                        value = value.substring( 0, value.length() - "]]>".length() );
                    }
                }
                // strip tags out of value and trim leading/trailing spaces
                value = value.replaceAll( "&gt;", ">" )
                             .replaceAll( "&lt;", "<" )
                             .replaceAll( "<[^>]*>", "" ).trim();

                tagList.put( sTag, value );

//                System.out.println( "\t<" + sTag + ">" + value + "</" + sTag + ">" );
            }
        }

        return tagList;
    }


/*
    FOLLOWING CODE REPLACED 2014-04-20 by same named function following this...
    //
    //
    //
    static String GetSymbol( String webPage )
    {
        // * * * * * * * * * * * * * * * * * * * * * * * * * *
        // look for probably ticker symbol if document is press release. ex: EX-99.1
        // * * * * * * * * * * * * * * * * * * * * * * * * * *
        // actual pattern for testing is "(\(\w*[ /-]*\w*\s*\:\s*?)([a-zA-Z]+[.-_]?[a-zA-Z]+)[\),; ]" tested at http://regexpal.com/
//        Pattern patternSymbol = Pattern.compile( "(\\(\\w*[ /-]*(\\w*\\s+)+\\:\\s*)([a-zA-Z]+[.-_]?[a-zA-Z]+)[\\),; ]" );
        Pattern patternSymbol = Pattern.compile( "(\\(\\w*[ /-]*(\\w*\\s*){0,4}\\:\\s*)([a-zA-Z]+[.-_]?[a-zA-Z]+)[),; ]" );
        Matcher matcherSymbol = patternSymbol.matcher( webPage );
        String  Symbol = "";
        Boolean bUniqueTickerFound = true;
        for( Integer iCount = 0; iCount < 10 && matcherSymbol.find(); iCount++ )
        {
            if( matcherSymbol.group(1).startsWith( "(NYSE" ) == false
             && matcherSymbol.group(1).startsWith( "(NASDAQ" ) == false
             && matcherSymbol.group(1).startsWith( "(OTC" ) == false
             && matcherSymbol.group(1).startsWith( "(PINK" ) == false
             && matcherSymbol.group(1).startsWith( "(PK" ) == false
             && matcherSymbol.group(1).startsWith( "(AMEX" ) == false)
            {
//                System.out.println( "\tGroup0 {" + matcherSymbol.group(0) + "}" );
//                System.out.println( "\tGroup1 {" + matcherSymbol.group(1) + "}" );
            }
            else
            {
//                System.out.println( "\tGroup2 {" + matcherSymbol.group(2) + "}" );

                String sTemp = matcherSymbol.group(2).toUpperCase();
//                System.out.println( "\t:. Symbol {" + sTemp + "}" );
                if( Symbol.isEmpty() )
                {
                    Symbol = sTemp;
                }
                else if( Symbol.compareToIgnoreCase( sTemp ) != 0)
                {
                    bUniqueTickerFound = false;
                }
            }
        }
        if( bUniqueTickerFound == false )
        {
            Symbol = "";
        }

        // some companies still publishing their legacy symbols ending with .OB or .PK
        if( Symbol.endsWith( ".OB" ) || Symbol.endsWith( ".PK" ) )
        {
            Symbol = Symbol.substring( 0, Symbol.indexOf( '.' ) );
        }

        return Symbol;
    }
*/

    //
    //  Use cases tested using regexpal.com:
    /*
            Stewart Information Services Corp. (NYSE-STC)
            Stewart Information Services Corp. (NYSE:STC)
            SPI Solar (“SPI”) (SOPW:OTCBB)
            Medworxx Solutions Inc. ("Medworxx")(TSX VENTURE:MWX)
            TSS, Inc. (Other OTC: TSSI)
            Nutrisystem, Inc. (NASDAQ:NTRI)
            International Speedway Corporation (NASDAQ Global Select Market: ISCA; OTC Bulletin Board: ISCB) ("ISC")
            Aegion Corporation (Nasdaq Global Select Market: AEGN)
            Crestwood Midstream Partners LP (NYSE: CMLP)
            Taylor Devices, Inc.  (NASDAQ SmallCap: "TAYD")
            Summit Financial Services Group, Inc. (OTC: SFNS)
            Summit Financial Services Group, Inc. (OTC Bulletin Board: SFNS) 
    // following do not work and are not supported
            Bank of Commerce (BONC.OB)                          // not working 
            Mobile  TeleSystems  OJSC  ("MTS"  -  NYSE:   MBT)  // not working 
    */
    //      
    //
    static String GetSymbol( String webPage )
    {
        // * * * * * * * * * * * * * * * * * * * * * * * * * *
        // look for probably ticker symbol if document is press release. ex: EX-99.1
        // * * * * * * * * * * * * * * * * * * * * * * * * * *
        // actual pattern for testing is "(\(\w*[ /-]*\w*\s*\:\s*?)([a-zA-Z]+[.-_]?[a-zA-Z]+)[\),; ]" tested at http://regexpal.com/
//        Pattern patternSymbol = Pattern.compile( "(\\(\\w*[ /-]*\\w*\\s*\\:\\s*)([a-zA-Z]+[.-_]?[a-zA-Z]+)[\\),; ]" );
        Pattern patternSymbol = Pattern.compile( "\\((\\w*[ /-]*(\\w*\\s*){0,4})[-:]\\s*[\"']?([a-zA-Z]+[.-_]?[a-zA-Z]+)[\"''),; ]" );
        Matcher matcherSymbol = patternSymbol.matcher( webPage );
        String  Symbol = "";
        Boolean bUniqueTickerFound = true;
        for( Integer iCount = 0; iCount < 10 && matcherSymbol.find(); iCount++ )
        {
            if( matcherSymbol.group(1).toLowerCase().contains( "nyse" )
             || matcherSymbol.group(1).toLowerCase().contains( "nasdaq" )
             || matcherSymbol.group(1).toLowerCase().contains( "otc" )
             || matcherSymbol.group(1).toLowerCase().contains( "pink" )
             || matcherSymbol.group(1).toLowerCase().contains( "amex" )
              )
            {
//                System.out.println( "\tGroup2 {" + matcherSymbol.group(2) + "}" );

                String sTemp = matcherSymbol.group(3).toUpperCase();
//                System.out.println( "\t:. Symbol {" + sTemp + "}" );
                if( Symbol.isEmpty() )
                {
                    Symbol = sTemp;
                }
                else if( Symbol.compareToIgnoreCase( sTemp ) != 0)
                {
                    bUniqueTickerFound = false;
                }
            }
            else if( matcherSymbol.group(3).toLowerCase().contains( "nyse" )
                  || matcherSymbol.group(3).toLowerCase().contains( "nasdaq" )
                  || matcherSymbol.group(3).toLowerCase().contains( "otc" )
                  || matcherSymbol.group(3).toLowerCase().contains( "pink" )
                  || matcherSymbol.group(3).toLowerCase().contains( "amex" )
                   )
            {
                if( matcherSymbol.group(1).length() <= 10 )
                {
                    String sTemp = matcherSymbol.group(1).toUpperCase();
                    System.out.println( "\t:. Symbol {" + sTemp + "}" );
                    if( Symbol.isEmpty() )
                    {
                        Symbol = sTemp;
                    }
                    else if( Symbol.compareToIgnoreCase( sTemp ) != 0)
                    {
                        bUniqueTickerFound = false;
                    }
                }
            }
            else
            {
                System.out.println( "\tGroup0 {" + matcherSymbol.group(0) + "}" );
                System.out.println( "\tGroup1 {" + matcherSymbol.group(1) + "}" );
            }
        }
        if( bUniqueTickerFound == false )
        {
            Symbol = "";
        }

        // some companies still publishing their legacy symbols ending with .OB or .PK
        if( Symbol.endsWith( ".OB" ) || Symbol.endsWith( ".PK" ) )
        {
            Symbol = Symbol.substring( 0, Symbol.indexOf( '.' ) );
        }

        return Symbol;
    }


    //
    //
    //
    static String GetSymbolByTitle( String title )
    {
        String query = "SELECT IFNULL( lotc.Symbol, loc.Symbol ), IFNULL( lotc.Name, loc.Name ), pq.* " +
                       "FROM test.pr_queue pq " +
                            "LEFT JOIN list_of_otc_securities lotc ON LEFT( lotc.Name, 10 ) = left( pq.Title, 10 ) " +
                            "LEFT JOIN list_of_companies loc ON LEFT( loc.Name, 10 ) = LEFT( pq.Title, 10 ) " +
                       "where (lotc.Symbol IS NOT NULL OR loc.Symbol IS NOT NULL)";

        
        return null;
    }
}
