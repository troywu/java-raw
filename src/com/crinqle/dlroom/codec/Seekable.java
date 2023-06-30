package com.crinqle.dlroom.codec;


import java.io.*;



public interface Seekable
{
    long getPosition () throws IOException;

    int read4 () throws IOException;

    int read2 () throws IOException;

    long seek ( long n ) throws IOException;

    int skipBytes ( int n ) throws IOException;

    int bits ( int n ) throws Exception;

    int read () throws IOException;

    int read ( byte[] b ) throws IOException;

    int read ( byte[] b, int offset, int length ) throws IOException;

    int readFully ( byte[] b, int offset, int length ) throws IOException;
}
