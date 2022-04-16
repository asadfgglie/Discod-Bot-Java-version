package ckcsc.asadfgglie.util.command;

import ckcsc.asadfgglie.main.Basic;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class CommandData {
    public String[] cmd;
    public boolean isCmd;
    public User target = null;

    public CommandData(boolean isCommand, String[] command){
        this.cmd = command;
        this.isCmd = isCommand;

        if(isCmd) {
            try {
                String targetStr = CommandData.getUserID(cmd[cmd.length - 1]);

                if (Basic.BUILDER.getSelfUser().getId().equals(targetStr)) {
                    target = Basic.BUILDER.getSelfUser();
                }
            }
            catch (Exception ignore){}
        }
    }

    public static String getUserID(String contentRaw){
        return contentRaw.substring(2, contentRaw.length() - 1);
    }

    public boolean isTargetSelf(){
        return hasTarget() && target.getId().equals(Basic.BUILDER.getSelfUser().getId());
    }

    public boolean hasTarget(){
        return target != null;
    }

    public boolean cmdHeadEqual(CharSequence... str){
        for(CharSequence sequence :str){
            if(cmd[0].contentEquals(sequence)){
                return true;
            }
        }
        return false;
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
