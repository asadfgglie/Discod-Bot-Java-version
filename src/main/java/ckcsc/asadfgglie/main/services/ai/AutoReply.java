package ckcsc.asadfgglie.main.services.ai;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.util.command.CommandData;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Using Pretrained GPT2 to fine-tune to dialog-GPT.<br>
 * Do not use IDEA to terminate the bot, or the flask process can't close correctly.
 */
public class AutoReply extends Services{
    protected long CHANNEL_ID;

    /**
     * Language model use flask to call python interface.
     * Default value is 5000
     */
    private int FLASK_PORT = 5000;
    private boolean remote = false;

    private String MODEL_PATH;

    private final ArrayList<String> chat_history = new ArrayList<>();

    private final AtomicBoolean flask_console_output = new AtomicBoolean(false);

    protected Socket socket;

    @Override
    public void registerByEnvironment (@NotNull JSONObject values) {
        try {
            if (!values.isNull("CHANNEL_ID")) {
                this.CHANNEL_ID = values.getLong("CHANNEL_ID");
            }
            else {
                this.CHANNEL_ID = -1;
            }
        }
        catch (JSONException e) {
            logger.error("Need \"CHANNEL_ID\" to register service. If null or -1, it will be private channel mode.", e);
        }

        try {
            this.FLASK_PORT = values.getInt("port");
        }
        catch (JSONException e) {
            logger.warn("\"port\" set to default 5000.");
        }

        try{
            remote = values.getBoolean("remote");
        }
        catch (JSONException e){
            logger.warn("\"remote\" set to default false. It will create flask process for Java.");
        }

        if(!remote) {
            try {
                this.MODEL_PATH = values.getString("modelPath");
            }
            catch (JSONException e) {
                logger.error("Need \"modelPath\" to register service.", e);
            }

            try {
                flask_console_output.set(values.getBoolean("flask_console_output"));
                if (flask_console_output.get()) {
                    logger.info("Flask console will output to Java console.");
                }
                else {
                    logger.info("Flask console won't output to Java console.");
                }
            }
            catch (JSONException e) {
                logger.warn("\"flask_console_output\" set to default false. It won't log flask's output to Java.");
            }
        }

        try {
            socket = IO.socket("http://localhost:" + FLASK_PORT);
        }
        catch (URISyntaxException e) {
            logger.error("Illegal port.");
        }

        logger.info("Socket connecting...");
        socket.on(Socket.EVENT_CONNECT, args1 -> logger.info("Socket connect!"));

        socket.connect();

        try {
            if(!remote) {
                ProcessBuilder builder = new ProcessBuilder(values.getString("PYTHON_PATH"), Basic.SCRIPT_PATH + File.separator + "flask_service.py", MODEL_PATH, String.valueOf(FLASK_PORT));
                builder.redirectErrorStream(true);
                Process flask = builder.start();

                logger.info("flask process init...");

                // Use to get the flask output into java
                Thread FLASK_INPUT_STREAM = new Thread(null, () -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(flask.getInputStream(), "Big5"))) {
                        String tmp;
                        while (true) {
                            tmp = reader.readLine();
                            if (tmp != null && !tmp.equals("")) {
                                if (flask_console_output.get()) {
                                    logger.info("Flask - " + tmp);
                                }
                            }
                        }
                    }
                    catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }, "FLASK - " + logger.getName());

                Runtime.getRuntime().addShutdownHook(new Thread(flask::destroy));

                FLASK_INPUT_STREAM.start();

                while (!socket.connected());
            }

            logger.info("flask process is ready.");
        }
        catch (Exception e) {
            logger.error("Can't create flask process to run model.", e);
        }
    }

    @Override
    public void onMessageReceived (@NotNull MessageReceivedEvent event) {
        if(event.getChannel().getIdLong() == CHANNEL_ID || (!event.isFromGuild() && CHANNEL_ID == -1)) {
            if(CommandData.getCmdData(event).isCmd){
                CommandData cmd = CommandData.getCmdData(event);
                if(cmd.cmdHeadEqual("clear")){
                    chat_history.clear();
                    event.getChannel().sendMessage("Clear chat history.").queue();
                }
                return;
            }

            printMsg(event);
            if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
                return;
            }

            chat_history.add(event.getMessage().getContentDisplay());

            JSONObject json = new JSONObject();
            String[] tmp = new String[chat_history.size()];
            chat_history.toArray(tmp);
            json.put("text", tmp);
            json.put("CHANNEL_ID", event.getChannel().getIdLong());
            json.put("return_num_seq", 1);

            AtomicBoolean has_reply = new AtomicBoolean(false);
            socket.emit("generate", json).on("reply", args -> {
                if (!has_reply.get()) {
                    JSONObject data = new JSONObject(String.valueOf(args[0]));
                    if (data.getLong("CHANNEL_ID") == event.getChannel().getIdLong()) {
                        JSONArray reply = data.getJSONArray("reply");
                        event.getChannel().sendMessage(reply.getString(0)).queue();
                        has_reply.set(true);
                        chat_history.add((String) reply.get(0));
                    }
                }
            });
        }
    }

    @Override
    public String toString () {
        if(remote){
            return AutoReply.class.getSimpleName() + "(serviceName: " + this.serviceName + ", CHANNEL_ID: " + this.CHANNEL_ID
                    + ", FLASK_PORT: " + this.FLASK_PORT + ", remote: " + true + ")";
        }
        return AutoReply.class.getSimpleName() + "(serviceName: " + this.serviceName + ", CHANNEL_ID: " + this.CHANNEL_ID
                + ", FLASK_PORT: " + this.FLASK_PORT + ", MODEL_PATH: " + this.MODEL_PATH + ")";
    }

    @Override
    public Services copy () {
        return new AutoReply();
    }
}
