

public class Shell extends Thread
{

   public Shell()
   {

   }
   public void run()
   {
   	int status = 1;
   	while(true)
	{
		String command = "";
		SysLib.cout ( "shell[" + status + "]%" );
		SysLib.cin(command);
		String[] result = SysLib.stringToArgs( command);
		for (String s : result)
		{

		}


	}
   }

}
