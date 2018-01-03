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

namespace {
struct RegisterQMLMetaType {
    RegisterQMLMetaType() {
        qRegisterMetaType<RCTStatus*>();
    }
} registerMetaType2;
} // namespace

class RCTStatusPrivate {
public:
    Bridge* bridge = nullptr;
};

RCTStatus::RCTStatus(QObject* parent) : QObject(parent), d_ptr(new RCTStatusPrivate) {}

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

    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::parseJail(QString chatId, QString js, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::parseJail with param chatId: " << chatId << " js:" << " and callback id: " << callbackId;
    if (chatId == "console") {
        QString response = "{\"result\":\"{\\\"commands\\\":{\\\"phone,50\\\":{\\\"name\\\":\\\"phone\\\",\\\"title\\\":\\\"Send Phone Number\\\",\\\"description\\\":\\\"Find friends using your number\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#5bb2a2\\\",\\\"icon\\\":\\\"phone_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"phone\\\",\\\"type\\\":\\\"phone\\\",\\\"placeholder\\\":\\\"Phone number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"faucet,50\\\":{\\\"name\\\":\\\"faucet\\\",\\\"title\\\":\\\"Faucet\\\",\\\"description\\\":\\\"Get some ETH\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"url\\\",\\\"type\\\":\\\"text\\\",\\\"placeholder\\\":\\\"Faucet URL\\\"}],\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"debug,50\\\":{\\\"name\\\":\\\"debug\\\",\\\"title\\\":\\\"Debug mode\\\",\\\"description\\\":\\\"Starts\\/stops a debug mode\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"mode\\\",\\\"type\\\":\\\"text\\\"}],\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50}},\\\"responses\\\":{\\\"phone,50\\\":{\\\"name\\\":\\\"phone\\\",\\\"title\\\":\\\"Send Phone Number\\\",\\\"description\\\":\\\"Find friends using your number\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#5bb2a2\\\",\\\"icon\\\":\\\"phone_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"phone\\\",\\\"type\\\":\\\"phone\\\",\\\"placeholder\\\":\\\"Phone number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"confirmation-code,50\\\":{\\\"name\\\":\\\"confirmation-code\\\",\\\"description\\\":\\\"Confirmation code\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"params\\\":[{\\\"name\\\":\\\"code\\\",\\\"type\\\":\\\"number\\\"}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":50},\\\"password,42\\\":{\\\"name\\\":\\\"password\\\",\\\"description\\\":\\\"Password\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"icon\\\":\\\"lock_white\\\",\\\"params\\\":[{\\\"name\\\":\\\"password\\\",\\\"type\\\":\\\"password\\\",\\\"placeholder\\\":\\\"Type your password\\\",\\\"hidden\\\":true},{\\\"name\\\":\\\"password-confirmation\\\",\\\"type\\\":\\\"password\\\",\\\"placeholder\\\":\\\"Confirm\\\",\\\"hidden\\\":true}],\\\"sequential-params\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"anonymous\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":42},\\\"grant-permissions,58\\\":{\\\"name\\\":\\\"grant-permissions\\\",\\\"description\\\":\\\"Grant permissions\\\",\\\"has-handler\\\":true,\\\"async-handler\\\":false,\\\"color\\\":\\\"#7099e6\\\",\\\"icon\\\":\\\"lock_white\\\",\\\"params\\\":[],\\\"execute-immediately?\\\":true,\\\"scope\\\":[\\\"personal-chats\\\",\\\"anonymous\\\",\\\"registered\\\",\\\"dapps\\\"],\\\"scope-bitmask\\\":58}},\\\"functions\\\":{},\\\"subscriptions\\\":{}}\"}";

        QJsonParseError jsonError;
        QJsonDocument jsonDoc = QJsonDocument::fromJson(response.toLocal8Bit(), &jsonError);
        if (jsonError.error != QJsonParseError::NoError){
            qDebug() << jsonError.errorString();
        }

        qDebug() << "JSON response" << jsonDoc.toVariant();

        d->bridge->invokePromiseCallback(callbackId, jsonDoc.toVariant().toMap());
    } else {
        d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
    }
}


void RCTStatus::callJail(QString chatId, QString path, QString params, double callbackId) {
    Q_D(RCTStatus);
    
    qDebug() << "call of RCTStatus::callJail with param chatId: " << chatId << " path: " << path << " params: " << params <<  " and callback id: " << callbackId;
    if (path == "password") {
         d->bridge->invokePromiseCallback(callbackId, QVariantList{ QVariantMap{ {"result", ""} } });
    } else {
        d->bridge->invokePromiseCallback(callbackId, QVariantMap{ {"result", ""} });
    }
}


void RCTStatus::startNode(QString configString) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::startNode with param configString:" << configString;

    d->bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", "{\"type\":\"node.started\"}"}});
    d->bridge->eventDispatcher()->sendDeviceEvent("gethEvent", QVariantMap{{"jsonEvent", "{\"type\":\"node.ready\"}"}});
}


void RCTStatus::shouldMoveToInternalStorage(double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::shouldMoveToInternalStorage with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::moveToInternalStorage(double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::moveToInternalStorage with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::stopNode() {
}


void RCTStatus::createAccount(QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::createAccount with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::notify(QString token, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::notify with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::addPeer(QString enode, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::addPeer with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::recoverAccount(QString passphrase, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::recoverAccount with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::login(QString address, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::login with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::completeTransactions(QString hashes, QString password, double callbackId) {
    Q_D(RCTStatus);
    qDebug() << "call of RCTStatus::completeTransactions with param callbackId: " << callbackId;
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::discardTransaction(QString id) {
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
    d->bridge->invokePromiseCallback(callbackId, QVariantMap{});
}


void RCTStatus::closeApplication() {
}


