package im.status;





public class NimStatus {

  public NimStatus() {

  }
  
  public native String hashMessage(String message);
  public native String initKeystore(String keydir);
  public native String openAccounts(String datadir);
  public native String multiAccountGenerateAndDeriveAddresses(String paramsJSON);
  public native String multiAccountStoreDerivedAccounts(String paramsJSON);
  public native String multiAccountImportMnemonic(String paramsJSON);
  public native String multiAccountImportPrivateKey(String paramsJSON);
  public native String multiAccountDeriveAddresses(String paramsJSON);
  public native String saveAccountAndLogin(String accountData, String password, String settingsJSON, String configJSON, String subaccountData);
  public native String callRPC(String inputJSON);
  public native String callPrivateRPC(String inputJSON);
  public native String addPeer(String peer);
  public native void setSignalEventCallback(Object obj);
  public native String sendTransaction(String jsonArgs, String password);
  public native String generateAlias(String pk);
  public native String identicon(String pk);
  public native String login(String accountData, String password);
  public native String logout();
  public native String verifyAccountPassword(String keyStoreDir, String address, String password);
  public native String validateMnemonic(String mnemonic);
  public native String recoverAccount(String password, String mnemonic);
  public native String startOnboarding(int n, int mnemonicPhraseLength);
  public native String saveAccountAndLoginWithKeycard(String accountData, String password, String settingsJSON, String configJSON, String subaccountData, String keyHex);
  public native String hashTransaction(String txArgsJSON);
  public native String extractGroupMembershipSignatures(String signaturePairsStr);
  public native void connectionChange(String typ, String expensive);
  public native String multiformatSerializePublicKey(String key, String outBase);
  public native String multiformatDeserializePublicKey(String key, String outBase);
  public native String validateNodeConfig(String configJSON);
  public native String loginWithKeycard(String accountData, String password, String keyHex);
  public native String recover(String rpcParams);
  public native String writeHeapProfile(String dataDir);
  public native String importOnboardingAccount(String id, String password);
  public native void removeOnboarding();
  public native String hashTypedData(String data);
  public native String resetChainData();
  public native String signMessage(String rpcParams);
  public native String signTypedData(String data, String address, String password);
  public native String stopCPUProfiling();
  public native String getNodesFromContract(String rpcEndpoint, String contractAddress);
  public native String exportNodeLogs();
  public native String chaosModeUpdate(int on);
  public native String signHash(String hexEncodedHash);
  public native String createAccount(String password);
  public native String sendTransactionWithSignature(String txtArgsJSON, String sigString);
  public native String startCPUProfile(String dataDir);
  public native void appStateChange(String state);
  public native String signGroupMembership(String content);
  public native String multiAccountStoreAccount(String paramsJSON);
  public native String multiAccountLoadAccount(String paramsJSON);
  public native String multiAccountGenerate(String paramsJSON);
  public native String multiAccountReset();
  public native String deleteMultiaccount(String keyUID, String path);
  public native String migrateKeyStoreDir(String accountData, String password, String oldKeystoreDir, String multiaccountKeystoreDir);
  public native void startWallet();
  public native void stopWallet();
}
