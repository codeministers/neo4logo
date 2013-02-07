import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.QueryContext;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
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
 * This class creates relationships in NetLogo from every relationship in the Database.
 */
public class GetRelationships extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { });
	}

	public void perform(Argument args[], Context context) throws ExtensionException, LogoException {
		
		World world = (World) context.getAgent().world();
		AgentSet breed = world.links();
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			Index<Relationship> index = Helper.GRAPHDB.index().forRelationships("links");
			IndexHits<Relationship> hits = index.query("from-to", new QueryContext("*"));
			
			for(Relationship r : hits) {
				
				Turtle start = world.getTurtle((Long) r.getStartNode().getProperty("id"));
				Turtle end = world.getTurtle((Long) r.getEndNode().getProperty("id"));
				
				world.linkManager.createLink(start, end, breed);
			}
			
			tx.success();
		} finally {
			tx.finish();
		}
		
	}

}
