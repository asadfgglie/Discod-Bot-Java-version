package ckcsc.asadfgglie.main.services.Register;

import ckcsc.asadfgglie.main.services.GFloor;
import net.dv8tion.jda.api.events.Event;
import org.json.JSONObject;

import java.util.*;

public abstract class Services implements Register {
    protected String serviceName = null;
    protected long CHANNEL_ID = 0;
    public Thread thread = null;
    protected Event e = null;

    public static HashMap<String, ServiceArray> SERVICESLIST = new HashMap<>();

    public static void initialization() {
        loginService(GFloor.class.getSimpleName(), new GFloor());
    }

    private static void loginService(String className, Services service){
        SERVICESLIST.put(className, new ServiceArray(service));
    }

    @Override
    public void call(Event e) {
        this.e = e;
        this.thread = new Thread(this);
        this.thread.start();
    }

    protected Services(){}

    public abstract void registerByEnvironment(JSONObject values, String name);

    public abstract Services copy();

    protected abstract void printlnInfo(String msg);
    protected void printlnErr(String msg){}
}
