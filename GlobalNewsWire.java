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
public class GlobalNewsWire
{
    //
    //
    //
    GlobalNewsWire()
    {
        String URL = "http://globenewswire.com/Rss/subjectcode/13/Earnings%20Releases%20And%20Operating%20Results";
        
        String rssFeed = UtilityFunctions.ReadADocument( URL );

        if( rssFeed.isEmpty() )
        {
            System.out.println( "Unable to fetch document from [" + URL + "]" );
            return;
        }

        // initialize two simple date format variables to eastern time zone
        // input date format 2014-05-15T03:09:34Z
        SimpleDateFormat sdfInDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdfInDate.setTimeZone( TimeZone.getTimeZone( "Etc/Universal" ) );
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
            if( !tags.get( "link" ).toString().contains("/en/") )
            {
//                String [] elements = tags.get( "link" ).toString().split( "/" );
//                System.out.println( "<TR><TD COLSPAN='2'>Ignoring: language (" + elements[9] + ") not english</TD></TR>" );
                continue;
            }

            String PublicationDate = tags.get( "a10:updated" ).toString(),
                   Symbol = (tags.get( "description" ) != null) ? UtilityFunctions.GetSymbol( tags.get( "description" ).toString() ) : "",
                   Source = "globenews",
                   Link   = tags.get( "link" ).toString(),
                   Title  = tags.get( "title" ).toString();

            // make sure title fits into test.pr_queue
            if( Title.length() > 255 )
            {
                Title = Title.substring( 1, 255 );
            }

            // convert if necessary PublicationDate to eastern time
            try
            {
                // replace T with space, replace Z with GMT to work with simple date format
                PublicationDate = PublicationDate.replace( "T", " " )
                                                 .replace( "Z", " GMT" );
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
        }
    }
}
