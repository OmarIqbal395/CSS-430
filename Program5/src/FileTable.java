import java.util.Vector;

/**
 * @author Thuan Tran
 *         CSS 430 Final Project
 *         May 31th, 2017
 */
public class FileTable
{
    private final int SPECIAL_CASE = 2;

    private Vector table;         // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable(Directory directory)
    { // constructor
        table = new Vector();     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    /**
     * This method will allocate a table Entry (System wide) for a file, with different mod
     *
     * @param filename The name of the file
     * @param mode     The mode to access the file
     * @return The table entry, or null if there is no file and the system want to read it
     */
    public synchronized FileTableEntry falloc(String filename, String mode)
    {
        // If invalid parameter passed in then return null;
        if (filename == null || filename.length() == 0)
        {
            return null;
        }
        if (!(mode.equals("r") || mode.equals("w") || mode.equals("w+") || mode.equals("a")))
        {
            return null;
        }
        // Apple product, must have costs a ton (iNode)
        Inode newInode = null;

        short iNumber = 0;
        // Since we do not know when we will be able to access the file, must be in an infinite loop
        while (true)
        {
            // Get the corresponding number for the iNode in the directory
            iNumber = dir.namei(filename);
            // If we do not have the node, then we need to create it

            if (iNumber < 0)
            {
                // Will create a new inode when the system want to write to it
                if (!(mode.equals("r")))
                {
                    short createOnDirectory = dir.ialloc(filename);
                    newInode = new Inode(createOnDirectory);
                    newInode.flag = SPECIAL_CASE;
                    break;
                } else
                {
                    return null;
                }
            } else
            {


                if (iNumber >= 0)
                {
                    newInode = new Inode(iNumber);
                    // If we are trying to read the file and something is happening on the file
                    if (mode.equals("r"))
                    {
                        // The node is available
                        if (newInode.flag == 1 || newInode.flag == 0)
                        {
                            // Change the status of the file to being used
                            newInode.flag = 1;
                            break;

                        }
                        // The node is doing something else, need to wait for it until it changed
                        else
                        {
                            if (newInode.flag == SPECIAL_CASE)
                            {
                                try
                                {
                                    wait();
                                } catch (InterruptedException except)
                                {
                                    SysLib.cout("File is in special condition and error hapened");
                                }
                            }
                        }
                    }
                    // We are trying to write or append
                    else
                    {
                        if (newInode.flag == 0 || newInode.flag == 1)
                        {
                            // Indicate special case
                            newInode.flag = SPECIAL_CASE;
                            break;
                        }
                        // Wait for the node in special cases
                        else
                        {
                            try
                            {
                                wait();

                            } catch (InterruptedException except)
                            {
                                SysLib.cout("File is in special condition");
                            }
                        }
                    }


                }
            }
            // Couldn't find a node => new file

        }
        newInode.count++;
        newInode.toDisk(iNumber);
        FileTableEntry newEntry = new FileTableEntry(newInode, iNumber, mode);
        this.table.addElement(newEntry);
        return newEntry;
    }

    /**
     * This method free a file table entry in the File Table. It will make
     * sure to write back the data to the disk
     *
     * @param e the FileTableEntry that we need to free
     * @return a boolean value: True if we found the element and freed it, false if the the parameter is
     * invalid or we could not find it
     */
    public synchronized boolean ffree(FileTableEntry e)
    {
        // If invalid parameter or the table does not have the file, return false
        if (e == null || !table.contains(e))
        {
            return false;
        }
        // Get the entry from the vector
        FileTableEntry theEntry = (FileTableEntry) table.get(table.indexOf((FileTableEntry) e));
        Inode theNode = theEntry.inode;
        //write back to the disk and remove it from the table
        theNode.toDisk(theEntry.iNumber);
        table.remove(e);
        return true;
    }

    /**
     * This method check if the table is empty
     *
     * @return True if the table is empty
     */
    public synchronized boolean fempty()
    {
        return table.isEmpty();  // return if table is empty
    }                            // should be called before starting a format
}