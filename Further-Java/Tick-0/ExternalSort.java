package uk.ac.cam.pv273.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.PriorityQueue;

public class ExternalSort
{
    static int chunkSize = 1000000;
    static final int chunkSize2 = 500000;
    static final int chunkSizeInBytes = 16384;
    static boolean two = false;

    static int outChunkSizeInBytes = 102400;

    static int[] chunk;
    static int iterator;
    static int chunkCount;
    static Heap heap;
    static RandomAccessFile fileA1, fileA2;
    static RandomAccessFile fileB2;
    static FileInputStream Ain;
    static FileOutputStream Aout;
    static FileOutputStream Bout;
    static int N;
    static int numChunks;

    static int previousSeek = 0;
    static ChunkInfo chnks[] = new ChunkInfo[500];
    static int count[] = new int[500];
    static int[] cnt2 = new int[1<<16];

    static boolean mkay;
    
    static boolean ok = true;
    static boolean ok2 = false;
    static int previousNumber = -2147483648; // MIN_INT
    static int nextNumber = 2147483647; // MAX_INT
    static int totalNums = 0;

    static int[] t;
    static int heap_size;

    static byte[] buff = new byte[chunkSizeInBytes];
    static int buffSize = 0;
    static int buffIterator = 0;

    static byte[] outBuff = new byte[outChunkSizeInBytes];
    static int outBuffSize = 0;
    static int outBuffIterator = 0;

    static byte[] buff2 = new byte[outChunkSizeInBytes];
    static int buffSize2 = 0;
    static int buffIterator2 = 0;

    public static int[] buffAux = new int[32768];
    public static int buffIteratorAux = 0;

    private static void fread() throws IOException
    {
        buffIterator = 0;
        buffSize = Ain.read(buff);
    }

    private static int readByte() throws IOException, EOFException
    {
        if (buffIterator == buffSize)
        {
            fread();
            if (buffSize == -1) throw new EOFException();
        }
        return (int)(buff[buffIterator++] & 0xFF);
    }

    private static int readInteger() throws IOException, EOFException
    {
        return (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
    }

    private static void initializeBufferForWrites()
    {
        outBuffIterator = 0;
        outBuffSize = outChunkSizeInBytes;
    }

    private static void flushBuffer() throws IOException
    {
        Aout.write(outBuff, 0, outBuffIterator);
        outBuffIterator = 0;
    }

    private static void writeByte(int b) throws IOException
    {
        if (outBuffIterator == outBuffSize) flushBuffer();
        outBuff[outBuffIterator++] = (byte)b;
    }

    private static void writeInteger(int x) throws IOException
    {
        writeByte((x >> 24) & 0xFF);
        writeByte((x >> 16) & 0xFF);
        writeByte((x >> 8) & 0xFF);
        writeByte(x & 0xFF);
    }

    private static void initializeBufferForWrites2()
    {
        buffIterator2 = 0;
        buffSize2 = outChunkSizeInBytes;
    }

    private static void flushBuffer2() throws IOException
    {
        Bout.write(buff2, 0, buffIterator2);
        buffIterator2 = 0;
    }

    private static void writeByte2(int b) throws IOException
    {
        if (buffIterator2 == buffSize2) flushBuffer2();
        buff2[buffIterator2++] = (byte)b;
    }

    private static void writeInteger2(int x) throws IOException
    {
        writeByte2((x >> 24) & 0xFF);
        writeByte2((x >> 16) & 0xFF);
        writeByte2((x >> 8) & 0xFF);
        writeByte2(x & 0xFF);
    }

    private static void epiclyAwesomeRadixSort()
    {
        final int d = 16;
        final int w = 32;
        
        int[] cnt = new int[1<<d];
        for (int i=0;i<iterator;i++)
        {
            ++cnt[(chunk[i] ^ Integer.MIN_VALUE) & ((1 << d) - 1)];
        }
        for (int i=1;i<1<<d;i++)
        {
            cnt[i] += cnt[i-1];
        }
        for (int i=iterator-1;i>=0;i--)
        {
            t[--cnt[(chunk[i] ^ Integer.MIN_VALUE) & ((1 << d) - 1)]] = chunk[i];
        }
        
        cnt = new int[1<<d];
        for (int i=0;i<iterator;i++)
        {
            ++cnt[((t[i] ^ Integer.MIN_VALUE) >>> d) & ((1 << d) - 1)];
        }
        for (int i=1;i<1<<d;i++)
        {
            cnt[i] += cnt[i-1];
        }
        for (int i=iterator-1;i>=0;i--)
        {
            chunk[--cnt[((t[i] ^ Integer.MIN_VALUE) >>> d) & ((1 << d) - 1)]] = t[i];
        }
    }

    private static void radixSort2()
    {
        final int d = 16;
        final int w = 32;
        for (int p=0;p<w/d;p++)
        {
	    int cnt[] = new int[1<<d];
            for (int i=0;i<iterator;i++)
            {
                ++cnt[((chunk[i] ^ Integer.MIN_VALUE) >>> d*p) & ((1 << d) - 1)];
            }
            for (int i=1;i<1<<d;i++)
            {
                cnt[i] += cnt[i-1];
            }
            for (int i=iterator-1;i>=0;i--)
            {
                t[--cnt[((chunk[i] ^ Integer.MIN_VALUE) >>> d*p) & ((1 << d) - 1)]] = chunk[i];
            }
            System.arraycopy(t,0,chunk,0,iterator);
        }
    }

    public static void countingSort()
    {
    	for (int i = 0; i < iterator; i++) 
        {
	    //System.out.println("chunk[i] = " + chunk[i] + ", nextNumber = " + nextNumber);
      	    ++cnt2[chunk[i] - nextNumber];
    	}
	int len = previousNumber - nextNumber + 1;
	int iter2 = 0;
	for (int i = 0; i < len; i++)
	{
	    while (cnt2[i]-- > 0) chunk[iter2++] = i + nextNumber;
	}
    }

    static void swap(int i, int j) 
    {
        int t = chunk[j];
        chunk[j] = chunk[i];
        chunk[i] = t;
    }
    
    static void Heapify(int pos)
    {
        if (pos > heap_size) return;
        int ret = pos;
        int left = pos*2;
        int right = pos*2+1;
        if (left <= heap_size && chunk[left-1] > chunk[ret-1]) ret = left;
        if (right <= heap_size && chunk[right-1] > chunk[ret-1]) ret = right;
        if (ret != pos)
        {
            swap(pos-1, ret-1);
            Heapify(ret);
        }
    }

    static void Pop()
    {
        int pos = 1;
        swap(pos-1, heap_size-1);
	heap_size--;
        while (pos <= heap_size)
        {
            int ret = pos;
            int left = pos*2;
            int right = pos*2+1;
            if (left <= heap_size && chunk[left-1] > chunk[ret-1]) ret = left;
            if (right <= heap_size && chunk[right-1] > chunk[ret-1]) ret = right;
            if (ret != pos)
            {
                swap(pos-1, ret-1);
                pos = ret;
            }
            else break;
        }
    }

    static void heapSort()
    {
        heap_size = iterator;
        for (int i=iterator/2;i>=1;i--) Heapify(i);
        while (heap_size > 1) Pop();
    }

    private static void processChunk(String f2) throws IOException
    {
	if (!mkay) 
	{
	    if (two) radixSort2();
	    else heapSort(); //epiclyAwesomeRadixSort(); //radixSort2();
	}
	initializeBufferForWrites2();
	buffIteratorAux = 0;
	int nextSeek = previousSeek;
        for (int i=0;i<iterator;i++)
      	{
	    if (i < 4097)
	    {
		if (i > 0)
		{
		    buffAux[buffIteratorAux++] = ((chunk[i] >> 24) & 0xFF);
		    buffAux[buffIteratorAux++] = ((chunk[i] >> 16) & 0xFF);
		    buffAux[buffIteratorAux++] = ((chunk[i] >> 8) & 0xFF);
		    buffAux[buffIteratorAux++] = (chunk[i] & 0xFF);
		}
	    }
            else writeInteger2(chunk[i]);
        }
        flushBuffer2();    
	chnks[chunkCount] = new ChunkInfo(0, iterator, new RandomAccessFile(f2, "rw"), previousSeek, numChunks);
        previousSeek += 4 * (iterator - 4097);
        heap.Push(new Pair(chunk[0], chunkCount));
        chunkCount++;
    }

    private static void processOneChunk() throws IOException
    {
	initializeBufferForWrites();
        heapSort(); // epiclyAwesomeRadixSort(); //radixSort2();
        for (int i=0;i<iterator;i++)
        {
            writeInteger(chunk[i]);
        }
        ok2 = true;
    }
    
    private static int putInBucket(int x)
    {
        if (x >= 0) return (1 << 15) + (x >> 16);
        else return (1 << 15) - ((-(++x)) >> 16) - 1;
    }
	
    /*
     * Idea: Partition the input into chunks, sort each chunk separately.
     *       Write the sorted chunks into file B.
     *       Put all the chunk heads into a heap, and every time an element is removed, increment
     *         the chunk's respective iterator.
     *       Write all removed files into file A.
     */
    public static void sort(String f1, String f2) throws FileNotFoundException, IOException
    {
        //Initializing RandomAccessFile and Data Input/Output streams.
        fileA1 = new RandomAccessFile(f1, "rw");
        fileA2 = new RandomAccessFile(f1, "rw");
        fileB2 = new RandomAccessFile(f2, "rw");

        Ain = new FileInputStream(fileA1.getFD());
        Aout = new FileOutputStream(fileA2.getFD());
        Bout = new FileOutputStream(fileB2.getFD());

        //Getting the amount of chunks needed.
        N = ((int)fileA1.length() >> 2);
        if (N != 1000000)
	{
	    chunk = new int[chunkSize2];
	    t = new int[chunkSize2];
	    two = true;
	    chunkSize = chunkSize2;
	}
	else chunk = new int[chunkSize];
        numChunks = N / chunkSize + N % chunkSize;

        //Defining the priority queue.
        heap = new Heap();

        //Initializations
        chunkCount = 0;
        iterator = 0;

        //Input.
        try
        {
            while (true)
            {
		//startTime = System.nanoTime();
		previousNumber = -2147483648;
    		nextNumber = 2147483647;
                iterator = 0;
		mkay = true;
                while (iterator < chunkSize)
                {
                    chunk[iterator] = readInteger();
                    if (chunk[iterator] >= 0 && chunk[iterator] <= 300) count[chunk[iterator]]++;
                    else ok = false;
                    
		    if (chunk[iterator] >= previousNumber) previousNumber = chunk[iterator];
		    else mkay = false;
                    
                    ++iterator;
                }
		//endTime = System.nanoTime();
		//timeSpentReadingAndWriting += endTime - startTime;
                if (!ok) { if (!two) { processOneChunk(); break; } else processChunk(f2); }
            }
        }
        catch (EOFException eof)
        {
	    initializeBufferForWrites();
            if (iterator > 0)
            {
                if (!ok) 
                {
                    if (chunkCount == 0) processOneChunk();
                    else processChunk(f2);
                }
            }
        }
        finally
        {
	    //startTime = System.nanoTime();
            if (ok)
            {
                for (int i=0;i<=300;i++)
                {
                    while (count[i]-- > 0) writeInteger(i);
                }
            }
            else if (!ok2)
            {
            	if (chunkCount != 0)
            	{
                    Bout.flush();
                    while (heap.Size() > 1)
                    {
			
                  	    Pair top = heap.Pop();
                        writeInteger(top.getPayload());
                        int idd = top.getChunk();
                        int pozz = chnks[idd].getPos() + 1;
                        chnks[idd].incPos();
                        int sajz = chnks[idd].getSiz();
                        int nextMin = heap.GetMinimum();
                        while (pozz < sajz)
                        {
			    //long startT2 = System.nanoTime();
                            int cur = chnks[idd].readInt();
			    //timeSpentMergingHeap += System.nanoTime() - startT2;
                            if (cur <= nextMin)
                            {
			        //startT2 = System.nanoTime();
                                writeInteger(cur);
				//timeSpentMergingHeap += System.nanoTime() - startT2;
                                pozz++;
                                chnks[idd].incPos();
                            }
                            else
                            {
                                heap.Push(new Pair(cur, idd));
                                break;
                            }
                        }
                	}
                    Pair top = heap.Pop();
                    writeInteger(top.getPayload());
                    int idd = top.getChunk();
                    int pozz = chnks[idd].getPos() + 1;
                    int sajz = chnks[idd].getSiz();
                    while (pozz < sajz)
                    {
			//long startT2 = System.nanoTime();
                        writeInteger(chnks[idd].readInt());
			//timeSpentMergingHeap += System.nanoTime() - startT2;
                        pozz++;
                    }
            	}
            }
        }
        //long startT3 = System.nanoTime();
        flushBuffer();
        //Aout.flush();
	//timeSpentMerging += endTime - startTime;
	//timeSpentMergingHeap += endTime - startT3;

	//System.out.println("Time spent reading and writing: " + (timeSpentReadingAndWriting / 1000000));
	//System.out.println("Time spent sorting chunks: " + (timeSpentSorting / 1000000));
	//System.out.println("Time spent merging chunks: " + (timeSpentMerging / 1000000));
	//System.out.println("Time spent reading and writing: " + (timeSpentMergingHeap / 1000000));
    }

    private static String byteToHex(byte b) 
    {
        String r = Integer.toHexString(b);
        if (r.length() == 8)
        {
            return r.substring(6);
        }
        return r;
    }

    public static String checkSum(String f) 
    {
        try 
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream ds = new DigestInputStream(new FileInputStream(f), md);
            byte[] b = new byte[512];
            while (ds.read(b) != -1);
            String computed = "";
            for(byte v : md.digest()) computed += byteToHex(v);
            return computed;
        }
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return "<error computing checksum>";
    }

    public static void main(String[] args) throws Exception 
    {
        String f1 = args[0];
        String f2 = args[1];
        sort(f1, f2);
        System.out.println("The checksum is: "+checkSum(f1));
    }
}
