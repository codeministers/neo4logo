import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;

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
 * This class creates a node in the Database from NetLogo.
 */
public class CreateNode extends DefaultCommand {

	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { Syntax.TurtleType() });
	}

	public void perform(Argument args[], Context context) throws ExtensionException {
		Turtle turtle;
		
		try {
			turtle = args[0].getTurtle();
		} catch (LogoException e) {
			throw new ExtensionException(e.getMessage());
		}

		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			Index<Node> index = Helper.GRAPHDB.index().forNodes("turtles");
			
			Node node = Helper.GRAPHDB.createNode();
			node.setProperty("id", turtle.id());
			node.setProperty("x", turtle.xcor());
			node.setProperty("y", turtle.ycor());
			node.setProperty("shape", turtle.shape());
			
			index.add(node, "id", turtle.id());
			
			tx.success();
		} finally {
			tx.finish();
		}

	}

}