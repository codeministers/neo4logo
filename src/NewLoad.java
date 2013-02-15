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
		
		// Nuestras razas seran "DOGS" y "CATS"
		AgentSet breedTurtles = world.turtles();
		AgentSet breedDogs = world.getBreed("DOGS");
		AgentSet breedCats = world.getBreed("CATS");
		AgentSet breedLinks = world.links();
		
		// Guardamos las razas en el map "breeds"
		// Esto parece que no se usa
		Map<String, AgentSet> breeds = new HashMap<String, AgentSet>();
		
		breeds.put("TURTLES", breedTurtles);
		breeds.put("DOGS", breedDogs);
		breeds.put("CATS", breedCats);
		
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			
			// Creamos los indices para nuestras razas y el hitsNode para recorrer
			Index<Node> turtlesIndex = Helper.GRAPHDB.index().forNodes("TURTLES");
			
			// Estos se usarian para las cargas de estas breeds
			//Index<Node> dogsIndex = Helper.GRAPHDB.index().forNodes("DOGS");
			//Index<Node> catsIndex = Helper.GRAPHDB.index().forNodes("CATS");
			
			IndexHits<Node> turtlesHits = turtlesIndex.query("id", new QueryContext("*").sort(new Sort(new SortField("id", SortField.LONG))));
			
			// Aquí guardamos los nombres de las breeds con sus variables
			Map<String, String[]> nameVarTurtleBreeds = new HashMap<String, String[]>();
			
			// Para la breed TURTLE
			int turtleVarNumber = world.getVariablesArraySize((Turtle) null, world.turtles());
			String[] varNames = new String[turtleVarNumber];
			
			// Recorremos todas las variables de TURTLE
			for(int i = 0; i < turtleVarNumber; i++) {
				varNames[i] = world.turtlesOwnNameAt(i);
			}
			
			// Guardamos en el map
			nameVarTurtleBreeds.put("TURTLES", varNames);
			
			
			// Para el resto de breeds
			for(String s : world.getBreeds().keySet()) {	
				int varNumber = world.getVariablesArraySize((Turtle) null, world.getBreed(s));
				varNames = new String[varNumber];
				
				for (int i = turtleVarNumber; i < varNumber; i++) {
					varNames[i - turtleVarNumber] = world.breedsOwnNameAt(world.getBreed(s), i);
		        }
				
				nameVarTurtleBreeds.put(s, varNames);
			}
			
			// Recorremos los nodos
			for(Node n: turtlesHits) {
				// Comprobamos cual es la raza de nuestra turtle
				// AgentSet breed = breeds.get(n.getProperty("breed"));
				AgentSet breed = world.getBreed((String) n.getProperty("BREED"));
				Turtle t = world.createTurtle(breed, 0, 0);
				
				// Introducimos los valores de las variables de nuestra turtle
				String[] variables = nameVarTurtleBreeds.get("TURTLES");
				for(int i = 0; i < turtleVarNumber; i++) {
					if(i != 0 && i != 8) {
						t.setVariable(i, n.getProperty(variables[i]));
					}
				}
				
				// Introducimos los valores de su breed
				variables = nameVarTurtleBreeds.get(n.getProperty("BREED"));
				for(int i = turtleVarNumber; i < variables.length; i++) {
					t.setVariable(i, n.getProperty(variables[i - turtleVarNumber]));
				}
			}
			
			// ...and now, load all the links
			Index<Relationship> linksIndex = Helper.GRAPHDB.index().forRelationships("LINKS");
			IndexHits<Relationship> linksHits = linksIndex.query("from-to", new QueryContext("*"));

			for(Relationship r : linksHits) {
				
				// Al hacerlo automático, los id no son id
				String id = nameVarTurtleBreeds.get("TURTLES")[0];
				Turtle start = world.getTurtle(((Double) r.getStartNode().getProperty(id)).longValue());
				Turtle end = world.getTurtle(((Double) r.getEndNode().getProperty(id)).longValue());
				
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
