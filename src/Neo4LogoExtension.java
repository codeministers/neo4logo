import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;

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
 * This class tells NetLogo what primitives are available in the extension.
 */
public class Neo4LogoExtension extends DefaultClassManager {
	
	public void load(PrimitiveManager primitiveManager) {
		//Essentials primitives
		primitiveManager.addPrimitive("connect-db", new ConnectDB());
		primitiveManager.addPrimitive("disconnect-db", new DisconnectDB());
		
		//Old version methods
		primitiveManager.addPrimitive("create-node", new CreateNode());
		primitiveManager.addPrimitive("create-relationship", new CreateRelationship());
		primitiveManager.addPrimitive("get-nodes", new GetNodes());
		primitiveManager.addPrimitive("get-relationships", new GetRelationships());
		
		//New version methods
		primitiveManager.addPrimitive("save-world", new SaveWorld());
		primitiveManager.addPrimitive("load-world", new LoadWorld());
	}
	
}
