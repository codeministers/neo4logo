import java.util.HashMap;
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
		//AgentSet breedDogs = world.getBreed("DOGS");
		//AgentSet breedCats = world.getBreed("CATS");
		
		AgentSet breedLinks = world.links();
		
		// Ver que metemos en la transaccion y que no
		Transaction tx = Helper.GRAPHDB.beginTx();
		try {
			
			// Hacemos los indices para las turtles y las breeds de turtles
			Index<Node> turtlesIndex = Helper.GRAPHDB.index().forNodes("TURTLES");
			// Los indices de breeds seran para guardar o cargar solo esas 
			Index<Node> dogsIndex = Helper.GRAPHDB.index().forNodes("DOGS");
			Index<Node> catsIndex = Helper.GRAPHDB.index().forNodes("CATS");
			
			// Esto es del esquema, hay que ver como se trata
			Map<String, Index<Node>> allIndexs = new HashMap<String, Index<Node>>();
			allIndexs.put("DOGS", dogsIndex);
			allIndexs.put("CATS", catsIndex);
			
			// Hacemos el indice para los links
			Index<Relationship> linksIndex = Helper.GRAPHDB.index().forRelationships("LINKS");
			
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
			
			// Guardamos cada turtle
			for(Agent a : breedTurtles.agents()) {
				Turtle t = (Turtle) a;
				
				Node node = Helper.GRAPHDB.createNode();
				
				// Metemos la raza
				// Mejor forma de conseguir el breed de una turtle (t.getBreed().printname();)
				// y no por la lista que creamos y el indice de t.getBreedIndex();
				String breed = t.getBreed().printName();
				node.setProperty("BREED", breed);
				
				// Metemos todas las variables de Turtle
				for(int i = 0; i < turtleVarNumber; i++) {
					if(i != 8) {
						node.setProperty(nameVarTurtleBreeds.get("TURTLES")[i], t.getVariable(i));
					}
				}
				
				// Metemos todas las variables de la breed
				for(int i = turtleVarNumber; i < t.variables().length; i++) {
					// El (- turtleVarNumber) es para que el primer i sea 0, el segundo 1, ...
					node.setProperty(nameVarTurtleBreeds.get(breed)[i - turtleVarNumber], t.getVariable(i));
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
