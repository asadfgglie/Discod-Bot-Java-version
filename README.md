# Discord Bot - Java version

## Video Demo:  https://youtu.be/Mq5y7zLnBQI

## Description:

---

Here are the four important part:

* SetUp
* Basic
* Service
* Service register system

---

## SetUp

It is in `src/main/java/ckcsc/asadfgglie/setup/SetUp.java`.

It is the enterpoint.

Use `--configpath <path>` to set `config-folder` path.

If you don't set `config-folder`, it will be set to the `SetUp.java`'s current directory by default.

If you have packed all the code into an jar file by `/gradlew packageApp`, it will be set to the JAR file's current directory by default.

In `config-folder`, it must exist `BotConfig.json`.

* `BotConfig.json` example:
   ```json
   {
     "TOKEN": "Your Bot Token"
   }
   ```

In `config-folder`, it need the file called `RegisterConfig.json`.

It is the more information that you can go to Part `Service register system`.

Some command of this bot need the admin permission, you can write a file called `AdminConfig.json`.

* `AdminConfig.json` example:
  ```json
  {
    "YOUR_COUNT_NAME#YOUR_TAG": YOUR_COUNT_ID
  }
  ```

---

## Basic

It is in `src/main/java/ckcsc/asadfgglie/main/Basic.java`.

The bot's basic framework.

Mainly be used to receive Discord events and make registered services work.

* Basic Commands Available

  * `!info list`: Query currently registered services.

    * `!info <Service Class> <Service name>`: Query information about the corresponding service
    * `Service Class`: `Service class name` in `RegisterEnvironment.json`
    * `Service name`: `Service name` in `RegisterEnvironment.json`

  * `!stopBot @bot`: Shutdown the bot. Commander must be an admin.
    * `@bot`: You must use the tag method to fill in the name of the bot.
  
  * `!op @USER`: Add an new admin into `AdminConfig.json`.
      * `@USER`: You must use the tag method to fill in the name of the user.

---

## Service

It is in `src/main/java/ckcsc/asadfgglie/main/services/Register/Services.java`.

It is parent class for all services.

There records all of the available services in `SERVICES_LIST`.

All services must be registered in `init()` using `loginService()`.

At the same time, all services also need to override `copy()` for subsequent service registration.

`copy()` must return its own copy object instance.

---

#### GFloor

   This service is to check whether the channel is speaking in accordance with the rules of GFloor.

   The rules of GFloor:

   1. All text messages must start with the `g<number>`
   2. In `g<number>`, the `<number>` represents this floor.
   3. Floors must be constructed from floor `1` and the next speaker must build a floor after.
   4. The same message cannot newline to build an new floor continuously, and the same person cannot build an new floor continuously.
   5. It is not allowed to take back the message, taking back the message is regarded as demolishing GFloor.
   6. You can edit the message, but the message must comply with the regulations of GFloor after editing, otherwise it will be regarded as demolishing GFloor.

   Example:


      User1:
      g1 haha
      g2 666
      g3 777
      (Only treated as g1 floor)

      User2:
      g1 haha

      User2:
      g2 666
      X

      User1:
      g1 haha

      User2:
      g1 haha

      User1:
      g3 666

      User2
      g3
      X
     

##### Known bug

Due to API limitations, Bot cannot check whether the GFloor built before startup satisfies Rule 5 and Rule 6.

#### MusicPlayer

   Available commands:

   * `!play <url> <volume>`: play musics
     * `play` can be replaced by the abbreviations `pl` or `p`.
     * `url`: Music link
     * `volume`: Volume setting,the default is 15.

   * `!pause`: Pause/resume currently play music
     * `pause` can be replaced by the abbreviation `pa`.

   * `!skip`: skip current music
     * `skip` can be replaced by the abbreviation `sk`.

   * `!stop`: stop playing music
     * `stop` can be replaced by the abbreviation `st`.

   * `!volume <volume>`: adjust volume
     * `volume` can be replaced by the abbreviation `v`.
     * `volume`: volume

   * `!volume`: Display the current volume level
     * `volume` can be replaced by the abbreviation `v`.

   * `!loop` : Turn loop playback on/off
     * `loop` can be replaced by the abbreviation `lp`.

   * `!list` : Display the current playlist
     * `list` can be replaced by the abbreviation `ls`.

   * `!shuffle` : Scramble the to-be-played list
     * `shuffle` can be replaced by the abbreviation `sh`.

---

## Service register system

1. First, in `Service.init()`, use `Service.loginService()` to register and make the service `extends` the `Service` class.

2. Next, override the `registerByEnvironment()`, `copy()` methods in the service:

   * `copy()`
   ```Java
   public abstract Service copy();
   ```

   Please return a copy reference of the service object instance.

   * `registerByEnvironment()`
   ```Java
   public abstract void registerByEnvironment(JSONObject values, String name);
   ```

   Among them, `values` is a `json` object, which directly stores the initialization setting value of the object.

   Please initialize the service according to the incoming `json` object.

   `name` is the name of the service, it can be set to the value of `serviceName` if desired

3. Next, please write `RegisterEnvironment.json` for your service:

   `RegisterEnvironment.json` records the `json` object passed in by `registerByEnvironment(JSONObject values, String name)` when each service is initialized.

   Each `Service name` corresponds to an object instance, which is also the `json` object passed into `registerByEnvironment(JSONObject values, String name)`

   Please save it in the folder set by the command line parameter `--configpath <path>` during execution, and please write in the following format:

   * `RegisterEnvironment.json` example:
      ```json
      {
         "GFloor": { // `Service class` name
            "GFLoor_bot1": {// `Service name`, NO SAPCES!
               "CHANNEL_ID": 666, // Service value, more information can get in the defualt `RegisterEnvironment.json`
               "nowFloor": 0,
               "maxFloor": 0
            },
            "GFloor_bot2": {// `Service name`, NO SAPCES!
               "CHANNEL_ID": 777, // Service value
               "nowFloor": 0,
               "maxFloor": 0
            }
         },
         "MusicPlayer":{ // `Service class` name
            "DJ":{// `Service name`, NO SAPCES!
               "isInfoVisible":true
            }
         }
      }
      ```

   * `Service class name` please be sure to fill in the service object class name registered in `Service.init()`.
   * In the same `Service class` for multiple services, `Service name` must be a unique name and NO SPACES!
   * all the `Service value` will become a `json` object instance and passed to `registerByEnvironment()` along with the `Service name`.
