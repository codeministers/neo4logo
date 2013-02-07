import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Syntax;

/**
 * NetLogo extension for connection with Neo4j NOSQL
 * Graph DataBase.
 * 
 * @version 0.1
 * 
 * @author Camacho Sosa, Jose Manuel - camachososa@josemazo.com
 * @author Munoz Rios, Gabriel - gabriel10009@hotmail.com
 */

/**
 * This class shutdown the Database and closes the connection with the extension.
 */
public class DisconnectDB extends DefaultCommand {

	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] {});
	}
	
	public void perform(Argument args[], Context context) throws ExtensionException {
		LogFile.write("Closing DataBase");
		LogFile.closeFile();
		
		Helper.GRAPHDB.shutdown();
	}

}
