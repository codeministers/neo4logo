import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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


public class LoadWorld extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { });
	}

	public void perform(Argument args[], Context context) throws ExtensionException, LogoException {
		
		World world = (World) context.getAgent().world();
		//AgentSet breedTurtles = world.turtles();
		//Vamos a coger la breed RAZAS
		AgentSet breedTurtles = world.getBreed("RAZAS");
		AgentSet breedLinks = world.links();
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			
			//Load all the nodes...
			Index<Node> indexNode = Helper.GRAPHDB.index().forNodes("turtles");
			IndexHits<Node> hitsNode = indexNode.query("id", new QueryContext("*").sort(new Sort(new SortField("id", SortField.LONG))));
			
			LogFile.write("Number of turtles to load: " + hitsNode.size());
			
			for(Node n : hitsNode) {
				Turtle turtle = world.createTurtle(breedTurtles, 0, 0);
				//color
				turtle.heading((Double) n.getProperty("heading"));
				Double x = (Double) n.getProperty("x");
				Double y = (Double) n.getProperty("y");
				turtle.xandycor(x, y);
				turtle.shape((String) n.getProperty("shape"));
				turtle.label(n.getProperty("label"));
				turtle.labelColor((Double) n.getProperty("label-color"));
				//breed
				turtle.hidden((Boolean) n.getProperty("hidden")); 
				turtle.size((Double) n.getProperty("size"));
				turtle.penSize((Double) n.getProperty("pen-size"));
				turtle.penMode((String) n.getProperty("pen-mode"));
				
				int numVars = (Integer) n.getProperty("numVars");
				for(int i = 13; i < numVars; i++) {
					String varName = "var-" + i;
					turtle.setVariable(i, n.getProperty(varName));
				}
				
				LogFile.write("Loading turtle: " + turtle.id());
			}
			
			//...and now, load all the links
			Index<Relationship> indexLinks = Helper.GRAPHDB.index().forRelationships("links");
			IndexHits<Relationship> hitsLinks = indexLinks.query("from-to", new QueryContext("*"));
			
			LogFile.write("Number of links to load: " + hitsLinks.size());
			
			for(Relationship r : hitsLinks) {
				
				Turtle start = world.getTurtle((Long) r.getStartNode().getProperty("id"));
				Turtle end = world.getTurtle((Long) r.getEndNode().getProperty("id"));
				
				world.linkManager.createLink(start, end, breedLinks);
				
				LogFile.write("Loading link: " + start.id() + "-" + end.id());
			}
			
			tx.success();
		} catch (AgentException e) {
			throw new ExtensionException(e.getMessage());
		} finally {
			tx.finish();
		}
		
	}
	
}