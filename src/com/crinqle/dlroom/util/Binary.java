package com.crinqle.dlroom.util;

import java.util.*;


public class Binary
{
	public static int toInt ( String s )
	{
		final int length = (s.length() > 32) ? 32 : s.length();

		int n = 0;

		for ( int i = 0; i < length; ++i )
			n = ((n | ((s.charAt(i) == '0') ? 0 : 1)) << 1);

		return n;
	}


	public static String toBinary ( byte n )
	{
		String s = "";

		for ( int i = 0; i < 8; ++i )
		{
			s = (((n & 1) == 1) ? "1" : "0") + s;
			n >>= 1;
		}

		return s;
	}


	public static String toBinary ( short n )
	{
		String s = "";

		for ( int i = 0; i < 16; ++i )
		{
			s = (((n & 1) == 1) ? "1" : "0") + s;
			n >>= 1;
		}

		return s;
	}


	public static String toBinary ( char n )
	{
		String s = "";

		for ( int i = 0; i < 16; ++i )
		{
			s = (((n & 1) == 1) ? "1" : "0") + s;
			n >>= 1;
		}

		return s;
	}


	public static String toBinary ( int n )
	{
		String s = "";

		for ( int i = 0; i < 32; ++i )
		{
			s = (((n & 1) == 1) ? "1" : "0") + s;
			n >>= 1;
		}

		return s;
	}


	public static String toBinary ( long n )
	{
		String s = "";

		for ( int i = 0; i < 64; ++i )
		{
			s = (((n & 1) == 1) ? "1" : "0") + s;
			n >>= 1;
		}

		return s;
	}


	public static void main ( String args[] )
	{
		System.out.println(" 21: " + toBinary((byte)21));
		System.out.println("819: " + toBinary((short)819));
		System.out.println(" 69: " + toBinary((int)69));
		System.out.println(" 69: " + toBinary((long)69));
	}
}
