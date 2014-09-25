/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rssearningsdumptofile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Poppa
 */
public class SaveToFile
{
    static String path = "\\TMP\\rssEarningsDumpToFile.txt";
    static FileWriter fw = null;
    static String buffer = "";

    burp;
    
    //
    //  This initializer reads single file into memory for purpose of not adding duplicates
    //
    //
    SaveToFile()
    {
        try
        {
            // Buffer size of 5 megabytes determined by dumping 9423 records from test.pr_queue covering ~6 weeks.
            // Including all columns whereas here we only store five of nine columns.
            // The file created by mysqldump (included create table ddl) was 2,707,526 bytes.
            // Therefore, 5 megabytes should hold at least three months of records.
            // Will require cron job to run weekly to purge entries older than two months.
            // 
            char [] bytes = new char [5 * 1024 * 1024];
            FileReader file = new FileReader( path );
            Integer bytesRead;

            bytesRead = file.read( bytes, 0, bytes.length );

            file.close();
            
            if( bytesRead > 0 )
            {
                buffer = new String( bytes, 0, bytesRead );
            }
        }
        catch( FileNotFoundException ex )
        {
            System.out.println( "File not found exception: " + ex);
        }
        catch( IOException ioe )
        {
            System.out.println( "IO Exception: " + ioe + "." );
        }
    }


    //
    //
    static int saveRecord( String record )
    {
        if( buffer.contains( record ) )
        {
            return 0;
        }

        try
        {
            //
            //  open file for writing append
            //
            if( fw == null )
            {
                fw = new FileWriter( path, true );
            }
            fw.write( record + "\n" );
        }
        catch( IOException ex )
        {
            Logger.getLogger(SaveToFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    
    //
    static int close()
    {
        try
        {
            // if file was opened for write
            if( fw != null )
            {
                // flush and close the file
                fw.close();
                fw = null;      // make sure we do not try to use a closed file descriptor
            }
        }
        catch( IOException ex )
        {
            Logger.getLogger(SaveToFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }


    //
    //
    protected void finalize() throws Throwable
    {
        try
        {
            if( fw != null )
            {
                fw.close();
                fw = null;
            }
        }
        catch( IOException ex )
        {
            Logger.getLogger(SaveToFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            super.finalize();
        }
    }
}
