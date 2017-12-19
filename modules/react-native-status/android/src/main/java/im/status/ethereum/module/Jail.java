package im.status.ethereum.module;

public interface Jail {
    void initJail(String initJs);
    String parseJail(String chatId, String js);
    String callJail(String chatId, String path, String params);
    void reset();
}
