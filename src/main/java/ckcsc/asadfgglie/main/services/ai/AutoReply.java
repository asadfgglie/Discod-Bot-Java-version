package ckcsc.asadfgglie.main.services.ai;

import ckcsc.asadfgglie.main.services.AbstractAI;
import ckcsc.asadfgglie.main.services.Register.Services;

public class AutoReply extends AbstractAI {
    // TODO: 去訓練一個自動回覆的 AI 模型，然後塞進來
    @Override
    public Services copy () {
        return new AutoReply();
    }
}
