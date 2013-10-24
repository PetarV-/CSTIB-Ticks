package uk.ac.cam.pv273.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.lang.Math;

public class ChunkInfo
{
    private static final int availableMemory = 16384;

    private int pos;
    private int siz;
    private RandomAccessFile raf;
    private FileInputStream ds;
    private int allocatedMemory;
    private ByteBuffer buff2;

    public byte[] buff;
    public int buffSize = 0;
    public int buffIterator = 0;

    private void fread() throws IOException
    {
        buffIterator = 0;
        int lenToRead = Math.min(allocatedMemory, ((this.siz - this.pos) << 2));
	buffSize = ds.read(buff, 0, lenToRead); 
    }

    private int readByte() throws IOException, EOFException
    {
        if (buffIterator >= buffSize)
        {
            fread();
        }
        return (int)(buff[buffIterator++] & 0xFF);
    }

    private int readInteger() throws IOException, EOFException
    {
        return (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
    }
    
    public ChunkInfo(int pos, int siz, RandomAccessFile raf, int initSeek, int numChunks) throws IOException
    {
        this.pos = pos;
        this.siz = siz;
        this.raf = raf;
        this.raf.seek(initSeek);
        this.ds = new FileInputStream(raf.getFD());
	this.allocatedMemory = 16384;
        buff = new byte[allocatedMemory];
        buffSize = ExternalSort.buffIteratorAux; 
        for (int i=0;i<buffSize;i++)
        {
            buff[i] = (byte)ExternalSort.buffAux[i];
	}
    }

    public int getPos() { return pos; }
    public void incPos() { pos++; }
    public void setPos(int p) { pos = p; }
    public int getSiz() { return siz; }
    public int readInt() throws IOException { return readInteger(); }
}
