package com.crinqle.dlroom.util;


import java.io.*;
import java.util.*;



/**
 * Preferences.  Which can be loaded from a file.  It looks
 * like this:
 * <p>
 * <p>
 * #
 * # This is a comment line
 * #
 * <p>
 * [Color Spaces]
 * WorkingSpace=ProPhoto RGB
 * <p>
 * [Directories]
 * ColorProfiles=/home/qrux/.clear/profiles
 * <p>
 * <p>
 * The file contains simple ASCII text; each line either represent a
 * section heading (prefixed with a '[' and terminated with a ']') or
 * a property/value pair.  Properties cannot begin with a '[' nor can
 * they contain a '=', the first of which delimits properties and
 * values.  Properties which appear after a section heading belong to
 * that section; sections do not contain sections.
 * <p>
 * Lines whose first non-whitespace character is '#' is a comment.
 * Lines which contain nothing but whitespace are disregarded, along
 * with comments.
 * <p>
 * Lines are trimmed of whitespace first.
 */
public class Prefs
{
    private Hashtable<String, Hashtable<String, String>> f_hash = new Hashtable<String, Hashtable<String, String>>();
    private Hashtable<String, String>                    f_def  = new Hashtable<String, String>();


    public static Prefs loadFromFile ( File file ) throws IOException
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            Prefs prefs = new Prefs();

            String section = null;
            String line    = null;

            while ( (line = reader.readLine()) != null )
            {
                line = line.trim();

                if ( line.length() == 0 )
                    continue;

                else if ( line.charAt(0) == '#' )
                    continue;

                else if ( line.charAt(0) == '[' )
                {
                    final int ei = line.length() - 1;
                    section = line.substring(1, ei);
                }

                else
                {
                    final int index = line.indexOf('=');

                    if ( index < 0 )
                        continue;

                    String property = line.substring(0, index);
                    String value    = line.substring(index + 1);

                    if ( property != null )
                        property = property.trim();
                    if ( value != null )
                        value = value.trim();

                    System.out.println("[" + section + "] " + property + ": " + value);

                    if ( section == null )
                        prefs.put(property, value);
                    else
                        prefs.put(section, property, value);
                }
            }

            return prefs;
        }
        catch ( Exception e )
        {
        }

        return null;
    }


    public String get ( String prop )
    {
        return (String)f_def.get(prop);
    }


    public void put ( String prop, String value )
    {
        f_def.put(prop, value);
    }


    public String get ( String section, String prop )
    {
        Hashtable<String, String> h = f_hash.get(section);

        if ( h == null )
            return null;

        return h.get(prop);
    }


    public void put ( String section, String prop, String value )
    {
        Hashtable<String, String> h = f_hash.get(section);

        if ( h == null )
            f_hash.put(section, (h = new Hashtable<String, String>()));

        h.put(prop, value);
    }


    /*
     *
     * main driver.
     *
     */
    public static void main ( String[] args ) throws IOException
    {
        final int argc = args.length;

        try
        {
            for ( int i = 0; i < argc; ++i )
                  Prefs.loadFromFile(new File(args[0]));
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
