# Discord Bot - Java version

---

主要分四個區塊:

* SetUp
* Basic
* Service
* Service register system

目前已有服務:

* GFloor

   此服務為檢查頻道是否有按照G樓規則發言

   G樓規則:

   * 在所有文字訊息前都必須以 `g數字` 開頭
   * `g數字` 中，`數字` 表示樓層
   * 樓層必須從 `1` 樓開始建造，下一個發言人必須要接著蓋一層樓
   * 同一則訊息不可以換行來連續建樓，同一人也不可以連續建樓
     ```
     User1:
     g1 haha
     g2 666
     g3 777
     (僅視為 g1 樓)
     
     User2:
     g1 haha
     
     User2:
     g2 666
     X
     ```

* MusicPlayer

   音樂機器人服務

   指令中不需要有 `<` & `>`，以下僅作為示範用
   
   可用指令:

   * `!play <url> <volume>`: 播放音樂
     * `url`: 音樂連結
     * `volume`: 音量設定，預設為15
     
   * `!pause`: 暫停當前撥放的音樂
   
   * `!skip`: 跳過當前音樂

   * `!stop`: 停止撥放音樂

   * `!volume <volume>`: 調整音量大小
     * `volume`: 音量大小

   * `!volume`: 顯示當前音量大小


* 可用基本指令

  * `!info list`: 查詢當前已註冊服務

  * `!info <Service Class> <Service name>`: 查詢對應服務的資訊
    * `Service Class`: 在 `RegisterEnvironment.json` 中的 `Service class name`
    * `Service name`: 在 `RegisterEnvironment.json` 中的 `Service name`

---

## SetUp
位於 `src/main/java/ckcsc/asadfgglie/setup/SetUp.java`

為程式進入點

請在命令列中傳入 `--configpath <path>` 來指定 `config-folder` 的儲存路徑

若沒有進行指定，預設將以本檔案所在目錄為 `config-folder`(若已經打包成Jar檔，將設為Jar所在目錄)

`config-folder` 中，必須存在 `BotConfig.json`

* `BotConfig.json`example:
   ```json
   {
     "TOKEN": "Your Bot Token"
   }
   ```

---

## Basic
位於 `src/main/java/ckcsc/asadfgglie/main/Basic.java`

機器人的主體程式碼

主要用於接收 Discord 事件，並使註冊的服務運作

---

## Service

位於 `src/main/java/ckcsc/asadfgglie/main/services/Register/Services.java`

為所有服務的父類別

其中 `SERVICES_LIST` 記錄著可以使用的服務

所有的服務皆必須在 `init()` 使用 `loginService()` 登記

同時，所有服務也都需要覆寫 `copy()` 以利後續的服務註冊

`copy()` 必須要回傳自己的複製物件實例

---

## Service register system

1. 首先，請在 `Service.init()` 中，使用 `Service.loginService()` 登記，並使服務繼承 `Service` 類別

2. 接下來，請在服務中覆寫 `registerByEnvironment()`、`copy()` 這兩個方法:

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

3. 接下來請為你的服務撰寫 `RegisterEnvironment.json`，請存放於執行時命令列參數 `--configpath <path>` 所設定的資料夾

   並請依照以下格式撰寫:
   
   * `RegisterEnvironment.json`example:
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

   * `Service class name` 請務必填寫有在 `Service.init()` 中登記的服務物件類別名稱
   * 在同服務類別中，`Service name` 請務必為唯一名稱
   * `Service value` 們會變成一個 `json` 物件和 `Service class name` 一同傳入 `registerByEnvironment()` 中