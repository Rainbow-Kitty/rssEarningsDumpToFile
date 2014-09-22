/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rssearningsdumptofile;

/**
 *
 * @author Poppa
 */
public class RssEarningsDumpToFile {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        SaveToFile file = new SaveToFile();
        
        //
        //  These are ordered by smallest queue to largest
        //
        CNWGroup cnw = new CNWGroup();

        GlobalNewsWire gnw = new GlobalNewsWire();

        PRNewsWire prn = new PRNewsWire();

        //
        //  if no arguments then fetch from longer rss feeds.
        //  thought is to run shorter rss fees more often and longer rss feeds less often.
        //
        if( args.length == 0 )
        {
            MarketWire mw = new MarketWire();

//        OTCMarkets om = new OTCMarkets();

            BusinessWire bw = new BusinessWire();
        }

        SaveToFile.close();
    }
}
