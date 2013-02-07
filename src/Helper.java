import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;

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
 * This class helps to the other extension classes.
 */
public class Helper {

	public static GraphDatabaseService GRAPHDB;

	public static enum Neo4LogoRelationshipTypes implements RelationshipType {
		RELATIONSHIP
	}

}
