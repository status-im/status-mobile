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
#include "logger.h"

#include <QDebug>
#include <QJsonDocument>
#include <QByteArray>
#include <QVariantMap>
#include <QDir>
#include <QStandardPaths>
#include <QtConcurrent>

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
const QString RCTSTATUS = "RCTStatus";

RCTStatus::RCTStatus(QObject* parent) : QObject(parent), d_ptr(new RCTStatusPrivate) {
    RCTStatusPrivate::rctStatus = this;
    SetSignalEventCallback((void*)&RCTStatus::statusGoEventCallback);
    connect(this, &RCTStatus::statusGoEvent, this, &RCTStatus::onStatusGoEvent);
    registerLogModule(RCTSTATUS);
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
  rnLog(RCTSTATUS) << "::getDeviceUUID call";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{"com.status.StatusIm"});
}


void RCTStatus::startNode(QString configString) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::startNode call with param configString:" << configString;

    QJsonParseError jsonError;
    const QJsonDocument& jsonDoc = QJsonDocument::fromJson(configString.toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        rnLog(RCTSTATUS) << jsonError.errorString();
    }

    QVariantMap configJSON = jsonDoc.toVariant().toMap();
    rnLog(RCTSTATUS) << "::startNode configString: " << configJSON;

    int networkId = configJSON["NetworkId"].toInt();
    QString relativeDataDirPath = configJSON["DataDir"].toString();
    if (!relativeDataDirPath.startsWith("/"))
        relativeDataDirPath.prepend("/");

    QString rootDirPath = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QDir rootDir(rootDirPath);
    QString absDataDirPath = rootDirPath + relativeDataDirPath;
    QDir dataDir(absDataDirPath);
    if (!dataDir.exists()) {
      dataDir.mkpath(".");
    }

    configJSON["DataDir"] = absDataDirPath;
    configJSON["BackupDisabledDataDir"] = absDataDirPath;
    configJSON["KeyStoreDir"] = rootDir.absoluteFilePath("keystore");
    configJSON["LogFile"] = dataDir.absoluteFilePath("geth.log");

    const QJsonDocument& updatedJsonDoc = QJsonDocument::fromVariant(configJSON);
    rnLog(RCTSTATUS) << "::startNode updated configString: " << updatedJsonDoc.toVariant().toMap();
    const char* result = StartNode(QString(updatedJsonDoc.toJson(QJsonDocument::Compact)).toUtf8().data());
    rnLog(RCTSTATUS) << "::startNode StartNode result: " << statusGoResultError(result);
}


void RCTStatus::stopNode() {
    rnLog(RCTSTATUS) << "::stopNode call";
    const char* result = StopNode();
    rnLog(RCTSTATUS) << "::stopNode StopNode result: " << statusGoResultError(result);
}


void RCTStatus::createAccount(QString password, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::createAccount call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString password, double callbackId) {
            const char* result = CreateAccount(password.toUtf8().data());
            rnLog(RCTSTATUS) << "::createAccount CreateAccount result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, password, callbackId);
}


void RCTStatus::notifyUsers(QString token, QString payloadJSON, QString tokensJSON, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::notifyUsers call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString token, QString payloadJSON, QString tokensJSON, double callbackId) {
            const char* result = NotifyUsers(token.toUtf8().data(), payloadJSON.toUtf8().data(), tokensJSON.toUtf8().data());
            rnLog(RCTSTATUS) << "::notifyUsers Notify result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, token, payloadJSON, tokensJSON, callbackId);
}


void RCTStatus::addPeer(QString enode, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::addPeer call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString enode, double callbackId) {
            const char* result = AddPeer(enode.toUtf8().data());
            rnLog(RCTSTATUS) << "::addPeer AddPeer result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, enode, callbackId);
}


void RCTStatus::recoverAccount(QString passphrase, QString password, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::recoverAccount call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString passphrase, QString password, double callbackId) {
            const char* result = RecoverAccount(password.toUtf8().data(), passphrase.toUtf8().data());
            rnLog(RCTSTATUS) << "::recoverAccount RecoverAccount result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, passphrase, password, callbackId);
}


void RCTStatus::login(QString address, QString password, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::login call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString address, QString password, double callbackId) {
            const char* result = Login(address.toUtf8().data(), password.toUtf8().data());
            rnLog(RCTSTATUS) << "::login Login result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, address, password, callbackId);
}


void RCTStatus::sendTransaction(QString txArgsJSON, QString password, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::sendTransaction call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString txArgsJSON, QString password, double callbackId) {
            const char* result = SendTransaction(txArgsJSON.toUtf8().data(), password.toUtf8().data());
            rnLog(RCTSTATUS) << "sendTransaction SendTransaction result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, txArgsJSON, password, callbackId);
}


void RCTStatus::signMessage(QString rpcParams, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::signMessage call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString rpcParams, double callbackId) {
            const char* result = SignMessage(rpcParams.toUtf8().data());
            rnLog(RCTSTATUS) << "::signMessage SignMessage result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, rpcParams, callbackId);
}

void RCTStatus::signGroupMembership(QString content, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::signGroupMembership with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString content, double callbackId) {
            const char* result = SignGroupMembership(content.toUtf8().data());
            qDebug() << "RCTStatus::signGroupMembership SignGroupMembership result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, content, callbackId);
}

void RCTStatus::extractGroupMembershipSignatures(QString signatures, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::extractGroupMembershipSignatures with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString signatures, double callbackId) {
            const char* result = ExtractGroupMembershipSignatures(signatures.toUtf8().data());
            qDebug() << "RCTStatus::extractGroupMembershipSignatures ExtractGroupMembershipSignatures result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, signatures, callbackId);
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
    rnLog(RCTSTATUS) << "::callRPC call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString payload, double callbackId) {
            const char* result = CallRPC(payload.toUtf8().data());
            rnLog(RCTSTATUS) << "::callRPC CallRPC result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, payload, callbackId);
}

void RCTStatus::callPrivateRPC(QString payload, double callbackId) {
    Q_D(RCTStatus);
    rnLog(RCTSTATUS) << "::callPrivateRPC call with param callbackId: " << callbackId;
    QtConcurrent::run([&](QString payload, double callbackId) {
            const char* result = CallPrivateRPC(payload.toUtf8().data());
            rnLog(RCTSTATUS) << "::callPrivateRPC CallPrivateRPC result: " << statusGoResultError(result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, payload, callbackId);
}

void RCTStatus::closeApplication() {
}

bool RCTStatus::JSCEnabled() {
    rnLog(RCTSTATUS) << "::JSCEnabled call";
    return false;
}

void RCTStatus::statusGoEventCallback(const char* event) {
    rnLog(RCTSTATUS) << "::statusGoEventCallback call, ... event: " << event;
    RCTStatusPrivate::rctStatus->emitStatusGoEvent(event);
}

void RCTStatus::emitStatusGoEvent(QString event) {
    rnLog(RCTSTATUS) << "::emitStatusGoEvent call, ... event: " << event;
    Q_EMIT statusGoEvent(event);
}

void RCTStatus::onStatusGoEvent(QString event) {
    rnLog(RCTSTATUS) << "::onStatusGoEvent call, ... event: " << event.toUtf8().data();
    RCTStatusPrivate::bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", event.toUtf8().data()}});
}

QString RCTStatus::statusGoResultError(const char* result)
{
    QJsonParseError jsonError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(QString(result).toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        rnLog(RCTSTATUS) << jsonError.errorString();
        return QString("");
    }

    return QString("Error: %1").arg(jsonDoc.toVariant().toMap().value("error").toString());
}
