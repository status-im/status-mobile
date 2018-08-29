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
    SetSignalEventCallback((void*)&RCTStatus::statusGoEventCallback);
    connect(this, &RCTStatus::statusGoEvent, this, &RCTStatus::onStatusGoEvent);
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

void RCTStatus::getDeviceUUID(double callbackId) {
  Q_D(RCTStatus);
  qDebug() << "call of RCTStatus::getDeviceUUID";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{"com.status.StatusIm"});
}


void RCTStatus::startNode(QString configString, QString fleet) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::startNode with param configString:" << configString;

    QJsonParseError jsonError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(configString.toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        qDebug() << jsonError.errorString();
    }

    qDebug() << " RCTStatus::startNode configString: " << jsonDoc.toVariant().toMap();
    QVariantMap configJSON = jsonDoc.toVariant().toMap();

    int networkId = configJSON["NetworkId"].toInt();
    QString dataDir = configJSON["DataDir"].toString();

    QString rootDirPath = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation) + "/";
    QString networkDir = rootDirPath + dataDir;
    QString keyStoreDir = rootDirPath + "keystore";
    QDir dir(networkDir);
    if (!dir.exists()) {
      dir.mkpath(".");
    }
    qDebug()<<"RCTStatus::startNode networkDir: "<<networkDir;


    char *configChars = GenerateConfig(networkDir.toUtf8().data(), fleet.toUtf8().data(), networkId);
    qDebug() << "RCTStatus::startNode GenerateConfig result: " << configChars;

    jsonDoc = QJsonDocument::fromJson(QString(configChars).toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        qDebug() << jsonError.errorString();
    }

    qDebug() << " RCTStatus::startNode GenerateConfig configString: " << jsonDoc.toVariant().toMap();
    QVariantMap generatedConfig = jsonDoc.toVariant().toMap();
    generatedConfig["KeyStoreDir"] = keyStoreDir;
    generatedConfig["LogEnabled"] = true;
    generatedConfig["LogFile"] = networkDir + "/geth.log";
    generatedConfig["ClusterConfig.Fleet"] = fleet;
    //generatedConfig["LogLevel"] = "DEBUG";

    const char* result = StartNode(QString(QJsonDocument::fromVariant(generatedConfig).toJson(QJsonDocument::Compact)).toUtf8().data());
    qDebug() << "RCTStatus::startNode StartNode result: " << result;
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


void RCTStatus::sendTransaction(QString txArgsJSON, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::sendTransaction with param callbackId: " << callbackId;
    const char* result = SendTransaction(txArgsJSON.toUtf8().data(), password.toUtf8().data());
    qDebug() << "RCTStatus::sendTransaction SendTransaction result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}


void RCTStatus::signMessage(QString rpcParams, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::signMessage with param callbackId: " << callbackId;
    const char* result = SignMessage(rpcParams.toUtf8().data());
    qDebug() << "RCTStatus::signMessage SignMessage result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
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


void RCTStatus::callRPC(QString payload, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::callRPC with param callbackId: " << callbackId;
    const char* result = CallRPC(payload.toUtf8().data());
    qDebug() << "RCTStatus::callRPC CallRPC result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::callPrivateRPC(QString payload, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::callPrivateRPC with param callbackId: " << callbackId;
    const char* result = CallPrivateRPC(payload.toUtf8().data());
    qDebug() << "RCTStatus::callPrivateRPC CallPrivateRPC result: " << result;
    d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::closeApplication() {
}

bool RCTStatus::JSCEnabled() {
    qDebug() << "call of RCTStatus::JSCEnabled";
    return false;
}

void RCTStatus::statusGoEventCallback(const char* event) {
    qDebug() << "call of RCTStatus::statusGoEventCallback ... event: " << event;
    RCTStatusPrivate::rctStatus->emitStatusGoEvent(event);
}

void RCTStatus::emitStatusGoEvent(QString event) {
    qDebug() << "call of RCTStatus::emitStatusGoEvent ... event: " << event;
    Q_EMIT statusGoEvent(event);
}

void RCTStatus::onStatusGoEvent(QString event) {
    qDebug() << "call of RCTStatus::onStatusGoEvent ... event: " << event.toUtf8().data();
    RCTStatusPrivate::bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", event.toUtf8().data()}});
}
