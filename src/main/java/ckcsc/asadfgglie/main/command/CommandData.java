package ckcsc.asadfgglie.main.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class CommandData {
    public String[] cmd;
    public boolean isCmd;

    public CommandData(boolean isCommand, String[] command){
        this.cmd = command;
        this.isCmd = isCommand;
    }

    public boolean cmdHeadEqual(CharSequence str){
        return cmd[0].contentEquals(str);
    }

    public static CommandData getCmdData(@NotNull MessageReceivedEvent event){
        String commandStr = event.getMessage().getContentRaw();
        if(commandStr.startsWith("!")) {
            return new CommandData(true, commandStr.substring(1).split(" "));
        }
        else {
            return new CommandData(false, null);
        }
    }
}
