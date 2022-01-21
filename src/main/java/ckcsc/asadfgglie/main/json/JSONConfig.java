package ckcsc.asadfgglie.main.json;

import org.json.JSONObject;

public class JSONConfig extends JSONObject {
    public final String fullConfigName;

    public JSONConfig(String fullConfigName, String jsonStr){
        super(jsonStr);
        this.fullConfigName = fullConfigName;
    }

    // thanks for God timothychen write this
    @Override
        public String toString(){
            // get the original String
            StringBuilder configStrBlr = new StringBuilder(super.toString());

            String enterChar = ",{[}]";
            int[] nowIndent = {0, 0};
            int pointer = 0;
            boolean isInString = false;

            // Iterate all String to add '\newLine\' in suitable place
            for(; pointer < configStrBlr.length(); pointer++){
                char i = configStrBlr.charAt(pointer);

                // check if in the String
                if(i == '\'' || i == '\"'){
                    isInString = !isInString;
                }

                // add '\n' in front/back of the enterChar
                String iStr = String.valueOf(i);
                if (enterChar.substring(0, 3).contains(iStr) && !isInString) {
                    // in front
                    configStrBlr.insert(pointer + 1, "\\newLine\\");
                    pointer += 9;
                } else if (enterChar.substring(3).contains(iStr) && !isInString) {
                    // in back
                    configStrBlr.insert(pointer, "\\newLine\\");
                    pointer += "\\newLine\\".length();
                }
            }

            // split String
            String[] configStrArray = configStrBlr.toString().split("\\\\newLine\\\\");

            // add tab
            for(int i = 0; i < configStrArray.length; i++){
                // get String
                StringBuilder eachLine = new StringBuilder(configStrArray[i]);
                isInString = false;
                pointer = 0;

                for(; pointer < eachLine.length(); pointer++){
                    char j = eachLine.charAt(pointer);

                    if(j == '\'' || j == '\"'){
                        isInString = !isInString;
                    }

                    String iStr = String.valueOf(j);
                    if(enterChar.substring(1, 3).contains(iStr) && !isInString){
                        // add tab
                        nowIndent[1]++;
                    }
                    else if(enterChar.substring(3).contains(iStr) && !isInString){
                        // reduce tab
                        nowIndent[0]--;
                        nowIndent[1]--;
                    }
                }

                StringBuilder tab = new StringBuilder();
                tab.append("\t".repeat(Math.max(nowIndent[0], 0)));
                eachLine.insert(0, tab);
                eachLine.append('\n');

                nowIndent[0] = nowIndent[1];

                // replace String
                configStrArray[i] = eachLine.toString();
            }

            StringBuilder configStr = new StringBuilder();

            // connect whole String
            for(String content: configStrArray){
                configStr.append(content);
            }

        return configStr.toString();
    }
}
