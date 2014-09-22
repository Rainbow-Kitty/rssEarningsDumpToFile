/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rssearningsdumptofile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 * @author Poppa
 */
public class BusinessWire 
{
    //
    //
    //
    BusinessWire()
    {
        String URL = "http://feeds.businesswire.com/BW/Earnings_News-rss";
        
        String rssFeed = UtilityFunctions.ReadADocument( URL );

        if( rssFeed.isEmpty() )
        {
            System.out.println( "Unable to fetch document from [" + URL + "]" );
            return;
        }

        // initialize two simple date format variables to eastern time zone
        SimpleDateFormat sdfInDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        sdfInDate.setTimeZone( TimeZone.getTimeZone( "America/New_York" ) );
        SimpleDateFormat sdfOutDate = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss z" );
        sdfOutDate.setTimeZone( TimeZone.getTimeZone( "America/New_York" ) );

        // not handling tables within tables
        String items [] = rssFeed.split( "<item>" );

        for( int iItem = 1; iItem < items.length; iItem++)
        {
            String item = items[iItem];

            Map tags = UtilityFunctions.getTags( item );

            /*
            **  location for any possible debug code to display tags
            */
            
            // ignore non-english languages until we can process a non-english press release
            String guid = tags.get( "guid" ).toString();
            if( guid.matches( "\\d+en-?\\w?" ) == false )
            {
                String language = guid;

                // skip leading digits to leave the language code
                while( Character.isDigit( language.charAt( 0 ) ) )
                {
                    language = language.substring( 1 );
                }

//                System.out.println( "<TR><TD COLSPAN='2'>Ignoring: language (" + language + ") not english</TD></TR>" );
                continue;
            }

            String PublicationDate = tags.get( "pubDate" ).toString(),
                   Symbol = (tags.get( "description" ) != null) ? UtilityFunctions.GetSymbol( tags.get( "description" ).toString() ) : "",
                   Source = "bizwire",
                   Link   = tags.get( "pheedo:origLink" ).toString(),
                   Title  = tags.get( "title" ).toString();

            // make sure title fits into test.pr_queue
            if( Title.length() > 255 )
            {
                Title = Title.substring( 1, 255 );
            }

            // convert if necessary PublicationDate to eastern time
            try
            {
                if( PublicationDate.endsWith( "UT" ) )
                {
                    PublicationDate += "C";
                }
                Date parsed = sdfInDate.parse( PublicationDate );
//                System.out.println( "Old:-> " + PublicationDate + " New:-> " + sdfOutDate.format( parsed ) );
                PublicationDate = sdfOutDate.format( parsed );
            }
            catch( ParseException pe)
            {
                System.out.println("ERROR: Cannot parse \"" + PublicationDate + "\"");
            }

            String row = PublicationDate + "|" + Symbol + "|" + Source  + "|" + Link + "|" + Title;

            System.out.println( row );

            SaveToFile.saveRecord( row );
        }
    }
}
