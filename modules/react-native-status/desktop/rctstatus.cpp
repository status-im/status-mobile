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

Q_LOGGING_CATEGORY(RCTSTATUS, "RCTStatus")

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
  qCDebug(RCTSTATUS) << "::getDeviceUUID call";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{"com.status.StatusIm"});
}


void RCTStatus::startNode(QString configString) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::startNode call - configString:" << configString;

    QJsonParseError jsonError;
    const QJsonDocument& jsonDoc = QJsonDocument::fromJson(configString.toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError){
        qCWarning(RCTSTATUS) << jsonError.errorString();
    }

    QVariantMap configJSON = jsonDoc.toVariant().toMap();
    qCDebug(RCTSTATUS) << "::startNode configString: " << configJSON;

    int networkId = configJSON["NetworkId"].toInt();
    QString relativeDataDirPath = configJSON["DataDir"].toString();
    if (!relativeDataDirPath.startsWith("/"))
        relativeDataDirPath.prepend("/");

    QString rootDirPath = getRootDirPath();
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
    qCInfo(RCTSTATUS) << "::startNode updated configString: " << updatedJsonDoc.toVariant().toMap();
    const char* result = StartNode(QString(updatedJsonDoc.toJson(QJsonDocument::Compact)).toUtf8().data());
    logStatusGoResult("::startNode StartNode", result);
}


void RCTStatus::stopNode() {
    qCInfo(RCTSTATUS) << "::stopNode call";
    const char* result = StopNode();
    logStatusGoResult("::stopNode StopNode", result);
}


void RCTStatus::createAccount(QString password, double callbackId) {
    Q_D(RCTStatus);
    qCInfo(RCTSTATUS) << "::createAccount call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString password, double callbackId) {
            const char* result = CreateAccount(password.toUtf8().data());
            logStatusGoResult("::createAccount CreateAccount", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, password, callbackId);
}


void RCTStatus::notifyUsers(QString token, QString payloadJSON, QString tokensJSON, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::notifyUsers call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString token, QString payloadJSON, QString tokensJSON, double callbackId) {
            const char* result = NotifyUsers(token.toUtf8().data(), payloadJSON.toUtf8().data(), tokensJSON.toUtf8().data());
            logStatusGoResult("::notifyUsers Notify", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, token, payloadJSON, tokensJSON, callbackId);
}


void RCTStatus::addPeer(QString enode, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::addPeer call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString enode, double callbackId) {
            const char* result = AddPeer(enode.toUtf8().data());
            logStatusGoResult("::addPeer AddPeer", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, enode, callbackId);
}


void RCTStatus::recoverAccount(QString passphrase, QString password, double callbackId) {
    Q_D(RCTStatus);
    qCInfo(RCTSTATUS) << "::recoverAccount call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString passphrase, QString password, double callbackId) {
            const char* result = RecoverAccount(password.toUtf8().data(), passphrase.toUtf8().data());
            logStatusGoResult("::recoverAccount RecoverAccount", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, passphrase, password, callbackId);
}


void RCTStatus::login(QString address, QString password, double callbackId) {
    Q_D(RCTStatus);
    qCInfo(RCTSTATUS) << "::login call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString address, QString password, double callbackId) {
            const char* result = Login(address.toUtf8().data(), password.toUtf8().data());
            logStatusGoResult("::login Login", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, address, password, callbackId);
}

void RCTStatus::verify(QString address, QString password, double callbackId) {
    Q_D(RCTStatus);
    qCInfo(RCTSTATUS) << "::verify call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString address, QString password, double callbackId) {
            QDir rootDir(getRootDirPath());
            QString keystorePath = rootDir.absoluteFilePath("keystore");
            const char* result = VerifyAccountPassword(keystorePath.toUtf8().data(), address.toUtf8().data(), password.toUtf8().data());
            logStatusGoResult("::verify VerifyAccountPassword", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, address, password, callbackId);
}



void RCTStatus::sendTransaction(QString txArgsJSON, QString password, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::sendTransaction call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString txArgsJSON, QString password, double callbackId) {
            const char* result = SendTransaction(txArgsJSON.toUtf8().data(), password.toUtf8().data());
            logStatusGoResult("::sendTransaction SendTransaction", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, txArgsJSON, password, callbackId);
}


void RCTStatus::signMessage(QString rpcParams, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::signMessage call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString rpcParams, double callbackId) {
            const char* result = SignMessage(rpcParams.toUtf8().data());
            logStatusGoResult("::signMessage SignMessage", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, rpcParams, callbackId);
}

void RCTStatus::signGroupMembership(QString content, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::signGroupMembership - callbackId:" << callbackId;
    QtConcurrent::run([&](QString content, double callbackId) {
            const char* result = SignGroupMembership(content.toUtf8().data());
            logStatusGoResult("::signGroupMembership SignGroupMembership", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, content, callbackId);
}

void RCTStatus::extractGroupMembershipSignatures(QString signatures, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::extractGroupMembershipSignatures - callbackId:" << callbackId;
    QtConcurrent::run([&](QString signatures, double callbackId) {
            const char* result = ExtractGroupMembershipSignatures(signatures.toUtf8().data());
            logStatusGoResult("::extractGroupMembershipSignatures ExtractGroupMembershipSignatures", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, signatures, callbackId);
}

void RCTStatus::enableInstallation(QString installationId, double callbackId) {
    Q_D(RCTStatus);
    QtConcurrent::run([&](QString installationId, double callbackId) {
            const char* result = EnableInstallation(installationId.toUtf8().data());
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, installationId, callbackId);
}

void RCTStatus::disableInstallation(QString installationId, double callbackId) {
    Q_D(RCTStatus);
    QtConcurrent::run([&](QString installationId, double callbackId) {
            const char* result = DisableInstallation(installationId.toUtf8().data());
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, installationId, callbackId);
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
    qCDebug(RCTSTATUS) << "::callRPC call - payload:" << payload.left(128) << "callbackId:" << callbackId;
    QtConcurrent::run([&](QString payload, double callbackId) {
            const char* result = CallRPC(payload.toUtf8().data());
            logStatusGoResult("::callRPC CallRPC", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, payload, callbackId);
}

void RCTStatus::callPrivateRPC(QString payload, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::callPrivateRPC call - payload:" << payload.left(128) << "callbackId:" << callbackId;
    QtConcurrent::run([&](QString payload, double callbackId) {
            const char* result = CallPrivateRPC(payload.toUtf8().data());
            logStatusGoResult("::callPrivateRPC CallPrivateRPC", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, payload, callbackId);
}

void RCTStatus::closeApplication() {
}

bool RCTStatus::JSCEnabled() {
    qCDebug(RCTSTATUS) << "::JSCEnabled call";
    return false;
}

void RCTStatus::statusGoEventCallback(const char* event) {
    qCDebug(RCTSTATUS) << "::statusGoEventCallback call - event: " << event;
    RCTStatusPrivate::rctStatus->emitStatusGoEvent(event);
}

void RCTStatus::emitStatusGoEvent(QString event) {
    qCDebug(RCTSTATUS) << "::emitStatusGoEvent call - event: " << event;
    Q_EMIT statusGoEvent(event);
}

void RCTStatus::onStatusGoEvent(QString event) {
    qCDebug(RCTSTATUS) << "::onStatusGoEvent call - event: " << event.toUtf8().data();
    RCTStatusPrivate::bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", event.toUtf8().data()}});
}

void RCTStatus::logStatusGoResult(const char* methodName, const char* result)
{
    QJsonParseError jsonError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(QString(result).toUtf8(), &jsonError);
    if (jsonError.error != QJsonParseError::NoError) {
        qCWarning(RCTSTATUS) << qUtf8Printable(jsonError.errorString());
        return;
    }

    QString error = jsonDoc.toVariant().toMap().value("error").toString();
    if (error.isEmpty()) {
        qCDebug(RCTSTATUS) << methodName << "succeeded";
    } else {
        qCWarning(RCTSTATUS) << methodName << "- error:" << qUtf8Printable(error);
    }
}

void RCTStatus::updateMailservers(QString enodes, double callbackId) {
    Q_D(RCTStatus);
    qCDebug(RCTSTATUS) << "::updateMailservers call - callbackId:" << callbackId;
    QtConcurrent::run([&](QString enodes, double callbackId) {
            const char* result = UpdateMailservers(enodes.toUtf8().data());
            logStatusGoResult("::updateMailservers UpdateMailservers", result);
            d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
        }, enodes, callbackId);
}

QString RCTStatus::getRootDirPath() {
    QString statusDataDir = qgetenv("STATUS_DATA_DIR");
    QString rootDirPath;
    if (!statusDataDir.isEmpty()) {
        rootDirPath = statusDataDir;
    }
    else {
        rootDirPath = QStandardPaths::writableLocation(QStandardPaths::AppLocalDataLocation);
    }

    return rootDirPath;
}

