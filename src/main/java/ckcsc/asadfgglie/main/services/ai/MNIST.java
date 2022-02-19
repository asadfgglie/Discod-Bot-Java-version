package ckcsc.asadfgglie.main.services.ai;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.AbstractAI;
import ckcsc.asadfgglie.main.services.Register.Services;
import ckcsc.asadfgglie.util.Array;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.tensorflow.Tensor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MNIST extends AbstractAI {
    public MNIST(){}

    @Override
    public Services copy() {
        return new MNIST();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getChannel().getIdLong() != this.CHANNEL_ID){
            return;
        }

        Message message = event.getMessage();
        List<Message.Attachment> attachments = message.getAttachments();

        for(Message.Attachment attachment :attachments){
            if(attachment.isImage()){
                String path = Basic.PATH + "\\tmp\\";

                File tmp = new File(path);
                if(!tmp.exists() && !tmp.isDirectory()){
                    logger.info("Create tmp folder at " + tmp.getPath());
                    tmp.mkdir();
                }

                attachment.downloadToFile(path + attachment.getFileName()).thenAccept(file -> {
                    logger.info("Get the image on " + attachment.getUrl());
                    logger.info("Save the image on " + path + attachment.getFileName());

                    int prediction;
                    try {
                        prediction = predictNumber(file);
                    }
                    catch (IOException e){
                        logger.error("Couldn't read this image.", e);
                        event.getChannel().sendMessage("Couldn't read this image.").queue();
                        return;
                    }

                    event.getChannel().sendMessage("I guess it is "+ prediction + ".").queue();
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

        try(Tensor<Float> tensor = Tensor.create(pixels, Float.class)){
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
