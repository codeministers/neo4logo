import org.nlogo.agent.World;
import org.nlogo.api.Agent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;

public class Test extends DefaultCommand {
	
	public Syntax getSyntax() {
		return Syntax.commandSyntax(new int[] {});
	}
	
	public void perform(Argument[] arg0, Context context) throws ExtensionException, LogoException {
		World world = (World) context.getAgent().world();
		
		for(Agent a : world.turtles().agents()) {
			Turtle t = (Turtle) a;
			
			LogFile.write("Breed: " + t.getBreed().printName());
		}
	}
	
}
