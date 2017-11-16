package im.status.ethereum.module;

import com.github.status_im.status_go.cmd.Statusgo;

public class OttoJail implements Jail {
    @Override
    public void initJail(String initJs) {
        Statusgo.InitJail(initJs);
    }

    @Override
    public String parseJail(String chatId, String js) {
        return Statusgo.Parse(chatId, js);
    }

    @Override
    public String callJail(String chatId, String path, String params) {
        return Statusgo.Call(chatId, path, params);
    }

    @Override
    public void reset() {

    }
}
