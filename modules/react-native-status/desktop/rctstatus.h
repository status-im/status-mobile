/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#ifndef RCTSTATUS_H
#define RCTSTATUS_H

#include "moduleinterface.h"

#include <QLoggingCategory>
#include <QVariantMap>

Q_DECLARE_LOGGING_CATEGORY(RCTSTATUS)

class RCTStatusPrivate;
class RCTStatus : public QObject, public ModuleInterface {
  Q_OBJECT
  Q_INTERFACES(ModuleInterface)

  Q_DECLARE_PRIVATE(RCTStatus)

public:
  Q_INVOKABLE RCTStatus(QObject *parent = 0);
  ~RCTStatus();

  void setBridge(Bridge *bridge) override;

  QString moduleName() override;
  QList<ModuleMethod *> methodsToExport() override;
  QVariantMap constantsToExport() override;

  Q_INVOKABLE void shouldMoveToInternalStorage(double callbackId);
  Q_INVOKABLE void moveToInternalStorage(double callbackId);
  Q_INVOKABLE void initKeystore();
  Q_INVOKABLE void sendLogs(QString dbJSON, QString jsLogs, double callbackId);
  Q_INVOKABLE void exportLogs(double callbackId);
  Q_INVOKABLE void addPeer(QString enode, double callbackId);
  Q_INVOKABLE void prepareDirAndUpdateConfig(QString configString,
                                             double callbackId);
  Q_INVOKABLE void login(QString accountData, QString password);
  //  Q_INVOKABLE void loginWithKeycard(QString accountData, QString password,
  //                                    QString chatKey);
  Q_INVOKABLE void saveAccountAndLogin(QString accountData, QString password,
                                       QString config, QString subAccountsData);
  //  Q_INVOKABLE void saveAccountAndLoginWithKeycard(QString accountData,
  //                                                  QString password,
  //                                                  QString config,
  //                                                  QString chatKey);
  Q_INVOKABLE void logout();
  Q_INVOKABLE void openAccounts(double callbackId);
  Q_INVOKABLE void multiAccountStoreAccount(QString json, double callbackId);
  Q_INVOKABLE void multiAccountLoadAccount(QString json, double callbackId);
  Q_INVOKABLE void multiAccountReset(double callbackId);
  Q_INVOKABLE void multiAccountDeriveAddresses(QString json, double callbackId);
  Q_INVOKABLE void multiAccountImportMnemonic(QString json, double callbackId);
  Q_INVOKABLE void multiAccountStoreDerived(QString json, double callbackId);
  Q_INVOKABLE void multiAccountGenerateAndDeriveAddresses(QString json,
                                                          double callbackId);
  Q_INVOKABLE void verify(QString address, QString password, double callbackId);
  Q_INVOKABLE void sendTransaction(QString txArgsJSON, QString password,
                                   double callbackId);
  Q_INVOKABLE void signMessage(QString rpcParams, double callbackId);
  Q_INVOKABLE void signTypedData(QString data, QString account,
                                 QString password, double callbackId);

  Q_INVOKABLE void signGroupMembership(QString content, double callbackId);
  Q_INVOKABLE void extractGroupMembershipSignatures(QString signatures,
                                                    double callbackId);
  Q_INVOKABLE void getNodesFromContract(QString url, QString address,
                                        double callbackId);
  Q_INVOKABLE void chaosModeUpdate(bool on, double callbackId);

  Q_INVOKABLE void setAdjustResize();
  Q_INVOKABLE void setAdjustPan();
  Q_INVOKABLE void setSoftInputMode(int i);

  Q_INVOKABLE void clearCookies();
  Q_INVOKABLE void clearStorageAPIs();
  Q_INVOKABLE void callRPC(QString payload, double callbackId);
  Q_INVOKABLE void callPrivateRPC(QString payload, double callbackId);
  Q_INVOKABLE void closeApplication();
  Q_INVOKABLE void connectionChange(QString type, bool isExpensive);
  Q_INVOKABLE void appStateChange(QString type);

  Q_INVOKABLE static bool JSCEnabled();
  Q_INVOKABLE static void statusGoEventCallback(const char *event);

  Q_INVOKABLE QString identicon(QString publicKey);
  Q_INVOKABLE void identiconAsync(QString publicKey, double callbackId);
  Q_INVOKABLE QString generateAlias(QString publicKey);
  Q_INVOKABLE void generateAliasAsync(QString publicKey, double callbackId);
  Q_INVOKABLE void generateAliasAndIdenticonAsync(QString publicKey, double callbackId);

  void emitStatusGoEvent(QString event);

Q_SIGNALS:
  void statusGoEvent(QString event);

private Q_SLOTS:
  void onStatusGoEvent(QString event);

private:
  void logStatusGoResult(const char *methodName, const char *result);

  QString prepareDirAndUpdateConfig(QString configString);
  QScopedPointer<RCTStatusPrivate> d_ptr;
  QString d_gethLogFilePath;
};

#endif // RCTSTATUS_H
