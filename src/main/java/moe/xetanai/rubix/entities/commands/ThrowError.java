package moe.xetanai.rubix.entities.commands;

import moe.xetanai.rubix.entities.Command;
import moe.xetanai.rubix.entities.CommandContext;
import moe.xetanai.rubix.entities.CommandException;

public class ThrowError extends Command {

    public ThrowError () {
        super(new String[]{"throwerror"});
    }

    @Override
    public void run (CommandContext ctx) throws CommandException {
        throw new CommandException(ctx.getEvent().getMessage().getContentRaw());
    }
}
