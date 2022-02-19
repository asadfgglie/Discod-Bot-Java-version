package ckcsc.asadfgglie.main.services;

import ckcsc.asadfgglie.main.Basic;
import ckcsc.asadfgglie.main.services.Register.Services;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.TensorFlowException;

public abstract class AbstractAI extends Services {
    protected SavedModelBundle modelBundle;

    protected String modelPath;
    protected String setTags;

    protected String inputName;
    protected String outputName;

    protected int inputXSize;
    protected int inputYSize;

    protected long CHANNEL_ID;

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
            setTags = values.getString("modelTags");
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
}
