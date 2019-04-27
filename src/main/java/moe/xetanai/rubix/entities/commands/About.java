package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;

public class About extends Command {
	public About() {
		super(new String[]{"about", "info"});
	}
}
