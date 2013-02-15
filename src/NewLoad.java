import java.util.HashMap;
import java.util.Map;

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

public class NewLoad extends DefaultCommand {

	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] { });
	}

	public void perform(Argument args[], Context context) throws ExtensionException, LogoException {
		
		World world = (World) context.getAgent().world();
		
		//Nuestras razas seran "DOGS" y "CATS"
		AgentSet breedTurtles = world.turtles();
		AgentSet breedDogs = world.getBreed("DOGS");
		AgentSet breedCats = world.getBreed("CATS");
		AgentSet breedLinks = world.links();
		
		//Guardamos las razas en el map "breeds"
		Map<String, AgentSet> breeds = new HashMap<String, AgentSet>();
		
		breeds.put("TURTLES", breedTurtles);
		breeds.put("DOGS", breedDogs);
		breeds.put("CATS", breedCats);
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			
			//Creamos los indices para nuestras razas y el hitsNode para recorrer
			Index<Node> turtlesIndex = Helper.GRAPHDB.index().forNodes("TURTLES");
			Index<Node> dogsIndex = Helper.GRAPHDB.index().forNodes("DOGS");
			Index<Node> catsIndex = Helper.GRAPHDB.index().forNodes("CATS");
			
			IndexHits<Node> turtlesHitsNode = turtlesIndex.query("id", new QueryContext("*").sort(new Sort(new SortField("id", SortField.LONG))));
			
			// Cogemos las variables de la raza (mismo codigo que en
			// NewSave.java)
			Map<String, String[]> nameVarsTurtleBreeds = new HashMap<String, String[]>();
			for (String s : breeds.keySet()) {
				
				// Guardamos los nombres de las variables cada breed
				// Se podría optimizar ya que las 13 primeras son las mismas
				int variableNumber = world.getVariablesArraySize((Turtle) null, world.getBreed(s));
				String[] variableNames = new String[variableNumber];
				
				for (int i = 0; i < variableNumber; i++) {
					variableNames[i] = world.breedsOwnNameAt(world.getBreed(s), i);
				}
				nameVarsTurtleBreeds.put(s, variableNames);
			}
			
			//Recorremos los nodos
			for(Node n: turtlesHitsNode) {
				//Comprobamos cual es la raza de nuestra turtle
				//AgentSet breed = breeds.get(n.getProperty("breed"));
				AgentSet breed = world.getBreed((String) n.getProperty("breed"));
				Turtle turtle = world.createTurtle(breed, 0, 0);
				
				//Introducimos los valores de las variables de nuestra turtle
				String[] variables = nameVarsTurtleBreeds.get(breed);
				for(int i = 0; i < variables.length; i++) {
					turtle.setVariable(world.breedsOwnIndexOf(breed, variables[i]), n.getProperty(variables[i]));
				}
			}
			
			//...and now, load all the links
			Index<Relationship> linksIndex = Helper.GRAPHDB.index().forRelationships("links");
			IndexHits<Relationship> hitsLinks = linksIndex.query("from-to", new QueryContext("*"));

			for(Relationship r : hitsLinks) {
				
				Turtle start = world.getTurtle((Long) r.getStartNode().getProperty("id"));
				Turtle end = world.getTurtle((Long) r.getEndNode().getProperty("id"));
				
				world.linkManager.createLink(start, end, breedLinks);
				
			}
			
			tx.success();
		} catch (AgentException e) {
			throw new ExtensionException(e.getMessage());
		} finally {
			tx.finish();
		}
		
	}
	
}
