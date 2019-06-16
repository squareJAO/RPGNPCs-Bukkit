package rpg_npcs.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.fathzer.soft.javaluator.DoubleEvaluator;

public class CommadCalculate implements TabExecutor {
	private final DoubleEvaluator evaluator = new DoubleEvaluator();

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		
		String expression = String.join(" ", args);
		double result = evaluator.evaluate(expression);
		sender.sendMessage(" = " + result);
		
		return true;
	}

}
