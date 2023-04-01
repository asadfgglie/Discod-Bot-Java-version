package ckcsc.asadfgglie.util.json;

import org.json.JSONObject;

public class JSONConfig extends JSONObject {
    public final String fullConfigName;

    public JSONConfig(String fullConfigName, String jsonStr){
        super(jsonStr);
        this.fullConfigName = fullConfigName;
    }

    @Override
    public String toString(){
        return super.toString(1);
    }
}
