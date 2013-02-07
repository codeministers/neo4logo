import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
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
 * This class starts the Database and connects it to the extension.
 * Also, create a trigger for shutdown it in case of JVM close.
 */
public class ConnectDB extends DefaultCommand {

	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { Syntax.StringType(), Syntax.StringType() });
	}
	
	public void perform(Argument args[], Context context) throws ExtensionException {
		String path;
		String logFilePath;
		
		try {
			path = args[0].getString();
			logFilePath = args[1].getString();
		} catch (LogoException e) {
			throw new ExtensionException(e.getMessage());
		}
		
		LogFile.openFile(logFilePath);
		LogFile.write("Opening DataBase");
		
		Helper.GRAPHDB = new GraphDatabaseFactory().newEmbeddedDatabase(path);
		registerShutdownHook(Helper.GRAPHDB);

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			public void run() {
				graphDb.shutdown();
			}
			
		});
	}

}
