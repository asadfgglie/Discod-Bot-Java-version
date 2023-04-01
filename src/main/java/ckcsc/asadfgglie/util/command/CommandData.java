package ckcsc.asadfgglie.util.command;

import ckcsc.asadfgglie.main.Basic;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO: 新班DC API搞炸了我的ID獲取系統
public class CommandData {
    @Nullable
    public String[] cmd;
    public boolean isCmd;
    public User target = null;

    private CommandData(boolean isCommand, @Nullable String[] command){
        this.cmd = command;
        this.isCmd = isCommand;

        if(isCmd && cmd != null) {
            try {
                String targetStr = CommandData.getUserID(cmd[cmd.length - 1]);

                if (Basic.BUILDER.getSelfUser().getId().equals(targetStr)) {
                    target = Basic.BUILDER.getSelfUser();
                }
            }
            catch (Exception ignore){}
        }
        else if(isCmd){
            this.isCmd = false;
        }
    }

    public static String getUserID(String contentRaw){
        return contentRaw.substring(2, contentRaw.length() - 1);
    }

    public boolean isTargetSelf(){
        return !hasTarget() || !target.getId().equals(Basic.BUILDER.getSelfUser().getId());
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
