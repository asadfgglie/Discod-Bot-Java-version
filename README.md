# Discord Bot - Java version

---

主要分三個區塊:

* Main
* Service
* Service register system

---

## Main
位於 `src/main/java/ckcsc/asadfgglie/main/Main.java`

機器人的主體程式碼

主要用於接收 Discord 事件，並使註冊的服務運作

---

## Service

位於 `src/main/java/ckcsc/asadfgglie/main/services/Register/Services.java`

為所有服務的父類別

其中 `SERVICESLIST` 記錄著可以使用的服務

所有的服務皆必須在 `initialization()` 使用 `loginService()` 登記

同時，所有服務也都需要覆寫 `copy()` 以利後續的服務註冊

`copy()` 必須要回傳自己的複製物件實例

請覆寫 `run()` 來讓服務運行在新的執行續中

預設會會執行在新的執行續之中，如果有必要，請覆寫 `Service.call()` 方法改變執行規則

---

## Service register system

1. 首先，請在 `Service.initialization()` 中，使用 `Service.loginService()` 登記，並使服務繼承 `Service` 類別

2. 接下來，請在服務中覆寫 `registerByEnvironment()`、`copy()`、`run()` 這三個方法:

   * `copy()`
   ```Java 
   public abstract Service copy();
   ```

   請回傳服務物件的複製參照

   * `registerByEnvironment()`
   ```Java
   public abstract void registerByEnvironment(JSONObject values, String name);
   ```

   其中，`values` 為 `json` 物件，裡面直接存放物件的初始化設定值

   請自行依照傳入的 `json` 物件初始化服務

   `name` 為服務的名稱，如有需要，可以將其設為 `serviceName` 的值

   * `run()`
   ```Java
   public void run(){}
   ```
   此為服務的進入點，會執行在一個新的執行續。

   如果不想執行在新執行續，可以覆寫 `Service.call()` 方法:
   * `Service.call()`
   ```Java
   public void call(Event e){} 
   ```

3. 接下來請為你的服務撰寫 `RegisterEnvironment.json`，預設路徑為 `src/main/resources/RegisterEnvironment.json`

   並請依照以下格式撰寫:
   
   example:
   ```json 
   {
      "GFloor": { // Service class name
         "成電服專用GG人1號": {// Service name
            "CHANNEL_ID": 666, // Service value
            "nowFloor": 0,     // Service value
            "maxFloor": 0      // Service value
         },
         "成電服專用GG人2號": {// Service name
            "CHANNEL_ID": 777, // Service value
            "nowFloor": 0,     // Service value
            "maxFloor": 0      // Service value
         }
      }
   }
   ```

   * `Service class name` 請務必填寫有在 `Service.initialization()` 中登記的服務物件類別名稱
   * 在同服務類別中，`Service name` 請務必為唯一名稱
   * `Service value` 們會變成一個 `json` 物件和 `Service class name` 一同傳入 `registerByEnvironment()` 中