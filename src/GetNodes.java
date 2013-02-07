import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
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
 * This class creates nodes in NetLogo from every node in the Database.
 */
public class GetNodes extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { });
	}

	public void perform(Argument args[], Context context) throws ExtensionException, LogoException {
		
		World world = (World) context.getAgent().world();
		AgentSet breed = world.turtles();
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			Index<Node> index = Helper.GRAPHDB.index().forNodes("turtles");
			IndexHits<Node> hits = index.query("id", new QueryContext("*").sort(new Sort(new SortField("id", SortField.LONG))));
			
			for(Node n : hits) {
				Turtle turtle = world.createTurtle(breed, 0, 0);
				Double x = (Double) n.getProperty("x");
				Double y = (Double) n.getProperty("y");
				turtle.xandycor(x, y);
				turtle.shape((String) n.getProperty("shape"));
			}
			
			tx.success();
		} catch (AgentException e) {
			throw new ExtensionException(e.getMessage());
		} finally {
			tx.finish();
		}
		
	}
	
}
