import java.util.Vector;

/**
 * @author Thuan Tran
 *         University of Washing Bothell
 *         CSS 430 Program 4
 *         Sunday May 14,2017
 */
public class Cache
{
    private int bufferSize;
    private int size;
    private Vector<Data> theCache;
    private int pointerIndex; // Used for the second chance algorithm

    /**
     * Constructor for the Cache class that allow
     * how many block to be created and the size of each block
     *
     * @param blockSize   Size of each block
     * @param cacheBlocks Number of block
     */
    public Cache(int blockSize, int cacheBlocks)
    {
        bufferSize = blockSize;
        pointerIndex = 0;
        size = cacheBlocks;
        theCache = new Vector<Data>(size);
        for (int i = 0; i < size; i++)
        {
            // Intialize new empty block
            Data newBlock = new Data(blockSize);
            theCache.add(newBlock);
        }
    }

    /**
     * This method is used to read the data from a block into a buffer
     *
     * @param blockID the block that we need to read data from
     * @param buffer  the buffer that is used to hold the data
     * @return True if the data has been written to the buffer
     */
    public synchronized boolean read(int blockID, byte buffer[])
    {
        // Find corresponding block that has the same block ID
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            // Already in the cache, just read it
            if (theBlock.blockNumber == blockID)
            {
                System.arraycopy(theBlock.buff, 0, buffer, 0, bufferSize);
                theBlock.referenceBit = true;
                return true;
            }

        }
        // If didn't find, find the one that is empty (-1)
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == -1)
            {
                // Get the data from the disk then copy the data to the buffer array
                SysLib.rawread(blockID, theBlock.buff);
                System.arraycopy(theBlock.buff, 0, buffer, 0, bufferSize);
                theBlock.referenceBit = true;
                theBlock.blockNumber = blockID;
                return true;
            }
        }
        // Find the next victim
        int victim = findNextVictim();
        // Update to the disk and read from the disk to the cache
        // Replace that block by the block in the parameter
        if (theCache.get(victim).dirtyBit)
        {
            SysLib.rawwrite(theCache.get(victim).blockNumber, theCache.get(victim).buff);
            theCache.get(victim).dirtyBit = false; // Reset the dirty bit to false
        }
        // read from disk to cache
        SysLib.rawread(blockID, theCache.get(victim).buff);
        // Write from cache to buffer
        System.arraycopy(theCache.get(victim).buff, 0, buffer, 0, bufferSize);
        theCache.get(victim).referenceBit = true; // Just been used
        theCache.get(victim).blockNumber = blockID; // Update the block
        return true;
    }


    /**
     * This method is used to write the data from the buffer into a block
     *
     * @param blockID the block that we need to read data from
     * @param buffer  the buffer that is used to hold the data
     * @return True if the data has been write from the buffer to the block
     */
    public synchronized boolean write(int blockID, byte buffer[])
    {

        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == blockID)
            {
                System.arraycopy(buffer, 0, theBlock.buff, 0, bufferSize);
                theBlock.referenceBit = true;
                theBlock.dirtyBit = true;
                return true;
            }
        }   // If didn't find, fine the one that is empty
        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == -1)
            {
                System.arraycopy(buffer, 0, theBlock.buff, 0, bufferSize);
                theBlock.referenceBit = true;
                theBlock.blockNumber = blockID;
                theBlock.dirtyBit = true;
                return true;
            }
        }
        int victim = findNextVictim();
        Data theBlock = theCache.get(victim);
        if (theBlock.dirtyBit)
        {
            // need to write back to the disk before replacing it
            SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
        }
        System.arraycopy(buffer, 0, theBlock.buff, 0, bufferSize);
        theBlock.blockNumber = blockID;
        theBlock.referenceBit = true;
        theBlock.dirtyBit = true;
        return false;


    }

    /**
     * This method is used to sync
     * It will search for all dirty block and write back the data to the disk
     */
    public synchronized void sync()
    {

        for (int i = 0; i < size; i++)
        {
            Data theBlock = theCache.get(i);
            if (theBlock.blockNumber == -1)
            {
                // Skip empty block since it did not have anything
                // The only case when we flush that have -1 block number is when we first
                // created the block or after we flushed it. Otherwise, its block number should be != -1
                continue;
            }
            if (theBlock.dirtyBit)
            {
                SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
                theBlock.dirtyBit = false;
            }

        }
        SysLib.sync();

    }

    /**
     * This method is used to flush all the data
     * It will write back to the disk all of the information of the dirty bit
     * It will also set the reference bit to false and block number to -1
     */
    public synchronized void flush()
    {
        for (int i = 0; i < size; i++)

        {
            Data theBlock = theCache.get(i);

            if (theBlock.blockNumber == -1)
            {
                // Skip empty block since it did not have anything
                // The only case when we flush that have -1 block number is when we first
                // created the block or after we flushed it. Otherwise, its block number should be != -1
                continue;
            }
            // Write to the Disk
            if (theBlock.dirtyBit)
            {
                SysLib.rawwrite(theBlock.blockNumber, theBlock.buff);
                theBlock.dirtyBit = false;
            }
            theBlock.referenceBit = false;
            theBlock.blockNumber = -1;
        }
        SysLib.sync();
    }

    /**
     * This is the implementation of the enhanced second chance algorithm
     *
     * @return The index of the block we will use in the vector
     */
    private int findNextVictim()
    {
        // This will be used to indicate the second time the algorithm passed

        int loop = 1;
        boolean alreadyPassed = false;
        while (true)
        {

            if (theCache.get(pointerIndex).referenceBit == false && theCache.get(pointerIndex).dirtyBit == false)
            {
                int value = pointerIndex;
                // Update to the next block before return
                pointerIndex = (pointerIndex + 1) % theCache.size();
                return value;
            }
            // if this happen, that means that we have gone through the vector cache 2 whole time
            // but still haven't find. So In this case, we will pick the first 0,1
            if (loop == theCache.size() * 2)
            {
                alreadyPassed = true;
            }
            if (alreadyPassed && theCache.get(pointerIndex).referenceBit == false && theCache.get(pointerIndex).dirtyBit)
            {
                int value = pointerIndex;
                // Use this one temporary and advance the Pointer
                pointerIndex = (pointerIndex + 1) % theCache.size();
                return value;
            }
            // Set this block to be NOT recently used
            theCache.get(pointerIndex).referenceBit = false;
            pointerIndex = (pointerIndex + 1) % theCache.size();
            loop++;
        }
    }

    /**
     * This class is used to represent a block that hold the data
     * The block number (ID). And its bits to determine if it has updated or not
     */
    private class Data
    {
        // Data, ID, being accessed and updated
        private byte[] buff;
        private int blockNumber;
        private boolean referenceBit = false;
        private boolean dirtyBit = false;

        /**
         * The constructor for the Cache Block
         *
         * @param theSize the size of the byte array this block can hold
         */
        public Data(int theSize)
        {
            // Initialize all to default value
            buff = new byte[theSize];
            blockNumber = -1;
            referenceBit = false;
            dirtyBit = false;
        }
    }
}
