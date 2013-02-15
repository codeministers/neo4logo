import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;

public class NewSave extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] {});
	}
	
	public void perform(Argument[] arg0, Context context) throws ExtensionException, LogoException {
		
		// Cogemos el mundo y sus razas
		World world = (World) context.getAgent().world();
		AgentSet breedTurtles = world.turtles();
		
		// No se usan
		// AgentSet breedDogs = world.getBreed("DOGS");
		// AgentSet breedCats = world.getBreed("CATS");
		
		AgentSet breedLinks = world.links();
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
		
			// Hacemos los indices para las turtles y las razas de turtles
			Index<Node> turtlesIndex = Helper.GRAPHDB.index().forNodes("TURTLES");
			Index<Node> dogsIndex = Helper.GRAPHDB.index().forNodes("DOGS");
			Index<Node> catsIndex = Helper.GRAPHDB.index().forNodes("CATS");
			
			// Esto es del esquema, hay que ver como se trata
			Map<String, Index<Node>> allIndexs = new HashMap<String, Index<Node>>();
			allIndexs.put("DOGS", dogsIndex);
			allIndexs.put("CATS", catsIndex);
			
			// Hacemos el indice para los links
			Index<Relationship> linksIndex = Helper.GRAPHDB.index().forRelationships("LINKS");
			
			// Aquí guardamos las razas y sus variables
			Map<String, Object> mapTurtleBreeds = world.getBreeds();
			List<String> listTurtleBreeds = new ArrayList<String>();
			Map<String, String[]> nameVarsTurtleBreeds = new HashMap<String, String[]>();
			
			// Aquí hacemos lo de guardar las razas y sus variables
			for(String s : mapTurtleBreeds.keySet()) {
				// Guardamos el nombre de la breed
				listTurtleBreeds.add(s);
				
				// Guardamos los nombres de las variables cada breed
				// Se podría optimizar ya que las 13 primeras son las mismas
				int variableNumber = world.getVariablesArraySize((Turtle)null, world.getBreed(s));
				String[] variableNames = new String[variableNumber];
				for (int i = 0; i < variableNumber; i++) {
					variableNames[i] = world.breedsOwnNameAt(world.getBreed(s), i);
		        }
				
				// Guardamos el nombre de las variables de la breed
				nameVarsTurtleBreeds.put(s, variableNames);
			}
			
			// Guardamos cada turtle o raza
			for(Agent a : breedTurtles.agents()) {
				Turtle t = (Turtle) a;
				
				Node node = Helper.GRAPHDB.createNode();
				
				// Metemos la raza
				String breed = listTurtleBreeds.get(t.getBreedIndex());
				node.setProperty("breed", breed);
				
				// Metemos todas las variables
				for(int i = 0; i < t.variables().length; i++) {
					if(i != 8) {
						node.setProperty(nameVarsTurtleBreeds.get(breed)[i], t.getVariable(i));
					}
				}
				
				turtlesIndex.add(node, "id", t.id());
				allIndexs.get(breed).add(node, "id", t.id());
				
			}
			
			for(Agent a : breedLinks.agents()) {
				Link l = (Link) a;
				
				IndexHits<Relationship> hits = linksIndex.get("from-to", l.end1().id() + "-" + l.end2().id());
				if(!hits.hasNext()) {
					
					Node start = turtlesIndex.get("id", l.end1().id()).getSingle();
					Node end = turtlesIndex.get("id", l.end2().id()).getSingle();
					
					Relationship rel = start.createRelationshipTo(end, Helper.Neo4LogoRelationshipTypes.RELATIONSHIP);
					
					linksIndex.add(rel, "from-to", l.end1().id() + "-" + l.end2().id());
				}
			}
			
			tx.success();
		} finally {
			tx.finish();
		}
		
	}
	
}
