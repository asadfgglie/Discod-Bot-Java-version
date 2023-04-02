package ckcsc.asadfgglie.main.services.ai;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.util.Path;
import ckcsc.asadfgglie.util.command.CommandData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HumanFeedback extends AutoReply {
    /**
     * Store all user's ChatData
     */
    private final HashMap<Long, UserData> UserChatData = new HashMap<>();

    /**
     * How many number of reply will generate.
     */
    private int return_num_seq = 5;

    @Override
    public void onReady (@NotNull ReadyEvent event) {
        TextChannel main = event.getJDA().getTextChannelById(CHANNEL_ID);
        assert main != null;
        do {
            for (Message message : main.getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
                message.delete().complete();
            }
        }
        while (!main.getHistoryFromBeginning(100).complete().isEmpty());

        for (GuildChannel channel: main.getParentCategory().getChannels()){
            if(channel.getIdLong() == CHANNEL_ID || !(channel instanceof TextChannel)){
                continue;
            }

            try {
                ((TextChannel) channel).sendMessage("因為後臺的重啟，使得對話紀錄已被清除").queue();
                UserChatData.put(Long.parseLong(channel.getName()), new UserData(
                        new ArrayList<>(),
                        (TextChannel) channel,
                        new ArrayList<>(),
                        new BufferedWriter(new FileWriter(Path.getPath() + File.separator + "data" + File.separator + channel.getName(), true))
                ));
            }
            catch (IOException e){
                logger.error("Can't save chat data in " + Path.getPath() + File.separator + "data" + File.separator + channel.getName(), e);
            }
        }

        main.sendMessage(
            "歡迎來到人類反饋訓練計畫!\n" +
                "對著機器人說 `!start` 就可以開始與機器人對話!\n\n" +

                "每次對話時，機器人會給你5個回覆，你可以選擇一個你喜歡的回覆，或著是自己再輸入一個回覆指導他要怎麼回\n\n" +

                "注意不要亂教機器人\n" +
                "管理員會檢查訓練資料的\n" +
                "要是發現你不斷的在亂教機器人\n" +
                "你的對話紀錄將不會被採用").queue();

        logger.info("Ready.");
    }

    @Override
    public void onShutdown (@NotNull ShutdownEvent useless) {
        for (long userId: UserChatData.keySet()) {
            UserData data = UserChatData.get(userId);

            saveData(data, userId, false);

            try {
                data.writer.close();
            }
            catch (IOException e) {
                logger.error("Can't save data in " + Path.getPath() + File.separator + "data" + File.separator + userId, e);
            }
        }
    }

    private void saveData (UserData data, long userId, boolean clear){
        try {
            data.writer.write( "dialog: " + new JSONArray(data.chatHistory.toArray()) + ", clear: " + clear);
            data.writer.newLine();
        }
        catch (IOException e) {
            logger.error("Can't save chat data in " + Path.getPath() + File.separator + "data" + File.separator + userId, e);
        }
    }

    @Override
    public void onMessageReceived (@NotNull MessageReceivedEvent event) {
        CommandData cmd = CommandData.getCmdData(event);
        if(cmd.isCmd){
            if(CHANNEL_ID == event.getChannel().getIdLong()) {
                if (cmd.cmdHeadEqual("start")) {
                    event.getMessage().delete().queue();
                    if(!UserChatData.containsKey(event.getAuthor().getIdLong())) {
                        try {
                            TextChannel channel = event.getGuild().createTextChannel(event.getAuthor().getId(), event.getChannel().asTextChannel().getParentCategory()).complete();

                            List<Permission> allow = new ArrayList<>();
                            allow.add(Permission.MESSAGE_SEND);
                            allow.add(Permission.MESSAGE_HISTORY);
                            allow.add(Permission.MESSAGE_ADD_REACTION);
                            allow.add(Permission.VIEW_CHANNEL);

                            channel.getManager().putMemberPermissionOverride(event.getAuthor().getIdLong(), allow, new ArrayList<>()).complete();

                            channel.sendMessage(event.getAuthor().getAsMention() + " 現在你可以跟我說話了！\n" +
                                    "發表消息後，您可以使用 ✅ 選擇您認為最好的消息。\n" +
                                    "你可以用 `!clear` 清除我的聊天記錄，你也可以用 `!say 範例` 教我該怎麼說。\n" +
                                    "你也可以使用 `!re` 讓我重新生成新的回复。").queue(message -> message.pin().queue());



                            UserChatData.put(event.getAuthor().getIdLong(), new UserData(
                                    new ArrayList<>(),
                                    channel,
                                    new ArrayList<>(),
                                    new BufferedWriter(new FileWriter(Path.getPath() + File.separator + "data" + File.separator + event.getAuthor().getIdLong(), true))
                            ));

                        }
                        catch (IOException e) {
                            event.getChannel().sendMessage("Sorry, IOError occur, please notice admin to fix it.").queue();
                            logger.error("Can't save chat data in " + Path.getPath() + File.separator + "data" + File.separator + event.getAuthor().getIdLong(), e);
                        }
                        catch (Exception e){
                            event.getMessage().getChannel().sendMessage("Sorry, some error occur, please notice admin to fix it.").queue();
                            logger.error(e.getMessage(), e);
                        }
                    }
                    else {
                        UserChatData.get(event.getAuthor().getIdLong()).chatThread.sendMessage(event.getAuthor().getAsMention() + "\n" + "Talk with me at here!").queue();
                    }
                }
            }


            if((UserChatData.containsKey(event.getAuthor().getIdLong()) &&
                    UserChatData.get(event.getAuthor().getIdLong()).chatThread.getIdLong() == event.getChannel().getIdLong()) ||
                    Basic.ADMIN_USER_LIST.contains(event.getAuthor().getIdLong())){
                if(cmd.cmdHeadEqual("clear", "cls")){
                    if(Basic.ADMIN_USER_LIST.contains(event.getAuthor().getIdLong())){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("做出你的選擇在跟我說下一句話，我腦子比較小，還搞不清楚太複雜的對話方式").queue();
                        return;
                    }
                    if(!UserChatData.get(event.getAuthor().getIdLong()).messageTmp.isEmpty()){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("做出你的選擇在跟我說下一句話，我腦子比較小，還搞不清楚太複雜的對話方式").queue();
                        return;
                    }

                    UserData data = UserChatData.get(event.getAuthor().getIdLong());

                    saveData(data, event.getAuthor().getIdLong(), true);

                    data.chatHistory.clear();
                    event.getChannel().sendMessage("Clear chat history.").queue();
                    clearMessageTmp(event.getAuthor().getIdLong());
                }

                if(cmd.cmdHeadEqual("teach", "say")){
                    if(Basic.ADMIN_USER_LIST.contains(event.getAuthor().getIdLong())){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("管理員，我在等使用者跟我說一句話，我還不知道他要跟我聊什麼").queue();
                        return;
                    }
                    if(UserChatData.get(event.getAuthor().getIdLong()).messageTmp.isEmpty()){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("跟我說一句話，我還不知道你要跟我聊什麼").queue();
                        return;
                    }

                    assert cmd.cmd != null;
                    StringBuilder tmp = new StringBuilder();

                    for(int i = 0; i < cmd.cmd.length; i++){
                        if(i == 0) continue;
                        tmp.append(cmd.cmd[i]);
                    }

                    UserChatData.get(event.getAuthor().getIdLong()).chatHistory.add(tmp.toString());
                    event.getChannel().sendMessage("OK, 我學到了: " + tmp).queue();
                    clearMessageTmp(event.getAuthor().getIdLong());

                    event.getMessage().delete().queue();
                }

                if(cmd.cmdHeadEqual("re", "re-reply")){
                    if(Basic.ADMIN_USER_LIST.contains(event.getAuthor().getIdLong())){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("管理員，我在等使用者跟我說一句話，我還不知道他要跟我聊什麼").queue();
                        return;
                    }
                    if(UserChatData.get(event.getAuthor().getIdLong()).messageTmp.isEmpty()){
                        event.getMessage().delete().queue();
                        event.getChannel().sendMessage("跟我說一句話，我還不知道你要跟我聊什麼").queue();
                        return;
                    }

                    sendReply(event.getAuthor(), event.getChannel());

                    event.getMessage().delete().queue();
                }
            }
            return;
        }

        if(UserChatData.containsKey(event.getAuthor().getIdLong()) && event.isFromGuild() && event.getChannel().getIdLong() != CHANNEL_ID) {
            if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
                return;
            }
            if(!UserChatData.get(event.getAuthor().getIdLong()).messageTmp.isEmpty()){
                event.getMessage().delete().queue();
                event.getChannel().sendMessage("做出你的選擇在跟我說下一句話，我腦子比較小，還搞不清楚太複雜的對話方式").queue();
                return;
            }
            printMsg(event);
            if(UserChatData.get(event.getAuthor().getIdLong()).chatThread.getIdLong() != event.getChannel().getIdLong()){
                return;
            }

            sendReply(event);
        }

        if(CHANNEL_ID == event.getChannel().getIdLong() && !event.getAuthor().isBot()){
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageReactionAdd (@NotNull MessageReactionAddEvent event) {
        if(!event.isFromGuild() || event.getUser().isBot()){
            return;
        }
        if(UserChatData.get(event.getUserIdLong()) == null){
            return;
        }
        if(UserChatData.get(event.getUserIdLong()).chatThread.getIdLong() != event.getChannel().getIdLong()){
            return;
        }

        if(UserChatData.get(event.getUserIdLong()).chatThread.getIdLong() == event.getChannel().getIdLong()){
            if(event.getEmoji().equals(Emoji.fromFormatted("✅"))){
                Message m = event.retrieveMessage().complete();

                while (UserChatData.get(event.getUserIdLong()).messageTmp.size() % return_num_seq != 0);

                if(UserChatData.get(event.getUserIdLong()).messageTmp.contains(m)) {
                    StringBuilder tmp = new StringBuilder();
                    String[] arrayTmp = m.getContentDisplay().split("[0-9]+\\. ");
                    for(int i = 0; i < arrayTmp.length; i++){
                        if(i == 0) continue;
                        tmp.append(arrayTmp[i]);
                    }
                    UserChatData.get(event.getUserIdLong()).chatHistory.add(tmp.toString());
                    clearMessageTmp(event.getUserIdLong());
                    event.getChannel().sendMessage(tmp).queue();
                }
            }
        }
    }

    private void clearMessageTmp(long userId){
        for(Message m: UserChatData.get(userId).messageTmp){
            m.delete().queue();
        }
        UserChatData.get(userId).messageTmp.clear();
    }

    private void sendReply (@NotNull User user, @NotNull MessageChannelUnion channel){
        ArrayList<String> chat_history = UserChatData.get(user.getIdLong()).chatHistory;

        JSONObject json = new JSONObject();
        String[] tmp = new String[chat_history.size()];
        chat_history.toArray(tmp);
        json.put("text", tmp);
        json.put("CHANNEL_ID", channel.getIdLong());
        json.put("return_num_seq", return_num_seq);

        AtomicBoolean has_reply = new AtomicBoolean(false);
        socket.emit("generate", json).on("reply", args -> {
            if (!has_reply.get()) {
                JSONObject data = new JSONObject(String.valueOf(args[0]));
                if (data.getLong("CHANNEL_ID") == channel.getIdLong()) {
                    JSONArray reply = data.getJSONArray("reply");

                    int i = 1;
                    for (Object tmp_reply: reply) {
                        Message message = channel.sendMessage(i + ". " + tmp_reply.toString()).complete();
                        
                        UserChatData.get(user.getIdLong()).messageTmp.add(message);
                        message.addReaction(Emoji.fromFormatted("✅")).complete();

                        i++;
                    }
                    has_reply.set(true);
                }
            }
        });
    }

    private void sendReply(@NotNull MessageReceivedEvent event){
        UserChatData.get(event.getAuthor().getIdLong()).chatHistory.add(event.getMessage().getContentDisplay());

        sendReply(event.getAuthor(), event.getChannel());
    }

    @Override
    public void registerByEnvironment (@NotNull JSONObject values) {
        super.registerByEnvironment(values);

        if(CHANNEL_ID == -1){
            logger.error("Need \"CHANNEL_ID\" to register service, and HumanFeedback service can't be -1");
        }

        try{
            return_num_seq = values.getInt("return_num_seq");
        }
        catch (JSONException e){
            logger.warn("\"return_num_seq\" will be set by 5.");
        }
    }

    @Override
    public Services copy () {
        return new HumanFeedback();
    }
}

class UserData{
    /**
     * Store all user's ChatHistory
     */
    public final ArrayList<String> chatHistory;
    /**
     * Store all user's ChatThread
     */
    public final TextChannel chatThread;
    /**
     * Store all user's reply option
     */
    public final ArrayList<Message> messageTmp;
    /**
     * Store all user's ChatHistory writer
     */
    public final BufferedWriter writer;

    public UserData (ArrayList<String> chatHistory, TextChannel chatThread, ArrayList<Message> messageTmp, BufferedWriter writer) {
        this.chatHistory = chatHistory;
        this.chatThread = chatThread;
        this.messageTmp = messageTmp;
        this.writer = writer;
    }
}