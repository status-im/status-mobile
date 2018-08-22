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

#include <QVariantMap>

class RCTStatusPrivate;
class RCTStatus : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

    Q_DECLARE_PRIVATE(RCTStatus)

public:
    Q_INVOKABLE RCTStatus(QObject* parent = 0);
    ~RCTStatus();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

    Q_INVOKABLE void startNode(QString configString, QString fleet);
    Q_INVOKABLE void stopNode();
    Q_INVOKABLE void createAccount(QString password, double callbackId);
    Q_INVOKABLE void notifyUsers(QString token, QString payloadJSON, QString tokensJSON, double callbackId);
    Q_INVOKABLE void addPeer(QString enode, double callbackId);
    Q_INVOKABLE void recoverAccount(QString passphrase, QString password, double callbackId);
    Q_INVOKABLE void login(QString address, QString password, double callbackId);
    Q_INVOKABLE void sendTransaction(QString txArgsJSON, QString password, double callbackId);
    Q_INVOKABLE void signMessage(QString rpcParams, double callbackId);

    Q_INVOKABLE void setAdjustResize();
    Q_INVOKABLE void setAdjustPan();
    Q_INVOKABLE void setSoftInputMode(int i);

    Q_INVOKABLE void clearCookies();
    Q_INVOKABLE void clearStorageAPIs();
    Q_INVOKABLE void callRPC(QString payload, double callbackId);
    Q_INVOKABLE void callPrivateRPC(QString payload, double callbackId);
    Q_INVOKABLE void closeApplication();
    Q_INVOKABLE void getDeviceUUID(double callbackId);

    Q_INVOKABLE static bool JSCEnabled();
    Q_INVOKABLE static void statusGoEventCallback(const char* event);

    void emitStatusGoEvent(QString event);

Q_SIGNALS:
    void statusGoEvent(QString event);

private Q_SLOTS:
    void onStatusGoEvent(QString event);

private:
    QScopedPointer<RCTStatusPrivate> d_ptr;
};

#endif // RCTSTATUS_H
