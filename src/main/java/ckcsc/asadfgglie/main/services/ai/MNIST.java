package ckcsc.asadfgglie.main.services.ai;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.util.Array;
import ckcsc.asadfgglie.util.Path;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlowException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MNIST extends Services {
    private SavedModelBundle modelBundle;

    private String modelPath;

    private String inputName;
    private String outputName;

    private int inputXSize;
    private int inputYSize;

    private long CHANNEL_ID;

    public MNIST(){}

    @Override
    public void registerByEnvironment(JSONObject values) {
        boolean loadSuccess = true;

        try{
            this.CHANNEL_ID = values.getLong("CHANNEL_ID");
        }
        catch (JSONException e){
            logger.error("You need to set the \"CHANNEL_ID\" to let MNIST know where to work!", e);
        }
        try{
            inputName = values.getString("inputName");
            outputName = values.getString("outputName");
        }
        catch (JSONException e){
            logger.error("You need to set the \"inputName\" and the \"outputName\" to let MNIST know how to use this model!");
            logger.error("If you want check the model I/O method name, use \"saved_model_cli\" to get more information.", e);
            loadSuccess = false;
        }
        try{
            inputXSize = values.getInt("inputXSize");
            inputYSize = values.getInt("inputYSize");
        }
        catch (JSONException e){
            logger.error("You need to set the \"inputXSize\" and the \"inputYSize\" to let MNIST know how to use this model!");
            logger.error("If you want check the model I/O method name, use \"saved_model_cli\" to get more information.", e);
            loadSuccess = false;
        }
        try{
            modelPath = values.getString("modelPath");
            String setTags = values.getString("modelTags");
            modelBundle = SavedModelBundle.load(modelPath, setTags);
        }
        catch (JSONException e){
            logger.error("You need to set the \"modelPath\" and the \"modelTags\" to let MNIST know work with which model!");
            logger.error("If you want check the model setTags, use \"saved_model_cli\" to get more information.", e);
            loadSuccess = false;
        }
        catch (TensorFlowException e){
            logger.error("Load model from " + modelPath + " failed.", e);
            loadSuccess = false;
        }

        if(loadSuccess) {
            logger.info("Load model from " + modelPath + " success.");

            getSelfConfig().put("LoadState", true);

            Basic.saveConfig(Basic.REGISTER_CONFIG);
        }
        else {
            getSelfConfig().put("LoadState", false);

            Basic.saveConfig(Basic.REGISTER_CONFIG);
            Basic.removeService(this);
        }
    }

    @Override
    public Services copy() {
        return new MNIST();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getChannel().getIdLong() != this.CHANNEL_ID){
            return;
        }

        for(Message.Attachment attachment : event.getMessage().getAttachments()){
            if(attachment.isImage()){
                String path = Path.transferPath(Basic.CONFIG_PATH + "\\tmp\\");

                File tmp = new File(path);
                if(!tmp.exists() && !tmp.isDirectory()){
                    logger.info("Create tmp folder at " + tmp.getPath());
                    tmp.mkdir();
                }

                attachment.getProxy().downloadToFile(new File(path + attachment.getFileName())).thenAccept(file -> {
                    logger.info("Get the image on " + attachment.getUrl());
                    logger.info("Save the image on " + file.getAbsolutePath());
                    int prediction;
                    try {
                        prediction = predictNumber(file);
                        logger.info("I guess it is "+ prediction + ".");
                        event.getChannel().sendMessage("I guess it is "+ prediction + ".").queue();
                    }
                    catch (IOException e){
                        logger.error("Couldn't read this image.", e);
                        event.getChannel().sendMessage("Couldn't read this image.").queue();
                    }
                });
            }
        }
    }

    private int predictNumber(File imgFile) throws IOException {
        BufferedImage bufferedImg = ImageIO.read(imgFile);

        float[][] pixels = getPixels(adjustImg(bufferedImg));
        float[] predictionArray;

        rollToCenter(pixels);

        Array.divide(pixels, 255);

        try(Tensor<Float> tensor = Tensor.create(new float[][][]{pixels}, Float.class)){
            predictionArray = modelBundle.session()
                    .runner()
                    .feed(inputName, tensor)
                    .fetch(outputName)
                    .run()
                    .get(0)
                    .copyTo(new float[1][10])[0];
        }

        return Array.maxIndex(predictionArray);
    }

    private void rollToCenter(float[][] pixels) {
        double[] beforeCenter = Array.centerPoint(pixels);

        logger.debug("Before roll center: " + Arrays.toString(beforeCenter));

        double xRoll = inputXSize / 2.0 - beforeCenter[0];
        double yRoll = inputYSize / 2.0 - beforeCenter[1];

        logger.debug("Target roll: [" + xRoll + ", " + yRoll + "]");

        Array.roll(pixels, (int) -Math.round(xRoll), (int) -Math.round(yRoll));

        logger.debug("After roll center: " + Arrays.toString(Array.centerPoint(pixels)));
    }

    private BufferedImage adjustImg(BufferedImage original) {
        BufferedImage adjustImg = new BufferedImage(inputXSize, inputYSize, original.getType());

        Graphics2D originalG = adjustImg.createGraphics();

        originalG.drawImage(original.getScaledInstance(inputXSize, inputYSize, Image.SCALE_AREA_AVERAGING), 0, 0, null);

        originalG.dispose();

        return adjustImg;
    }

    private float[][] getPixels(BufferedImage bufferedImg) {
        int width = bufferedImg.getWidth();
        int height = bufferedImg.getHeight();

        float[][] pixels = new float[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = bufferedImg.getRaster().getSampleFloat(x, y, 0);
            }
        }
        return pixels;
    }

    @Override
    public String toString(){
        return MNIST.class.getSimpleName() + "(serviceName: " + this.serviceName + ")";
    }
}
