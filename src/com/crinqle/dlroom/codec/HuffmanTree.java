package com.crinqle.dlroom.codec;


import java.util.*;



public class HuffmanTree
{
    private HuffmanNode root    = new HuffmanNode();
    private HuffmanNode current = root;
    private int         f_depth = 0;

	/*
	 * DEBUG! only
	 *
	 private StringBuffer f_pathSave = new StringBuffer();
	 private String f_path = "";
	*/


    /**
     * @param array             This array must not contain, for each value
     *                          from index 0 to index (distributionCount - 1), values which
     *                          are greater than 2^(index).  If it does, we'll throw a
     *                          runtime exception here.  Also, if we don't get enough
     * @param distributionCount
     *
     * @throws RuntimeException
     */
    public HuffmanTree ( short[] array, final int distributionCount )
    {
        int leafIndex = distributionCount;

        for ( int depth = 0; depth < distributionCount; ++depth )
        {
            final int count = array[depth];

            for ( int j = 0; j < count; ++j )
            {
                // final int iter = leafIndex - distributionCount;

                final int leaf         = array[leafIndex++];
                final int target_depth = depth + 1;

                // System.err.println("Adding symbol (0x" + Integer.toHexString(leaf) + ") at depth (" + target_depth + ")...");

                HuffmanNode n    = root.dfs(leaf, target_depth, 0);
                HuffmanNode tn   = n;
                String      path = "";

                while ( true )
                {
                    HuffmanNode p = tn.f_parent;

                    if ( p == null )
                        break;

                    if ( tn == p.f_left )
                        path = "0" + path;
                    else
                        path = "1" + path;

                    tn = p;
                }

                // System.err.println("    Depth " + target_depth + " path to (0x" + Integer.toHexString(leaf) + ") --> " + findLeaf(leaf));
                System.err.println("    Depth " + target_depth + " path to (0x" + Integer.toHexString(leaf) + ") --> " + path);
            }

            System.err.println();
        }
    }


    /**
     * Use carefully!  This method has state, and keeps track of
     * where the hell we've been.  If we make calls to find()
     * without resetting the find (when we mean to find a new
     * value, and not continue the existing search), then we're
     * screwed.
     * <p>
     * To help us, find() will auto-reset once it encounters a
     * leaf node.
     */
    public int find ( int dir )
    {
        // DEBUG! f_pathSave.append(dir);

        HuffmanNode n = current.left();

        if ( dir == 1 )
            n = current.right();

        final int l = n.leaf();

        ++f_depth;

        /*
         * Got the node.  Is this an actual leaf?
         */
        // System.err.println("(" + dir + ") [" + f_depth + "] " + (l >= 0 ? Integer.toString(l) : "."));

        if ( l >= 0 )
        {
            // System.err.println();

            f_pathReset();
        }
        else
        {
            current = n;
        }

        return l;
    }


    /*
     *
     */
    public String findLeaf ( int leaf )
    {
        return root.findLeaf(leaf, -1);
    }


    private void f_pathReset ()
    {
		/*
		f_path = f_pathSave.toString();
		f_pathSave = new StringBuffer();
		*/

        current = root;
        f_depth = 0;
    }


	/*
	 * DEBUG! only
	 *
	public String findPath()
	{
		String s = f_pathSave.toString();

		if ( s.length() == 0 )
			return f_path;

		return s;
	}
	*/


    /**
     * Test driver.
     */
    public static void main ( String[] args )
    {
        final int argc = args.length;

        if ( argc < 2 )
        {
            System.err.println("Usage: java strong.util.HuffmanTree <tree> <path>");
            System.exit(1);
        }

        final int tree_index = Integer.parseInt(args[0]);

        final String path_string = args[1];
        final int    path_length = path_string.length();
        final int[]  path        = new int[path_length];

        for ( int i = 0; i < path_length; ++i )
              path[i] = (path_string.charAt(i) == '0') ? 0 : 1;

        short src[] = null;

        switch ( tree_index )
        {
            case 1:
                src = StaticTree.FIRST_TREE[0];
                break;

            case 2:
                src = StaticTree.FIRST_TREE[1];
                break;

            case 3:
                src = StaticTree.FIRST_TREE[2];
                break;

            case 4:
                src = StaticTree.SECOND_TREE[0];
                break;

            case 5:
                src = StaticTree.SECOND_TREE[1];
                break;

            case 6:
                src = StaticTree.SECOND_TREE[2];
                break;

            default:
                src = StaticTree.FIRST_TREE[0];
        }

        HuffmanTree tree               = new HuffmanTree(src, 16);
        int         leaf               = -1;
        String      actual_path_string = null;

        try
        {
            for ( int i = 0; i < path_length; ++i )
            {
                // DEBUG! actual_path_string = tree.findPath() + ((i == 0) ? "0" : "1");

                leaf = tree.find(path[i]);

                if ( leaf >= 0 )
                    break;
            }

            // DEBUG! System.err.println("path: " + actual_path_string + " --> " + leaf + " (0x" + Integer.toHexString(leaf) + ")");
            System.err.println(leaf + " (0x" + Integer.toHexString(leaf) + ")");
        }
        catch ( Exception e )
        {
            System.err.println("Cannot traverse path: " + path_string);
            System.exit(1);
        }
    }
}


class HuffmanNode
{
    HuffmanNode f_left  = null;
    HuffmanNode f_right = null;
    int         f_leaf  = -1;

    final HuffmanNode f_parent;


    HuffmanNode () { f_parent = null; }
    HuffmanNode ( HuffmanNode parent ) { f_parent = parent; }


    HuffmanNode dfs ( final int value, final int depth, final int d )
    {
        if ( d == depth )
        {
            /*
             * We're here.  If we have children, return.
             * Otherwise, set the f_leaf to value.
             */
            if ( f_left == null && f_right == null && f_leaf < 0 )
            {
                // System.err.println("  Here at depth (" + d + ") adding f_leaf value (0x" + Integer.toHexString(value) + ")");

                f_leaf = value;
                return this;
            }
            else
                return null;
        }
        else if ( f_leaf >= 0 )
            return null;


        if ( f_left == null )
            f_left = new HuffmanNode(this);

        // System.err.println("  Going left...");

        HuffmanNode ln = f_left.dfs(value, depth, d + 1);

        if ( ln != null )
            return ln;

        if ( f_right == null )
            f_right = new HuffmanNode(this);

        // System.err.println("  Going right...");

        HuffmanNode rn = f_right.dfs(value, depth, d + 1);

        return rn;
    }


    int leaf ()
    {
        return f_leaf;
    }


    String findLeaf ( final int leaf, final int fd )
    {
        if ( f_leaf == leaf )
            return f_findLeafString(fd);

        if ( f_left != null )
        {
            String ls = f_left.findLeaf(leaf, 0);

            if ( ls.equals("") != true )
                return f_findLeafString(fd) + ls;
        }

        if ( f_right != null )
        {
            String rs = f_right.findLeaf(leaf, 1);

            if ( rs.equals("") != true )
                return f_findLeafString(fd) + rs;
        }

        return "";
    }


    private String f_findLeafString ( final int fd )
    {
        switch ( fd )
        {
            case -1:
                return "";
            case 0:
                return "0";
            case 1:
                return "1";
        }
        return "";
    }


    HuffmanNode left ()
    {
        return f_left;
    }


    HuffmanNode right ()
    {
        return f_right;
    }


    public String toString ()
    {
        String ls = ((f_left == null) ? "-" : Integer.toString(f_left.f_leaf));
        String rs = ((f_right == null) ? "-" : Integer.toString(f_right.f_leaf));

        return f_leaf + "(" + ls + ", " + rs + ")";
    }
}
