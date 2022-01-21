package ckcsc.asadfgglie.main.services.Register;

import ckcsc.asadfgglie.main.services.GFloor;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;

public abstract class Services extends ListenerAdapter {
    protected String serviceName = null;
    protected long CHANNEL_ID = 0;

    public static HashMap<String, ServiceArray> SERVICES_LIST = new HashMap<>();

    public static void init() {
        loginService(GFloor.class.getSimpleName(), new GFloor());
    }

    private static void loginService(String className, Services service){
        SERVICES_LIST.put(className, new ServiceArray(service));
    }

    protected Services(){}

    public abstract void registerByEnvironment(JSONObject values, String name);

    public abstract Services copy();

    protected abstract void printlnInfo(String msg);
    protected void printlnErr(String msg){}
}
