package ckcsc.asadfgglie.main.services.Register;


public class ServiceArray {
    public Services[] array;
    private Services serviceClass;

    ServiceArray(Services service) {
        serviceClass = service;
    }

    public void setArraySize(int serviceNumber) {
        array = new Services[serviceNumber];
    }

    public void initArray() {
        for(int i = 0; i < array.length; i++){
            array[i] = serviceClass.copy();
        }
        serviceClass = null;
    }
}
