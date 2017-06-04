
/*
 The main file system class that ties everything together and its how you declare a FileSystem
 (FileSystem fs )
 */

public class FileSystem
{
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem(int diskBlocks)
    {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock(diskBlocks);

        // create directory, and register "/" in directory entry 0
        directory = new Directory(superblock.totalInodes);

        // file table is created, and store directory in the file table
        filetable = new FileTable(directory);

        // directory reconstruction
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }

        close(dirEnt);

    }


    /**
     * - allocates  new file descriptor fd to this file.
     * <p>
     * - file not exist in modes: “w”, “w+” or “a” 	-> file is created
     * - file not exist in mode: “r”				-> error -> return negative number
     * <p>
     * - file descriptor 0, 1 and 2 are reserved as standard input, output, and error
     * - therefore, newly opened file must receive a new descriptor numbered 3 <= fd <= 31
     * - if calling thread’s user file descriptor table is full, return error of negative number
     * <p>
     * seek pointer -> initialized to zero -> mode: “r”, “w”, and “w+”
     * -> initialized at the end of the file -> mode: “a”
     *
     * @param fileaname the name of the file to open
     * @param mode      The mode to open the file
     * @return a File Table Entry that correspond to the file with the given mode
     */

    public FileTableEntry open(String filename, String mode)
    {
        // Not in one of these mode => invalid mode
        if (!mode.equals("a"))
        {
            if (!mode.equals("w"))
            {
                if (!mode.equals("w+"))
                {
                    if (!mode.equals("r"))
                        return null; // invalid entry
                }
            }
        }
        // Allocate the file in the File Table
        // Check if it is in write mode or not, if it is then we need to deallocate the block at the File Table Entry
        FileTableEntry retVal = filetable.falloc(filename, mode);
        if (mode.equals("w") && !deallocAllBlocks(retVal))
        {
            return null;
        }
        return retVal;
    }


    /**
     * - reads up to buffer.length bytes from the file indicated by fd -> starting at the position currently pointed to by the seek pointer
     * <p>
     * if (bytes remaining between current seek pointer and end of file are less than buffer.length) {
     * - SysLib.read reads as many bytes as possible	-> put them to beginning of buffer
     * - increments the seek pointer by the number of bytes to have been read
     * - return: number of bytes that have been read		OR negative error value
     * }
     *
     * @param ftEnt the FileTableEntry to read from
     * @return buffer the buffer to read the data into
     */

    public int read(FileTableEntry ftEnt, byte[] buffer)
    {

        int sizeLeftToRead = 0;
        int trackDataRead = 0;
        int size = buffer.length;
        // Could not read if the File Table Entry has mode write or append
        if (ftEnt.mode.equals("w") || ftEnt.mode.equals("a")) return -1;
        // Check for invalid passed in parameter
        if (buffer == null || buffer.length < 1)
        {
            return -1;
        }

        synchronized (ftEnt)
        {
            // Only stop when the seek pointer is still within the range
            // And the buffer still have place to read data into
            while (ftEnt.seekPtr < fsize(ftEnt) && buffer.length > 0)
            {

                // FIND BLOCK NUMBER
                int blockNum = ftEnt.inode.getBlockNumPointer(ftEnt.seekPtr);
                //
                if (blockNum != -1)
                {

                    byte[] tempRead = new byte[512];
                    // Know the block location to read from, now load the data from disk
                    SysLib.rawread(blockNum, buffer);

                    // How far we go itno
                    int dataGetInto = ftEnt.seekPtr % 512;
                    int remainingBlocks = 512 - dataGetInto;
                    int remaining = fsize(ftEnt) - ftEnt.seekPtr;


                    int smallerBetweenBlockandData = Math.min(remainingBlocks, size);
                    // Check to see how much left we can read versus the size remaining
                    sizeLeftToRead = Math.min(smallerBetweenBlockandData, remaining, )


                    System.arraycopy(tempRead, dataGetInto, buffer, trackDataRead, sizeLeftToRead);
                    // Update the varaible to read into the byte array
                    trackDataRead += sizeLeftToRead;
                    // Update the Seek Pointer to read at new position
                    ftEnt.seekPtr += sizeLeftToRead;
                    // Update the size total.
                    size -= sizeLeftToRead;
                } else
                {
                    // Invalid block location
                    break;
                }
                return trackDataRead;
            }

        }
        // Default return value, if reached here, then no success
        return -1;


    }


    /**
     * This method sync the data from the directory back to the disk
     *
     * @param none
     * @return none
     */
    public void sync()
    {
        // Get all the information from the directory first, inluding all files name and size
        byte[] temp = directory.directory2bytes();
        // Open the Table Entry that correspond to the directory
        FileTableEntry tempEntry = open("/", "w");
        // Write back to the disk all the info from the directory
        write(tempEntry, temp);
        close(tempEntry);
        // Write back to the disk all the info
        superblock.sync();

    }


    /**
     * - closes corresponding to fd
     * - commits all file transactions o this file
     * - unregisters fd from the user file descriptor table of the calling thread’s TCB
     * - return value is 0 in success, otherwise -1
     *
     * @param ftEnt The File Table Entry that need to be closed
     * @return a boolean avarialble that indicate if we successfully close or not
     */
    boolean close(FileTableEntry ftEnt)
    {
        // Cant close an empty variable
        if (ftEnt == null)
        {
            return false;
        }
        synchronized (ftEnt)
        {
            ftEnt.count--;
            if (ftEnt.count <= 0)
            {
                return filetable.ffree(ftEnt);
            }


            return true;
        }
    }


    /**
     * - writes the contents of buffer to the file indicated by fd	->	starting at the position indicated by the seek pointer
     * - operation might overwrite existing data in the file and/ or append to the end of the file.
     * - SysLib.write increments the seek pointer by the number of bytes to have been written
     * - return value is the number of bytes that have been written	OR negative error value
     *
     * @param entry  The FileTableEntry that we want to write the data into
     * @param buffer the buffer that has the data that need to be rewritten
     */
    public int write(FileTableEntry entry, byte[] buffer)
    {
        // Check for invalid passed in parameter
        if (entry == null || buffer == null)
        {
            return -1;
        }

        if (entry.mode == "r")
        {
            return -1;
        }
        synchronized (entry)
        {
            int offset = 0;
            int size = buffer.length;

            while (size > 0)
            {
                int var6 = entry.inode.getBlockNumPointer(entry.seekPtr);
                if (var6 == -1)
                {
                    short var7 = (short) this.superblock.findFreeBlock();
                    switch (entry.inode.registerTargetBlock(entry.seekPtr, var7))
                    {
                        case -3:
                            short var8 = (short) this.superblock.getFreeBlock();
                            if (!entry.inode.registerIndexBlock(var8))
                            {
                                SysLib.cerr("ThreadOS: panic on write\n");
                                return -1;
                            }

                            if (entry.inode.registerTargetBlock(entry.seekPtr, var7) != 0)
                            {
                                SysLib.cerr("ThreadOS: panic on write\n");
                                return -1;
                            }
                        case 0:
                        default:
                            var6 = var7;
                            break;
                        case -2:
                        case -1:
                            SysLib.cerr("ThreadOS: filesystem panic on write\n");
                            return -1;
                    }
                }

                byte[] var13 = new byte[512];
                if (SysLib.rawread(var6, var13) == -1)
                {
                    System.exit(2);
                }

                int var14 = entry.seekPtr % 512;
                int var9 = 512 - var14;
                int var10 = Math.min(var9, size);
                System.arraycopy(buffer, offset, var13, var14, var10);
                SysLib.rawwrite(var6, var13);
                entry.seekPtr += var10;
                offset += var10;
                size -= var10;
                if (entry.seekPtr > entry.inode.length)
                {
                    entry.inode.length = entry.seekPtr;
                }
            }

            entry.inode.toDisk(entry.iNumber);
            return offset;
        }

    }


    /**
     * This method deallocate all the block that is pointed to in the passed in File Table Entry
     *
     * @param ftEnt The File Table Entry that need to be deallocate
     * @return a boolean variable that indicate success or not
     */
    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {
        // Invalid parameter
        if (ftEnt == null)
        {
            return false;
        }
        // Something is using it
        if (ftEnt.count > 0)
        {
            return false;
        }
        // Deallocate the indirect block
        byte[] data;
        int indirectStatus = ftEnt.inode.indirect;
        if (indirectStatus != -1)
        {
            data = new byte[512];
            SysLib.rawread(indirectStatus, data);
            ftEnt.inode.indirect = -1;
        } else
        {
            data = null;
        }
        if (data != null)
        {
            byte offset = 0;
            // Get all the block that is pointed to by the indirect block
            short blockID SysLib.bytes2short(data, offset);
            // And make it free. Let it go, let it go
            while (blockID != -1)
            {
                superblock.addFreeBlock(blockID);
                blockID = SysLib.bytes2short(data, offset);
            }
        }

        // Since each iNode can only have 11 pointer; Free all the block that is pointed to
        for (short blockIndex = 0; blockIndex < 11; blockIndex++)
        {
            superblock.addFreeBlock(ftEnt.inode.direct[blockIndex]);
            // Indicate that the block at this direct block is invalid
            ftEnt.inode.direct[blockIndex] = -1;
        }

        ftEnt.inode.toDisk(ftEnt.iNumber);
        return true;


    }


    /**
     * - deletes the file specified by fileName
     * - all blocks used by file is freed.
     * if ( file is open ) {
     * it is not deleted
     * return -1
     * }
     * - successfully delete = 0
     *
     * @param filename the Name of the file to be deleted
     * @return a boolean variable indicating if delete success or not
     */

    public boolean delete(String filename)
    {
        // Need to get the File Table Entry that has the file
        // Then get the i number which in turn point to the block that
        // need to be delete
        // Need to make sure that the file is close and free
        FileTableEntry corresponding = open(filename, 'w');
        short number = corresponding.iNumber;
        boolean closeSuccess = close(corresponding);
        boolean freeSuccess = directory.ifree(number);
        if (closeSuccess && freeSuccess)
        {
            return true;
        }
        return false;


    }


    /**
     * updates the seek pointer corresponding to fd as follows:
     * - whence == SEEK_SET(= 0), file’s seek pointer is set to offset bytes from the beginning of the file
     * - whence == SEEK_CUR(= 1), file’s seek pointer is set to its current value plus the offset, The offset can be (+) or (-)
     * - whence == SEEK_END(= 2), file’s seek pointer is set to the size of the file plus the offset, The offset can be (+) or (-)
     *
     * @param ftEnt  The File Table Entry to seek the data
     * @param offset How far into it that we want to read
     * @param whence The Mode to read
     * @return an integer indicating success or not
     */


    public int seek(FileTableEntry ftEnt, int offset, int whence)
    {
        // Invalid mode to set the pointer, can only be 1,2,3
        if (whence != 0 && whence != 1 && whence != 2)
        {
            return -1;
        }

        synchronized (ftEnt)
        {
            if (ftEnt == null) return -1;

            if (whence == SEEK_SET)
            {
                if (offset <= fsize(ftEnt))
                {
                    ftEnt.seekPtr = offset;

                }

            } else if (whence == SEEK_CUR)
            {
                if (ftEnt.seekPtr + offset <= fsize(ftEnt) && (ftEnt.seekPtr + offset >= 0))

                {
                    ftEnt.seekPtr += offset;
                }

            } else if (whence == SEEK_END)
            {

                if (fsize(ftEnt) + offset >= 0 && fsize(ftEnt) + offset <= fsize(ftEnt))
                {


                    ftEnt.seekPtr = fsize(ftEnt) + offset;
                } else
                {
                    return -1
                }
            } else
            {
                return -1;
            }


            return ftEnt.seekPtr;
        }

    }


    /**
     * files: max number of files to be created (number of inodes to be allocated) in the file system
     *
     * @param files Number of files to be formated
     * @return a boolean variable that indicate the successful of format
     */
    public boolean format(int files)
    {

        superblock.format(files);
        // Create a new instance of Directory and FileTable
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        return true;
    }

    /**
     * // - returns the size in bytes of the file indicated by fd
     *
     * @param ftEnt the File Table Entry
     * @return an integer value representing the file size
     */
    public int fsize(FileTableEntry ftEnt)
    {

        synchronized (ftEnt)
        {
            if (ftEnt == null)
            {
                return -1;
            }


            return ftEnt.inode.length;
        }
    }
