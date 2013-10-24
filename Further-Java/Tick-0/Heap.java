package uk.ac.cam.pv273.fjava.tick0;

//Prioritetni red implementiran kao min-heap
//Slozenost: O(log N) za Push/Pop, O(1) za Empty/GetMinimum

public class Heap
{

    Pair[] PQ = new Pair[1001];
    int heap_size = 0;

    public boolean Empty()
    {
        return (heap_size == 0);
    }

    public int Size()
    {
        return heap_size;
    }

    public void Push(Pair x)
    {
        PQ[++heap_size] = x;
    	  int pos = heap_size;
        while (pos > 1 && PQ[pos/2].getPayload() > PQ[pos].getPayload())
      	{
            Pair tmp = PQ[pos/2];
            PQ[pos/2] = PQ[pos];
            PQ[pos] = tmp;
          	pos /= 2;
    	  }
    }

    public int GetMinimum()
    {
        return PQ[1].getPayload();
    }

    public Pair Pop()
    {
        Pair topPair = PQ[1];
    	int pos = 1;
        Pair tmp;
        PQ[pos] = PQ[heap_size];
        heap_size--;
    	while (pos <= heap_size)
    	{
            int ret = pos;
            int left = pos*2;
            int right = pos*2+1;
            if (left <= heap_size && PQ[left].getPayload() < PQ[ret].getPayload()) ret = left;
            if (right <= heap_size && PQ[right].getPayload() < PQ[ret].getPayload()) ret = right;
            if (ret != pos)
            {
                tmp = PQ[pos];
                PQ[pos] = PQ[ret];
                PQ[ret] = tmp;
                pos = ret;
            }
            else break;
        }
        return topPair;
    }
}
