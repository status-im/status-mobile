#include <jni.h>
#include <string.h>
#include "nim_status.h"

static JavaVM* javaVM;
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
  javaVM = vm;
  NimMain();
  return  JNI_VERSION_1_6;
}

jstring Java_im_status_NimStatus_hashMessage(JNIEnv* env, jobject thiz, jstring jmessage) {
  const char * message = (*env)->GetStringUTFChars(env, jmessage, 0);
  const char * result = hashMessage(message);

  (*env)->ReleaseStringUTFChars(env, jmessage, message);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_initKeystore(JNIEnv* env, jobject thiz, jstring jkeydir) {
  const char * keydir = (*env)->GetStringUTFChars(env, jkeydir, 0);
  const char * result = initKeystore(keydir);

  (*env)->ReleaseStringUTFChars(env, jkeydir, keydir);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_openAccounts(JNIEnv* env, jobject thiz, jstring jdatadir) {
  const char * datadir = (*env)->GetStringUTFChars(env, jdatadir, 0);
  const char * result = openAccounts(datadir);

  (*env)->ReleaseStringUTFChars(env, jdatadir, datadir);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountGenerateAndDeriveAddresses(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountGenerateAndDeriveAddresses(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountStoreDerivedAccounts(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountStoreDerivedAccounts(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountImportMnemonic(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountImportMnemonic(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountImportPrivateKey(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountImportPrivateKey(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountDeriveAddresses(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountDeriveAddresses(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_saveAccountAndLogin(JNIEnv* env, jobject thiz, jstring jaccountData, jstring jpassword, jstring jsettingsJSON, jstring jconfigJSON, jstring jsubaccountData) {

  const char * accountData = (*env)->GetStringUTFChars(env, jaccountData, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * settingsJSON = (*env)->GetStringUTFChars(env, jsettingsJSON, 0);
  const char * configJSON = (*env)->GetStringUTFChars(env, jconfigJSON, 0);
  const char * subaccountData = (*env)->GetStringUTFChars(env, jsubaccountData, 0);
  const char * result = saveAccountAndLogin(accountData, password, settingsJSON, configJSON, subaccountData);

  (*env)->ReleaseStringUTFChars(env, jaccountData, accountData);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);
  (*env)->ReleaseStringUTFChars(env, jsettingsJSON, settingsJSON);
  (*env)->ReleaseStringUTFChars(env, jconfigJSON, configJSON);
  (*env)->ReleaseStringUTFChars(env, jsubaccountData, subaccountData);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_callRPC(JNIEnv* env, jobject thiz, jstring jinputJSON) {
  const char * inputJSON = (*env)->GetStringUTFChars(env, jinputJSON, 0);
  const char * result = callRPC(inputJSON);

  (*env)->ReleaseStringUTFChars(env, jinputJSON, inputJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_callPrivateRPC(JNIEnv* env, jobject thiz, jstring jinputJSON) {
  const char * inputJSON = (*env)->GetStringUTFChars(env, jinputJSON, 0);
  const char * result = callPrivateRPC(inputJSON);

  (*env)->ReleaseStringUTFChars(env, jinputJSON, inputJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_addPeer(JNIEnv* env, jobject thiz, jstring jpeer) {
  const char * peer = (*env)->GetStringUTFChars(env, jpeer, 0);
  const char * result = addPeer(peer);

  (*env)->ReleaseStringUTFChars(env, jpeer, peer);

  return (*env)->NewStringUTF(env, result);
}

static jobject statusModule = NULL;

void signalCallback(char * msg) {
  JNIEnv *env;
  jint res = (*javaVM)->GetEnv(javaVM, (void**)&env, JNI_VERSION_1_6);
  if (res != JNI_OK) {
      res = (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);
      if (JNI_OK != res) {
          //LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
          return;
      }
  }

  jmethodID handleSignalMethodId = (*env)->GetMethodID(env, 
      (*env)->GetObjectClass(env, statusModule), 
      "handleSignal", "(Ljava/lang/String;)V");
  jstring jsonEventStr = (*env)->NewStringUTF(env, msg);
  (*env)->CallVoidMethod(env, statusModule, handleSignalMethodId, jsonEventStr);
  (*env)->DeleteLocalRef(env, jsonEventStr);

  (*javaVM)->DetachCurrentThread(javaVM);
}

void Java_im_status_NimStatus_setSignalEventCallback(JNIEnv* env, 
    jobject thiz, jobject jStatusModule) {
  if (statusModule == NULL) {
    statusModule = (*env)->NewGlobalRef(env, jStatusModule);
  }
  setSignalEventCallback(&signalCallback);
}

jstring Java_im_status_NimStatus_sendTransaction(JNIEnv* env, jobject thiz, jstring jjsonArgs, jstring jpassword) {

  const char * jsonArgs = (*env)->GetStringUTFChars(env, jjsonArgs, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * result = sendTransaction(jsonArgs, password);

  (*env)->ReleaseStringUTFChars(env, jjsonArgs, jsonArgs);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_generateAlias(JNIEnv* env, jobject thiz, jstring jpublicKey) {

  const char * publicKey = (*env)->GetStringUTFChars(env, jpublicKey, 0);
  const char * result = generateAlias(publicKey);
  (*env)->ReleaseStringUTFChars(env, jpublicKey, publicKey);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_identicon(JNIEnv* env, jobject thiz, jstring jpublicKey) {
  if (jpublicKey == NULL) {
    return (*env)->NewStringUTF(env, "");
  }
  const char * publicKey = (*env)->GetStringUTFChars(env, jpublicKey, 0);
  const char * result = identicon(publicKey);
  (*env)->ReleaseStringUTFChars(env, jpublicKey, publicKey);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_login(JNIEnv* env, jobject thiz, jstring jaccountData, jstring jpassword) {
  const char * accountData = (*env)->GetStringUTFChars(env, jaccountData, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * result = login(accountData, password);

  (*env)->ReleaseStringUTFChars(env, jaccountData, accountData);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_logout(JNIEnv* env, jobject thiz) {
  const char * result = logout();

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_verifyAccountPassword(JNIEnv* env, jobject thiz, jstring jkeyStoreDir, jstring jaddress, jstring jpassword) {
  const char * keyStoreDir = (*env)->GetStringUTFChars(env, jkeyStoreDir, 0);
  const char * address = (*env)->GetStringUTFChars(env, jaddress, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * result = verifyAccountPassword(keyStoreDir, address, password);

  (*env)->ReleaseStringUTFChars(env, jkeyStoreDir, keyStoreDir);
  (*env)->ReleaseStringUTFChars(env, jaddress, address);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_validateMnemonic(JNIEnv* env, jobject thiz, jstring jmnemonic) {
  const char * mnemonic = (*env)->GetStringUTFChars(env, jmnemonic, 0);
  const char * result = validateMnemonic(mnemonic);

  (*env)->ReleaseStringUTFChars(env, jmnemonic, mnemonic);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_recoverAccount(JNIEnv* env, jobject thiz, jstring jpassword, jstring jmnemonic) {

  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * mnemonic = (*env)->GetStringUTFChars(env, jmnemonic, 0);
  const char * result = recoverAccount(password, mnemonic);

  (*env)->ReleaseStringUTFChars(env, jpassword, password);
  (*env)->ReleaseStringUTFChars(env, jmnemonic, mnemonic);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_startOnboarding(JNIEnv* env, jobject thiz, jint jn, jint jmnemonicPhraseLength) {
  const char * result = startOnboarding(jn, jmnemonicPhraseLength);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_saveAccountAndLoginWithKeycard(JNIEnv* env, jobject thiz, jstring jaccountData, jstring jpassword, jstring jsettingsJSON, jstring jconfigJSON, jstring jsubaccountData, jstring jkeyHex) {

  const char * accountData = (*env)->GetStringUTFChars(env, jaccountData, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * settingsJSON = (*env)->GetStringUTFChars(env, jsettingsJSON, 0);
  const char * configJSON = (*env)->GetStringUTFChars(env, jconfigJSON, 0);
  const char * subaccountData = (*env)->GetStringUTFChars(env, jsubaccountData, 0);
  const char * keyHex = (*env)->GetStringUTFChars(env, jkeyHex, 0);

  const char * result = saveAccountAndLoginWithKeycard(accountData, 
                                                       password,
                                                       settingsJSON,
                                                       configJSON,
                                                       subaccountData,
                                                       keyHex);

  (*env)->ReleaseStringUTFChars(env, jaccountData, accountData);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);
  (*env)->ReleaseStringUTFChars(env, jsettingsJSON, settingsJSON);
  (*env)->ReleaseStringUTFChars(env, jconfigJSON, configJSON);
  (*env)->ReleaseStringUTFChars(env, jsubaccountData, subaccountData);
  (*env)->ReleaseStringUTFChars(env, jkeyHex, keyHex);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_hashTransaction(JNIEnv* env, jobject thiz, jstring jtxArgsJSON) {
  const char * txArgsJSON = (*env)->GetStringUTFChars(env, jtxArgsJSON, 0);
  const char * result = hashTransaction(txArgsJSON);

  (*env)->ReleaseStringUTFChars(env, jtxArgsJSON, txArgsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_extractGroupMembershipSignatures(JNIEnv* env, jobject thiz, jstring jsignaturePairsStr) {
  const char * signaturePairsStr = (*env)->GetStringUTFChars(env, jsignaturePairsStr, 0);
  const char * result = extractGroupMembershipSignatures(signaturePairsStr);

  (*env)->ReleaseStringUTFChars(env, jsignaturePairsStr, signaturePairsStr);

  return (*env)->NewStringUTF(env, result);
}

void Java_im_status_NimStatus_connectionChange(JNIEnv* env, jobject thiz, jstring jtyp, jstring jexpensive) {
  const char * typ = (*env)->GetStringUTFChars(env, jtyp, 0);
  const char * expensive = (*env)->GetStringUTFChars(env, jexpensive, 0);

  connectionChange(typ, expensive);

  (*env)->ReleaseStringUTFChars(env, jtyp, typ);
  (*env)->ReleaseStringUTFChars(env, jexpensive, expensive);
}

jstring Java_im_status_NimStatus_multiformatSerializePublicKey(JNIEnv* env, jobject thiz, jstring jkey, jstring joutBase) {

  const char * key = (*env)->GetStringUTFChars(env, jkey, 0);
  const char * outBase = (*env)->GetStringUTFChars(env, joutBase, 0);
  const char * result = multiformatSerializePublicKey(key, outBase);

  (*env)->ReleaseStringUTFChars(env, jkey, key);
  (*env)->ReleaseStringUTFChars(env, joutBase, outBase);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiformatDeserializePublicKey(JNIEnv* env, jobject thiz, jstring jkey, jstring joutBase) {

  const char * key = (*env)->GetStringUTFChars(env, jkey, 0);
  const char * outBase = (*env)->GetStringUTFChars(env, joutBase, 0);
  const char * result = multiformatDeserializePublicKey(key, outBase);

  (*env)->ReleaseStringUTFChars(env, jkey, key);
  (*env)->ReleaseStringUTFChars(env, joutBase, outBase);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_validateNodeConfig(JNIEnv* env, jobject thiz, jstring jconfigJSON) {
  const char * configJSON = (*env)->GetStringUTFChars(env, jconfigJSON, 0);
  const char * result = validateNodeConfig(configJSON);

  (*env)->ReleaseStringUTFChars(env, jconfigJSON, configJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_loginWithKeycard(JNIEnv* env, jobject thiz, jstring jaccountData, jstring jpassword, jstring jkeyHex) {

  const char * accountData = (*env)->GetStringUTFChars(env, jaccountData, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * keyHex = (*env)->GetStringUTFChars(env, jkeyHex, 0);

  const char * result = loginWithKeycard(accountData, password, keyHex);

  (*env)->ReleaseStringUTFChars(env, jaccountData, accountData);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);
  (*env)->ReleaseStringUTFChars(env, jkeyHex, keyHex);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_recover(JNIEnv* env, jobject thiz, jstring jrpcParams) {
  const char * rpcParams = (*env)->GetStringUTFChars(env, jrpcParams, 0);
  const char * result = recover(rpcParams);

  (*env)->ReleaseStringUTFChars(env, jrpcParams, rpcParams);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_writeHeapProfile(JNIEnv* env, jobject thiz, jstring jdataDir) {
  const char * dataDir = (*env)->GetStringUTFChars(env, jdataDir, 0);
  const char * result = writeHeapProfile(dataDir);

  (*env)->ReleaseStringUTFChars(env, jdataDir, dataDir);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_importOnboardingAccount(JNIEnv* env, jobject thiz, jstring jid, jstring jpassword) {
  const char * id = (*env)->GetStringUTFChars(env, jid, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);

  const char * result = importOnboardingAccount(id, password);

  (*env)->ReleaseStringUTFChars(env, jid, id);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

void Java_im_status_NimStatus_removeOnboarding(JNIEnv* env, jobject thiz) {
  removeOnboarding();
}

jstring Java_im_status_NimStatus_hashTypedData(JNIEnv* env, jobject thiz, jstring jdata) {
  const char * data = (*env)->GetStringUTFChars(env, jdata, 0);
  const char * result = hashTypedData(data);

  (*env)->ReleaseStringUTFChars(env, jdata, data);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_resetChainData(JNIEnv* env, jobject thiz) {
  const char * result = resetChainData();
  return (*env)->NewStringUTF(env, result);
} 

jstring Java_im_status_NimStatus_signMessage(JNIEnv* env, jobject thiz, jstring jrpcParams) {
  const char * rpcParams = (*env)->GetStringUTFChars(env, jrpcParams, 0);
  const char * result = signMessage(rpcParams);

  (*env)->ReleaseStringUTFChars(env, jrpcParams, rpcParams);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_signTypedData(JNIEnv* env, jobject thiz, jstring jdata, jstring jaddress, jstring jpassword) {

  const char * data = (*env)->GetStringUTFChars(env, jdata, 0);
  const char * address = (*env)->GetStringUTFChars(env, jaddress, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);

  const char * result = signTypedData(data, address, password);

  (*env)->ReleaseStringUTFChars(env, jdata, data);
  (*env)->ReleaseStringUTFChars(env, jaddress, address);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_stopCPUProfiling(JNIEnv* env, jobject thiz) {
  const char * result = stopCPUProfiling();

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_getNodesFromContract(JNIEnv* env, jobject thiz, jstring jrpcEndpoint, jstring jcontractAddress) {
  const char * rpcEndpoint = (*env)->GetStringUTFChars(env, jrpcEndpoint, 0);
  const char * contractAddress = (*env)->GetStringUTFChars(env, jcontractAddress, 0);

  const char * result = getNodesFromContract(rpcEndpoint, contractAddress);

  (*env)->ReleaseStringUTFChars(env, jrpcEndpoint, rpcEndpoint);
  (*env)->ReleaseStringUTFChars(env, jcontractAddress, contractAddress);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_exportNodeLogs(JNIEnv* env, jobject thiz) {
  const char * result = exportNodeLogs();
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_chaosModeUpdate(JNIEnv* env, jobject thiz, jint on) {
  const char * result = chaosModeUpdate(on);
  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_signHash(JNIEnv* env, jobject thiz, jstring jhexEncodedHash) {
  const char * hexEncodedHash = (*env)->GetStringUTFChars(env, jhexEncodedHash, 0);
  const char * result = signHash(hexEncodedHash);

  (*env)->ReleaseStringUTFChars(env, jhexEncodedHash, hexEncodedHash);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_createAccount(JNIEnv* env, jobject thiz, jstring jpassword) {
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * result = createAccount(password);

  (*env)->ReleaseStringUTFChars(env, jpassword, password);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_sendTransactionWithSignature(JNIEnv* env, jobject thiz, jstring jtxtArgsJSON, jstring jsigString) {
  const char * txtArgsJSON = (*env)->GetStringUTFChars(env, jtxtArgsJSON, 0);
  const char * sigString = (*env)->GetStringUTFChars(env, jsigString, 0);

  const char * result = sendTransactionWithSignature(txtArgsJSON, sigString);

  (*env)->ReleaseStringUTFChars(env, jtxtArgsJSON, txtArgsJSON);
  (*env)->ReleaseStringUTFChars(env, jsigString, sigString);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_startCPUProfile(JNIEnv* env, jobject thiz, jstring jdataDir) {
  const char * dataDir = (*env)->GetStringUTFChars(env, jdataDir, 0);
  const char * result = startCPUProfile(dataDir);

  (*env)->ReleaseStringUTFChars(env, jdataDir, dataDir);

  return (*env)->NewStringUTF(env, result);
}

void Java_im_status_NimStatus_appStateChange(JNIEnv* env, jobject thiz, jstring jstate) {
  const char * state = (*env)->GetStringUTFChars(env, jstate, 0);

  appStateChange(state);

  (*env)->ReleaseStringUTFChars(env, jstate, state);
}

jstring Java_im_status_NimStatus_signGroupMembership(JNIEnv* env, jobject thiz, jstring jcontent) {
  const char * content = (*env)->GetStringUTFChars(env, jcontent, 0);
  const char * result = signGroupMembership(content);

  (*env)->ReleaseStringUTFChars(env, jcontent, content);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountStoreAccount(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountStoreAccount(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountLoadAccount(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountLoadAccount(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountGenerate(JNIEnv* env, jobject thiz, jstring jparamsJSON) {
  const char * paramsJSON = (*env)->GetStringUTFChars(env, jparamsJSON, 0);
  const char * result = multiAccountGenerate(paramsJSON);

  (*env)->ReleaseStringUTFChars(env, jparamsJSON, paramsJSON);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_deleteMultiaccount(JNIEnv* env, jobject thiz, 
                                                    jstring jkeyUID,
                                                    jstring jpath) {
  const char * keyUID = (*env)->GetStringUTFChars(env, jkeyUID, 0);
  const char * path = (*env)->GetStringUTFChars(env, jpath, 0);
  const char * result = deleteMultiAccount(keyUID, path);

  (*env)->ReleaseStringUTFChars(env, jkeyUID, keyUID);
  (*env)->ReleaseStringUTFChars(env, jpath, path);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_migrateKeyStoreDir(JNIEnv* env, jobject thiz, 
                                                    jstring jaccountData, 
                                                    jstring jpassword, 
                                                    jstring joldKeystoreDir,
                                                    jstring jmultiaccountKeystoreDir) {
  const char * accountData = (*env)->GetStringUTFChars(env, jaccountData, 0);
  const char * password = (*env)->GetStringUTFChars(env, jpassword, 0);
  const char * oldKeystoreDir = (*env)->GetStringUTFChars(env, joldKeystoreDir, 0);
  const char * multiaccountKeystoreDir = (*env)->GetStringUTFChars(env, jmultiaccountKeystoreDir, 0);

  const char * result = migrateKeyStoreDir(accountData, password, oldKeystoreDir, multiaccountKeystoreDir);

  (*env)->ReleaseStringUTFChars(env, jaccountData, accountData);
  (*env)->ReleaseStringUTFChars(env, jpassword, password);
  (*env)->ReleaseStringUTFChars(env, joldKeystoreDir, oldKeystoreDir);
  (*env)->ReleaseStringUTFChars(env, jmultiaccountKeystoreDir, multiaccountKeystoreDir);

  return (*env)->NewStringUTF(env, result);
}

jstring Java_im_status_NimStatus_multiAccountReset(JNIEnv* env, jobject thiz) {
  const char * result = multiAccountReset();

  return (*env)->NewStringUTF(env, result);
}

void Java_im_status_NimStatus_startWallet(JNIEnv* env, jobject thiz) {
  startWallet();
}

void Java_im_status_NimStatus_stopWallet(JNIEnv* env, jobject thiz) {
  stopWallet();
}
