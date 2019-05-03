package com.bwssystems.HABridge.util;

import java.io.*;

/**
 * This class is an auto-resizable String array. It has similar methods to ArrayList
 *
 * @author Henry Zheng
 * @url http://www.ireasoning.com
 */

public class StringArray implements Serializable
{
    public static final long serialVersionUID = 42L;
    public static final int DEFAULT_CAPACITY = 10;

    protected String[] _strings = null;
    protected int _upperBound = 0;
    protected int _capacity = DEFAULT_CAPACITY;
    protected int _initialSize = _capacity;
    protected float _loadFactory = 1.5F;

    public StringArray ()
    {
        this(DEFAULT_CAPACITY);
    }

    public StringArray( int size)
    {
        _capacity = size;
        _initialSize = size;
        _strings = new String[size];
    }
    
    public synchronized void ensureCapacity(int capacity)
    {
        if(_capacity < capacity)
        {
            _capacity = (_capacity * 3)/2 + 1;
            if(_capacity < capacity)
            {
                _capacity = capacity;
            }
            String [] oldData = _strings;
            _strings = new String[_capacity];
            System.arraycopy(oldData, 0, _strings, 0, _upperBound);
        }
    }


    public synchronized void add(String s)
    {
        if(_upperBound == _capacity )
        {
            resize((int) (_capacity * _loadFactory));
        }
        _strings[_upperBound++] = s;
    }

    public synchronized void add(StringArray sa)
    {
        for (int i = 0; i < sa.size() ; i++) 
        {
            add(sa.get(i));
        }
    }
    
    public synchronized String get(int index)
    {
        return _strings[index];
    }

    public synchronized void set(int index, String newVal)
    {
        _strings[index] = newVal;
    }
    
    /** Adds all elements in passed string array */
    public synchronized void add(String [] strs)
    {
        for (int i = 0; i < strs.length ; i++) 
        {
            add(strs[i]);
        }
    }
    
    /** Resets this object. */
    public synchronized void clear()
    {
        _capacity = _initialSize;
        _strings = new String[_capacity];
        _upperBound = 0;

    }

    public synchronized String remove(int index)
    {
        if(index >= _upperBound )
        {
            throw new IndexOutOfBoundsException();
        }
        String s = _strings[index];
        for (int i = index; i < _upperBound - 1  ; i++)
        {
            _strings[i] = _strings[i + 1];
        }
        _upperBound --;
        return s;
    }

    /**
     * Removes the first occurance of passed str
     * @return the string removed, or null if not found
     */
    public synchronized String remove(String str)
    {
        for (int i = 0; i < _upperBound   ; i++)
        {
            if(_strings[i].equals(str))
            {
                return remove(i);
            }
        }
        return null;
    }
    
    public synchronized int size()
    {
        return _upperBound;
    }

    public synchronized boolean isEmpty()
    {
        return _upperBound == 0;    
    }
    
    public synchronized String[] toArray()
    {
        String [] ret = new String[_upperBound];
        for (int i = 0; i < _upperBound ; i++)
        {
            ret[i] = _strings[i];
        }
        return ret;
    }

    protected synchronized void resize(int newCapacity)
    {
        String [] as = new String[newCapacity];
        for (int i = 0; i < _strings.length ; i++)
        {
            as[i] = _strings[i];
        }
        _strings = as;
        _capacity = newCapacity;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < _upperBound ; i++)
        {
            buf.append(_strings[i] + "\n");
        }
        return buf.toString();
    }

    public static void main(String[] args)
    {
         StringArray as = new StringArray();
         String [] ss = null;
         ss = as.toArray();
         // System.out.println( "ss len="+ss.length);
         // System.out.println( "ss = " + ss);
         for (int i = 0; i < 10 ; i++)
         {
             as.add("" + i);
         }
         // System.out.println( "size = " + as.size());
         ss = as.toArray();
         for (int i = 0; i < ss.length ; i++)
         {
             // System.out.println( ss[i]);
         }
         // System.out.println( "remove 5th element.");
         as.remove(5);

         // System.out.println( "size = " + as.size());
         ss = as.toArray();
         for (int i = 0; i < ss.length ; i++)
         {
             // System.out.println( ss[i]);
         }
    }
}//end of class StringArray