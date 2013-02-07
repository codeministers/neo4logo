import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.nlogo.agent.World;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;


public class SaveWorld extends DefaultCommand {

	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] {});
	}

	public void perform(Argument args[], Context context) throws ExtensionException {
		World world = (World) context.getAgent().world();
		AgentSet breedTurtles = world.turtles();
		AgentSet breedLinks = world.links();
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			
			LogFile.write("Number of turtles to save: " + breedTurtles.count());
			LogFile.write("Number of links to save: " + breedLinks.count());
			
			Index<Node> turtleIndex = Helper.GRAPHDB.index().forNodes("turtles");
			Index<Relationship> relIndex = Helper.GRAPHDB.index().forRelationships("links");
			
			//Estas lineas sacan todas las razas de turtles salvo la "padre"
			Map<String, Object> mapTurtleBreeds = world.getBreeds();
			for(String s : mapTurtleBreeds.keySet()) {
				LogFile.write("Breed de turtle: " + s);
			}
			
			//Estas lineas sacan todas las razas de links salvo la "padre"
			Map<String, Object> mapLinksBreeds = world.getLinkBreeds();
			for(String s : mapLinksBreeds.keySet()) {
				LogFile.write("Breed de link: " + s);
			}
			
			LogFile.write("Variables de turtle: " + breedTurtles.agents().iterator().next().variables().length);
			LogFile.write("Variables de link: " + breedLinks.agents().iterator().next().variables().length);
			LogFile.write("Variables de patch: " + world.patches().agents().iterator().next().variables().length);
			
			Object[] variables = breedTurtles.agents().iterator().next().variables();
			for(Object o : variables) {
				LogFile.write("Una variable: " + o);
			}
			
			//Save all the nodes...
			for(Agent a : breedTurtles.agents()) {
				Turtle t = (Turtle) a;
				IndexHits<Node> hits = turtleIndex.get("id", t.id());
				if(!hits.hasNext()) {
					LogFile.write("Saving turtle: " + t.id());
					
					Node node = Helper.GRAPHDB.createNode();
					
					node.setProperty("id", t.id());
					node.setProperty("color", t.color());
					node.setProperty("heading", t.heading());
					node.setProperty("x", t.xcor());
					node.setProperty("y", t.ycor());
					node.setProperty("shape", t.shape());
					node.setProperty("label", t.labelString());
					node.setProperty("label-color", t.labelColor());
					node.setProperty("breed", t.getBreedIndex());
					node.setProperty("hidden", t.hidden());
					node.setProperty("size", t.size());
					node.setProperty("pen-size", t.getVariable(11));
					node.setProperty("pen-mode", t.getVariable(12));
					
					int numVars = t.variables().length;
					node.setProperty("numVars", numVars);
					
					for(int i = 13; i < numVars; i++) {
						node.setProperty("var-" + i, t.getVariable(i));
					}
					
					turtleIndex.add(node, "id", t.id());
				}
			}
			
			//...and now, save all the links
			for(Agent a : breedLinks.agents()) {
				Link l = (Link) a;
				
				IndexHits<Relationship> hits = relIndex.get("from-to", l.end1().id() + "-" + l.end2().id());
				if(!hits.hasNext()) {
					
					LogFile.write("Saving link: " + l.end1().id() + "-" + l.end2().id());
					
					Node start = turtleIndex.get("id", l.end1().id()).getSingle();
					Node end = turtleIndex.get("id", l.end2().id()).getSingle();
					
					Relationship rel = start.createRelationshipTo(end, Helper.Neo4LogoRelationshipTypes.RELATIONSHIP);
					
					relIndex.add(rel, "from-to", l.end1().id() + "-" + l.end2().id());
				}
			}
				
			tx.success();
		} finally {
			tx.finish();
		}
	}
}
