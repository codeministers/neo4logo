import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
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
 * This class creates a relationship in the Database from NetLogo.
 */
public class CreateRelationship extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { Syntax.LinkType() });
	}

	public void perform(Argument args[], Context context) throws ExtensionException {
		Link link;
		
		try {
			link = args[0].getLink();
		} catch (LogoException e) {
			throw new ExtensionException(e.getMessage());
		}

		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			Index<Node> index = Helper.GRAPHDB.index().forNodes("turtles");
			Node start = index.get("id", link.end1().id()).getSingle();
			Node end = index.get("id", link.end2().id()).getSingle();
			
			Relationship rel = start.createRelationshipTo(end, Helper.Neo4LogoRelationshipTypes.RELATIONSHIP);
			Index<Relationship> relIndex = Helper.GRAPHDB.index().forRelationships("links");
			relIndex.add(rel, "from-to", link.end1().id() + "-" + link.end2().id());
			
			tx.success();
		} finally {
			tx.finish();
		}

	}
	
}