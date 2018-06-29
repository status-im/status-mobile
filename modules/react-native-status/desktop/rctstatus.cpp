/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include "rctstatus.h"
#include "bridge.h"
#include "eventdispatcher.h"

#include <QDebug>
#include <QJsonDocument>
#include <QByteArray>
#include <QVariantMap>
#include <QDir>
#include <QStandardPaths>

#include "libstatus.h"

namespace {
struct RegisterQMLMetaType {
    RegisterQMLMetaType() {
        qRegisterMetaType<RCTStatus*>();
    }
} registerMetaType;
} // namespace

class RCTStatusPrivate {
public:
    static Bridge* bridge;
    static RCTStatus* rctStatus;
};

Bridge* RCTStatusPrivate::bridge = nullptr;
RCTStatus* RCTStatusPrivate::rctStatus = nullptr;

RCTStatus::RCTStatus(QObject* parent) : QObject(parent), d_ptr(new RCTStatusPrivate) {
    RCTStatusPrivate::rctStatus = this;
    SetSignalEventCallback((void*)&RCTStatus::jailSignalEventCallback);
    connect(this, &RCTStatus::jailSignalEvent, this, &RCTStatus::onJailSignalEvent);
}

RCTStatus::~RCTStatus() {}

void RCTStatus::setBridge(Bridge* bridge) {
    Q_D(RCTStatus);
    d->bridge = bridge;
}

QString RCTStatus::moduleName() {
    return "Status";
}

QList<ModuleMethod*> RCTStatus::methodsToExport() {
    return QList<ModuleMethod*>{};
}

QVariantMap RCTStatus::constantsToExport() {
    return QVariantMap();
}

void RCTStatus::initJail(QString js, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::initJail with param js:" << " and callback id: " << callbackId;

    InitJail(js.toUtf8().data());

    d->bridge->invokePromiseCallback(callbackId, QVariantList{ "{\"result\":\"\"}" });
}


void RCTStatus::parseJail(QString chatId, QString js, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::parseJail with param chatId: " << chatId << " js:" << " and callback id: " << callbackId;

    const char* result = Parse(chatId.toUtf8().data(), js.toUtf8().data());
    qDebug() << "RCTStatus::parseJail parseJail result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::callJail(QString chatId, QString path, QString params, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::callJail with param chatId: " << chatId << " path: " << path << " params: " << params <<  " and callback id: " << callbackId;

    const char* result = Call(chatId.toUtf8().data(), path.toUtf8().data(), params.toUtf8().data());
    qDebug() << "RCTStatus::callJail callJail result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::getDeviceUUID(double callbackId) {
  Q_D(RCTStatus);
  qDebug() << "call of RCTStatus::getDeviceUUID";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{"com.status.StatusIm"});
}


void RCTStatus::startNode(QString configString) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::startNode with param configString:" << configString;

    QJsonParseError jsonError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(configString.toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        qDebug() << jsonError.errorString();
    }

    qDebug() << " RCTStatus::startNode configString: " << jsonDoc.toVariant().toMap();
    QVariantMap configJSON = jsonDoc.toVariant().toMap();

    QString newKeystoreUrl = "keystore";

    int networkId = configJSON["NetworkId"].toInt();
    QString dataDir = configJSON["DataDir"].toString();

    QString networkDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation) + "/" + dataDir;
    QDir dir(networkDir);
    if (!dir.exists()) {
      dir.mkpath(".");
    }
    qDebug()<<"RCTStatus::startNode networkDir: "<<networkDir;


    char *configChars = GenerateConfig(networkDir.toUtf8().data(), networkId);
    qDebug() << "RCTStatus::startNode GenerateConfig result: " << configChars;

    jsonDoc = QJsonDocument::fromJson(QString(configChars).toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        qDebug() << jsonError.errorString();
    }

    qDebug() << " RCTStatus::startNode GenerateConfig configString: " << jsonDoc.toVariant().toMap();
    QVariantMap generatedConfig = jsonDoc.toVariant().toMap();
    generatedConfig["KeyStoreDir"] = newKeystoreUrl;
    generatedConfig["LogEnabled"] = true;
    generatedConfig["LogFile"] = networkDir + "/geth.log";
    //generatedConfig["LogLevel"] = "DEBUG";

    const char* result = StartNode(QString(QJsonDocument::fromVariant(generatedConfig).toJson(QJsonDocument::Compact)).toUtf8().data());
    qDebug() << "RCTStatus::startNode StartNode result: " << result;
}


void RCTStatus::shouldMoveToInternalStorage(double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::shouldMoveToInternalStorage with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{});
}


void RCTStatus::moveToInternalStorage(double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::moveToInternalStorage with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{ "{\"result\":\"\"}" });
}


void RCTStatus::stopNode() {
    qDebug() << "call of RCTStatus::stopNode";
    const char* result = StopNode();
    qDebug() << "RCTStatus::stopNode StopNode result: " << result;
}


void RCTStatus::createAccount(QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::createAccount with param callbackId: " << callbackId;
    const char* result = CreateAccount(password.toUtf8().data());
    qDebug() << "RCTStatus::createAccount CreateAccount result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::notifyUsers(QString token, QString payloadJSON, QString tokensJSON, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::notifyUsers with param callbackId: " << callbackId;
    const char* result = NotifyUsers(token.toUtf8().data(), payloadJSON.toUtf8().data(), tokensJSON.toUtf8().data());
    qDebug() << "RCTStatus::notifyUsers Notify result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::addPeer(QString enode, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::addPeer with param callbackId: " << callbackId;
    const char* result = AddPeer(enode.toUtf8().data());
    qDebug() << "RCTStatus::addPeer AddPeer result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::recoverAccount(QString passphrase, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::recoverAccount with param callbackId: " << callbackId;
    const char* result = RecoverAccount(password.toUtf8().data(), passphrase.toUtf8().data());
    qDebug() << "RCTStatus::recoverAccount RecoverAccount result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::login(QString address, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::login with param callbackId: " << callbackId;
    const char* result = Login(address.toUtf8().data(), password.toUtf8().data());
    qDebug() << "RCTStatus::login Login result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::approveSignRequests(QString hashes, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::approveSignRequests with param callbackId: " << callbackId;
    const char* result = ApproveSignRequests(hashes.toUtf8().data(), password.toUtf8().data());
    qDebug() << "RCTStatus::approveSignRequests CompleteTransactions result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::discardSignRequest(QString id) {
    qDebug() << "call of RCTStatus::discardSignRequest with id: " << id;
    DiscardSignRequest(id.toUtf8().data());
}

void RCTStatus::setAdjustResize() {
}


void RCTStatus::setAdjustPan() {
}


void RCTStatus::setSoftInputMode(int i) {
}



void RCTStatus::clearCookies() {
}


void RCTStatus::clearStorageAPIs() {
}


void RCTStatus::sendWeb3Request(QString payload, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::sendWeb3Request with param callbackId: " << callbackId;
    const char* result = CallRPC(payload.toUtf8().data());
    qDebug() << "RCTStatus::sendWeb3Request CallRPC result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::sendWeb3PrivateRequest(QString payload, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::sendWeb3PrivateRequest with param callbackId: " << callbackId;
    const char* result = CallPrivateRPC(payload.toUtf8().data());
    qDebug() << "RCTStatus::sendWeb3PrivateRequest CallPrivateRPC result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::closeApplication() {
}

bool RCTStatus::JSCEnabled() {
    qDebug() << "call of RCTStatus::JSCEnabled";
    return false;
}

void RCTStatus::jailSignalEventCallback(const char* signal) {
    qDebug() << "call of RCTStatus::jailSignalEventCallback ... signal: " << signal;
    RCTStatusPrivate::rctStatus->emitSignalEvent(signal);
}

void RCTStatus::emitSignalEvent(const char* signal) {
    qDebug() << "call of RCTStatus::emitSignalEvent ... signal: " << signal;
    Q_EMIT jailSignalEvent(signal);
}

void RCTStatus::onJailSignalEvent(const char* signal) {
    qDebug() << "call of RCTStatus::onJailSignalEvent ... signal: " << signal;
    RCTStatusPrivate::bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", signal}});
}
