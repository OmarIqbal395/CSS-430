/*
	CSS 430 (Operating Systems) Homework 1 Part 2
	Implement a Shell for the simulated OS. The Shell should manages concurrent and sequential commands
	@author Thuan Tran
	@date April 7th,2017
 */


import java.util.HashSet;
import java.util.Set;

public class Shell extends Thread
{
	// This variable will be used to act as the log file for the concurrent processess
	Set< Integer > keeper;

	public Shell ( )
	{
		keeper = new HashSet<> ( );
	}

	public void run ( )
	{
		// Used to indicate how much statements have passed
		int status = 1;
		while ( true )
		{
			StringBuffer input = new StringBuffer ( );
			String command = "";
			SysLib.cout ( "shell[" + status + "]% " );
			SysLib.cin ( input );
			status++;
			// Parse the string from key board input
			command = new String ( input );
			String[] result = SysLib.stringToArgs ( command );
			if ( result.length == 1 && result[ 0 ].equals ( "exit" ) )
			{
				// check for key wold to exit

				break;
			} else
			{
				if ( result.length < 1 )
				{
					// Check odd cases like empty space
					continue;
				}
				// Split them into sequential commands
				// So that each string in the array will be used sequentialy
				String[] parsed = command.split ( ";" );
				for ( String s : parsed )
				{
					handle ( s );
				}
			}
		}
		// User entered "exit"
		SysLib.cout ( "Out of the Shell" + "\n" );
		SysLib.exit ( );
	}

	/*
		This method is used to execute the command that need to be executed before
		moving to the next sequential command
		Note: The passed in command might be a concurrent as well
		Example: PingPong abc 45 & PingPong std 15 ; PingPong asd 20
		After being splitted above , then we will have PingPong abc 45 & PingPong std 15 and
		PingPong asd 20 as 2 sequential command

		@param command the Command to execute



	 */
	public void handle ( String command )
	{
		String[] s = { command };
		// Split the command if there are any concurrent processes
		String[] concurrent = s[ 0 ].split ( "&" );

		int i = 0;
		// Loop through and execute the command
		while ( i < concurrent.length )
		{
			String execute = concurrent[ i ];
			int id = SysLib.exec ( SysLib.stringToArgs ( execute ) );
			if ( id < 0 )
			{
				// Odd cases, Can't create a thread
				return;
			}
			// log file
			keeper.add ( id );
			i++;
		}
		while ( true )
		{
			int afterExecute = SysLib.join ( );
			if ( afterExecute < 0 )
			{
				// Odd cases
				keeper.clear ( );
				return;
			}
			if ( keeper.contains ( afterExecute ) )
			{
				// We know that the threads with this id has terminated, remove from ongoing log
				keeper.remove ( afterExecute );
			}
			if ( keeper.isEmpty ( ) )
			{
				// Everything has terminated, can return to the shell for next sequential command
				break;
			}
		}
		return;
	}
}

