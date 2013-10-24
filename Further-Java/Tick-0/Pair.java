package uk.ac.cam.pv273.fjava.tick0;

public class Pair implements Comparable<Pair>
{
    private int payload;
    private int chunkID;
   
    public Pair(int payload, int chunkID)
    {
        this.payload = payload;
        this.chunkID = chunkID;
    }

    public int getPayload() { return this.payload; }
    public int getChunk() { return this.chunkID; }

    public int compareTo(Pair p)
    {
        if (this.payload < p.payload) return -1;
        else if (this.payload > p.payload) return 1;
        return 0;
    }
}
