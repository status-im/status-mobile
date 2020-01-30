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

#include <QByteArray>
#include <QDebug>
#include <QDir>
#include <QJsonDocument>
#include <QMessageBox>
#include <QStandardPaths>
#include <QStorageInfo>
#include <QVariantMap>
#include <QtConcurrent>

#include "libstatus.h"

extern QString getDataStoragePath();
extern QString getLogFilePath();

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<RCTStatus *>(); }
} registerMetaType;
} // namespace

class RCTStatusPrivate {
public:
  static Bridge *bridge;
  static RCTStatus *rctStatus;
};

Bridge *RCTStatusPrivate::bridge = nullptr;
RCTStatus *RCTStatusPrivate::rctStatus = nullptr;

Q_LOGGING_CATEGORY(RCTSTATUS, "RCTStatus")

RCTStatus::RCTStatus(QObject *parent)
    : QObject(parent), d_ptr(new RCTStatusPrivate) {
  RCTStatusPrivate::rctStatus = this;
  SetSignalEventCallback((void *)&RCTStatus::statusGoEventCallback);
  connect(this, &RCTStatus::statusGoEvent, this, &RCTStatus::onStatusGoEvent);
}

RCTStatus::~RCTStatus() {}

void RCTStatus::setBridge(Bridge *bridge) {
  Q_D(RCTStatus);
  d->bridge = bridge;
}

QString RCTStatus::moduleName() { return "Status"; }

QList<ModuleMethod *> RCTStatus::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap RCTStatus::constantsToExport() { return QVariantMap(); }

void RCTStatus::shouldMoveToInternalStorage(double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::shouldMoveToInternalStorage call";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{QVariant()});
}

void RCTStatus::moveToInternalStorage(double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::moveToInternalStorage call";

  d->bridge->invokePromiseCallback(callbackId, QVariantList{QVariant()});
}

QString RCTStatus::prepareDirAndUpdateConfig(QString configString) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::prepareDirAndUpdateConfig call - configString:"
                     << configString;

  QJsonParseError jsonError;
  const QJsonDocument &jsonDoc =
      QJsonDocument::fromJson(configString.toUtf8(), &jsonError);
  if (jsonError.error != QJsonParseError::NoError) {
    qCWarning(RCTSTATUS) << jsonError.errorString();
  }

  QVariantMap configJSON = jsonDoc.toVariant().toMap();
  QVariantMap shhextConfig = configJSON["ShhextConfig"].toMap();
  qCDebug(RCTSTATUS) << "::startNode configString: " << configJSON;

  int networkId = configJSON["NetworkId"].toInt();
  QString relativeDataDirPath = configJSON["DataDir"].toString();
  if (!relativeDataDirPath.startsWith("/"))
    relativeDataDirPath.prepend("/");

  QString rootDirPath = getDataStoragePath();
  QDir rootDir(rootDirPath);
  QString absDataDirPath = rootDirPath + relativeDataDirPath;
  QDir dataDir(absDataDirPath);
  if (!dataDir.exists()) {
    dataDir.mkpath(".");
  }

  d_gethLogFilePath = dataDir.absoluteFilePath("geth.log");
  configJSON["DataDir"] = absDataDirPath;
  configJSON["KeyStoreDir"] = rootDir.absoluteFilePath("keystore");
  configJSON["LogFile"] = d_gethLogFilePath;

  shhextConfig["BackupDisabledDataDir"] = rootDirPath;

  configJSON["ShhExtConfig"] = shhextConfig;

  const QJsonDocument &updatedJsonDoc = QJsonDocument::fromVariant(configJSON);
  qCInfo(RCTSTATUS) << "::startNode updated configString: "
                    << updatedJsonDoc.toVariant().toMap();
  return QString(updatedJsonDoc.toJson(QJsonDocument::Compact));
}

void RCTStatus::prepareDirAndUpdateConfig(QString configString,
                                          double callbackId) {
  Q_D(RCTStatus);
  qCInfo(RCTSTATUS) << "::prepareDirAndUpdateConfig call - callbackId:"
                    << callbackId;
  QtConcurrent::run(
      [&](QString configString, double callbackId) {
        QString updatedConfig = prepareDirAndUpdateConfig(configString);
        d->bridge->invokePromiseCallback(
            callbackId, QVariantList{updatedConfig.toUtf8().data()});
      },
      configString, callbackId);
}

void RCTStatus::initKeystore() {
  qCInfo(RCTSTATUS) << "::initKeystore call";
  QString rootDir = getDataStoragePath();
  const char *result = InitKeystore(rootDir.toUtf8().data());
  logStatusGoResult("::initKeystore InitKeystore", result);
}

#include <QApplication>
#include <QMessageBox>
#include <QProcess>
#include <QStandardPaths>
#include <QtGui/private/qzipwriter_p.h>

void showFileInGraphicalShell(QWidget *parent, const QFileInfo &fileInfo) {
// Mac, Windows support folder or file.
#ifdef Q_OS_WIN
  const QString explorer =
      QStandardPaths::findExecutable(QLatin1String("explorer.exe"));
  if (explorer.isEmpty()) {
    QMessageBox::warning(
        parent, QApplication::translate("Core::Internal",
                                        "Launching Windows Explorer Failed"),
        QApplication::translate(
            "Core::Internal",
            "Could not find explorer.exe in path to launch Windows Explorer."));
    return;
  }
  QStringList param;
  if (!fileInfo.isDir())
    param += QLatin1String("/select,");
  param += QDir::toNativeSeparators(fileInfo.canonicalFilePath());
  QProcess::startDetached(explorer, param);
#elif defined(Q_OS_MAC)
  QStringList scriptArgs;
  scriptArgs << QLatin1String("-e")
             << QString::fromLatin1(
                    "tell application \"Finder\" to reveal POSIX file \"%1\"")
                    .arg(fileInfo.canonicalFilePath());
  QProcess::execute(QLatin1String("/usr/bin/osascript"), scriptArgs);
  scriptArgs.clear();
  scriptArgs << QLatin1String("-e")
             << QLatin1String("tell application \"Finder\" to activate");
  QProcess::execute(QLatin1String("/usr/bin/osascript"), scriptArgs);
#else
  // we cannot select a file here, because no file browser really supports it...
  const QString folder = fileInfo.isDir() ? fileInfo.absoluteFilePath()
                                          : fileInfo.dir().absolutePath();
  QProcess browserProc;
  browserProc.setProgram("xdg-open");
  browserProc.setArguments(QStringList(folder));
  bool success = browserProc.startDetached();
  const QString error =
      QString::fromLocal8Bit(browserProc.readAllStandardError());
  success = success && error.isEmpty();
  if (!success) {
    QMessageBox::warning(parent, "Launching Explorer Failed", error);
    return;
  }
#endif
}

void RCTStatus::sendLogs(QString dbJSON, QString jsLogs, double callbackId) {
  Q_D(RCTStatus);

  qCDebug(RCTSTATUS) << "::sendLogs call - logFilePath:" << getLogFilePath()
                     << "d_gethLogFilePath:" << d_gethLogFilePath
                     << "dbJSON:" << dbJSON;

  QString tmpDirName("Status.im");
  QDir tmpDir(QStandardPaths::writableLocation(QStandardPaths::TempLocation));
  if (!tmpDir.mkpath(tmpDirName)) {
    qCWarning(RCTSTATUS) << "::sendLogs could not create temp dir:"
                         << tmpDirName;
    return;
  }

  // Check that at least 20MB are available for log generation
  QStorageInfo storage(tmpDir);
  if (storage.bytesAvailable() < 20 * 1024 * 1024) {
    QMessageBox dlg;
    dlg.warning(QApplication::activeWindow(), "Error",
                QString("Insufficient storage space available in %1 for log "
                        "generation. Please free up some space.")
                    .arg(storage.rootPath()),
                QMessageBox::Close);
    return;
  }

  QFile zipFile(tmpDir.absoluteFilePath(tmpDirName + QDir::separator() +
                                        "Status-debug-logs.zip"));
  QZipWriter zipWriter(&zipFile);
  QFile gethLogFile(d_gethLogFilePath);
  QFile logFile(getLogFilePath());
  zipWriter.addFile("db.json", dbJSON.toUtf8());
  zipWriter.addFile("js_logs.log", jsLogs.toUtf8());
  if (gethLogFile.exists()) {
    zipWriter.addFile(QFileInfo(gethLogFile).fileName(), &gethLogFile);
  }
  if (logFile.exists()) {
    zipWriter.addFile(QFileInfo(logFile).fileName(), &logFile);
  }
  zipWriter.close();

  showFileInGraphicalShell(QApplication::activeWindow(), QFileInfo(zipFile));
}

void RCTStatus::exportLogs(double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](double callbackId) {
        const char *result = ExportNodeLogs();
        logStatusGoResult("::exportLogs", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      callbackId);
}

void RCTStatus::addPeer(QString enode, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::addPeer call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString enode, double callbackId) {
        const char *result = AddPeer(enode.toUtf8().data());
        logStatusGoResult("::addPeer AddPeer", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      enode, callbackId);
}

void RCTStatus::saveAccountAndLogin(QString accountData, QString password,
                                    QString config, QString subAccountsData) {

  Q_D(RCTStatus);
  QString finalConfig = prepareDirAndUpdateConfig(config);
  QtConcurrent::run(
      [&](QString accountData, QString password, QString finalConfig,
          QString subAccountsData) {
        const char *result = SaveAccountAndLogin(
            accountData.toUtf8().data(), password.toUtf8().data(),
            finalConfig.toUtf8().data(), subAccountsData.toUtf8().data());
        logStatusGoResult("::saveAccountAndLogin", result);
      },
      accountData, password, finalConfig, subAccountsData);
}

// void RCTStatus::saveAccountAndLoginWithKeycard(QString accountData,
//                                               QString password, QString
//                                               config,
//                                               QString chatKey) {
//  Q_D(RCTStatus);
//  QString finalConfig = prepareDirAndUpdateConfig(config);
//  QtConcurrent::run(
//      [&](QString accountData, QString password, QString finalConfig,
//          QString chatKey) {
//        const char *result = SaveAccountAndLoginWithKeycard(
//            accountData.toUtf8().data(), password.toUtf8().data(),
//            finalConfig.toUtf8().data(), chatKey.toUtf8().data());
//        logStatusGoResult("::saveAccountAndLoginWithKeycard", result);
//      },
//      accountData, password, finalConfig, chatKey);
//}

void RCTStatus::login(QString accountData, QString password) {

  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString accountData, QString password) {
        const char *result =
            Login(accountData.toUtf8().data(), password.toUtf8().data());
        logStatusGoResult("::login", result);
      },
      accountData, password);
}

// void RCTStatus::loginWithKeycard(QString accountData, QString password,
//                                 QString chatKey) {
//
//  Q_D(RCTStatus);
//  QtConcurrent::run(
//      [&](QString accountData, QString password, QString chatKey) {
//        const char *result =
//            LoginWithKeycard(accountData.toUtf8().data(),
//                             password.toUtf8().data(),
//                             chatKey.toUtf8().data());
//        logStatusGoResult("::loginWithKeycard", result);
//      },
//      accountData, password, chatKey);
//}

void RCTStatus::logout() {
  Q_D(RCTStatus);
  QtConcurrent::run([&]() {
    const char *result = Logout();
    logStatusGoResult("::logout", result);
  });
}

void RCTStatus::openAccounts(double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](double callbackId) {
        QString rootDir = getDataStoragePath();
        const char *result = OpenAccounts(rootDir.toUtf8().data());
        logStatusGoResult("::openAccounts", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      callbackId);
}

void RCTStatus::multiAccountStoreAccount(QString json, double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result = MultiAccountStoreAccount(json.toUtf8().data());
        logStatusGoResult("::multiAccountStoreAccount", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::multiAccountLoadAccount(QString json, double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result = MultiAccountLoadAccount(json.toUtf8().data());
        logStatusGoResult("::multiAccountLoadAccount", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::multiAccountReset(double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](double callbackId) {
        const char *result = MultiAccountReset();
        logStatusGoResult("::multiAccountReset", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      callbackId);
}

void RCTStatus::multiAccountDeriveAddresses(QString json, double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result = MultiAccountDeriveAddresses(json.toUtf8().data());
        logStatusGoResult("::multiAccountDeriveAddresses", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::multiAccountStoreDerived(QString json, double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result =
            MultiAccountStoreDerivedAccounts(json.toUtf8().data());
        logStatusGoResult("::multiAccountStoreDerived", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::multiAccountGenerateAndDeriveAddresses(QString json,
                                                       double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result =
            MultiAccountGenerateAndDeriveAddresses(json.toUtf8().data());
        logStatusGoResult("::multiAccountGenerateAndDeriveAddresses", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::multiAccountImportMnemonic(QString json, double callbackId) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString json, double callbackId) {
        const char *result = MultiAccountImportMnemonic(json.toUtf8().data());
        logStatusGoResult("::multiAccountImportMnemonic", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      json, callbackId);
}

void RCTStatus::verify(QString address, QString password, double callbackId) {
  Q_D(RCTStatus);
  qCInfo(RCTSTATUS) << "::verify call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString address, QString password, double callbackId) {
        QDir rootDir(getDataStoragePath());
        QString keystorePath = rootDir.absoluteFilePath("keystore");
        const char *result = VerifyAccountPassword(keystorePath.toUtf8().data(),
                                                   address.toUtf8().data(),
                                                   password.toUtf8().data());
        logStatusGoResult("::verify VerifyAccountPassword", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      address, password, callbackId);
}

void RCTStatus::sendTransaction(QString txArgsJSON, QString password,
                                double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::sendTransaction call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString txArgsJSON, QString password, double callbackId) {
        const char *result = SendTransaction(txArgsJSON.toUtf8().data(),
                                             password.toUtf8().data());
        logStatusGoResult("::sendTransaction SendTransaction", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      txArgsJSON, password, callbackId);
}

void RCTStatus::signMessage(QString rpcParams, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::signMessage call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString rpcParams, double callbackId) {
        const char *result = SignMessage(rpcParams.toUtf8().data());
        logStatusGoResult("::signMessage SignMessage", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      rpcParams, callbackId);
}

void RCTStatus::signTypedData(QString data, QString account, QString password,
                              double callbackId) {

  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::signMessage call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString data, QString account, QString password, double callbackId) {
        const char *result =
            SignTypedData(data.toUtf8().data(), account.toUtf8().data(),
                          password.toUtf8().data());
        logStatusGoResult("::signTypedData signTypedData", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      data, account, password, callbackId);
}
void RCTStatus::signGroupMembership(QString content, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::signGroupMembership - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString content, double callbackId) {
        const char *result = SignGroupMembership(content.toUtf8().data());
        logStatusGoResult("::signGroupMembership SignGroupMembership", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      content, callbackId);
}

void RCTStatus::extractGroupMembershipSignatures(QString signatures,
                                                 double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::extractGroupMembershipSignatures - callbackId:"
                     << callbackId;
  QtConcurrent::run(
      [&](QString signatures, double callbackId) {
        const char *result =
            ExtractGroupMembershipSignatures(signatures.toUtf8().data());
        logStatusGoResult("::extractGroupMembershipSignatures "
                          "ExtractGroupMembershipSignatures",
                          result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      signatures, callbackId);
}

void RCTStatus::setAdjustResize() {}

void RCTStatus::setAdjustPan() {}

void RCTStatus::setSoftInputMode(int i) {}

void RCTStatus::clearCookies() {}

void RCTStatus::clearStorageAPIs() {}

void RCTStatus::callRPC(QString payload, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::callRPC call - payload:" << payload.left(128)
                     << "callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString payload, double callbackId) {
        const char *result = CallRPC(payload.toUtf8().data());
        logStatusGoResult("::callRPC CallRPC", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      payload, callbackId);
}

void RCTStatus::callPrivateRPC(QString payload, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::callPrivateRPC call - payload:" << payload.left(128)
                     << "callbackId:" << callbackId;
  QtConcurrent::run(
      [&](QString payload, double callbackId) {
        const char *result = CallPrivateRPC(payload.toUtf8().data());
        logStatusGoResult("::callPrivateRPC CallPrivateRPC", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      payload, callbackId);
}

void RCTStatus::closeApplication() {}

void RCTStatus::connectionChange(QString type, bool isExpensive) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString type, bool isExpensive) {
        ConnectionChange(type.toUtf8().data(), isExpensive ? 1 : 0);
        qCWarning(RCTSTATUS) << "::connectionChange";
      },
      type, isExpensive);
}

void RCTStatus::appStateChange(QString type) {
  Q_D(RCTStatus);
  QtConcurrent::run(
      [&](QString type) {
        AppStateChange(type.toUtf8().data());
        qCWarning(RCTSTATUS) << "::appStateChange";
      },
      type);
}

bool RCTStatus::JSCEnabled() {
  qCDebug(RCTSTATUS) << "::JSCEnabled call";
  return false;
}

void RCTStatus::statusGoEventCallback(const char *event) {
  qCDebug(RCTSTATUS) << "::statusGoEventCallback call - event: " << event;
  RCTStatusPrivate::rctStatus->emitStatusGoEvent(event);
}

void RCTStatus::emitStatusGoEvent(QString event) {
  qCDebug(RCTSTATUS) << "::emitStatusGoEvent call - event: " << event;
  Q_EMIT statusGoEvent(event);
}

void RCTStatus::onStatusGoEvent(QString event) {
  qCDebug(RCTSTATUS) << "::onStatusGoEvent call - event: "
                     << event.toUtf8().data();
  RCTStatusPrivate::bridge->eventDispatcher()->sendDeviceEvent(
      "gethEvent", QVariantMap{{"jsonEvent", event.toUtf8().data()}});
}

void RCTStatus::logStatusGoResult(const char *methodName, const char *result) {
  QJsonParseError jsonError;
  QJsonDocument jsonDoc =
      QJsonDocument::fromJson(QString(result).toUtf8(), &jsonError);
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

void RCTStatus::getNodesFromContract(QString url, QString address,
                                     double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::getNodesFromContract call - callbackId:"
                     << callbackId;
  QtConcurrent::run(
      [&](QString url, QString address, double callbackId) {
        const char *result =
            GetNodesFromContract(url.toUtf8().data(), address.toUtf8().data());
        logStatusGoResult("::getNodesFromContract GetNodesFromContract",
                          result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      url, address, callbackId);
}

void RCTStatus::chaosModeUpdate(bool on, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::chaosModeUpdate call - callbackId:" << callbackId;
  QtConcurrent::run(
      [&](bool on, double callbackId) {
        const char *result = ChaosModeUpdate(on);
        logStatusGoResult("::chaosModeUpdate ChaosModeUpdate", result);
        d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
      },
      on, callbackId);
}

QString RCTStatus::generateAlias(QString publicKey) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::generateAlias call";
  return "";
}

void RCTStatus::generateAliasAsync(QString publicKey, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::generateAliasAsync call";
  QByteArray b = publicKey.toUtf8();
  const char *result = GenerateAlias({b.data(), b.length()});
    qCDebug(RCTSTATUS) << "::generateAliasAsync call result"<<result;
  d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

QString RCTStatus::identicon(QString publicKey) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::identicon call";
  return "";
}

void RCTStatus::identiconAsync(QString publicKey, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::identiconAsync call";
  QByteArray b = publicKey.toUtf8();
  const char *result = Identicon({b.data(), b.length()});
  qCDebug(RCTSTATUS) << "::identiconAsync call result"<<result;
  d->bridge->invokePromiseCallback(callbackId, QVariantList{result});
}

void RCTStatus::generateAliasAndIdenticonAsync(QString publicKey, double callbackId) {
  Q_D(RCTStatus);
  qCDebug(RCTSTATUS) << "::generateAliasAndIdenticonAsync call";
  QByteArray pubKey = publicKey.toUtf8();
  const char *alias = GenerateAlias({pubKey.data(), pubKey.length()});
  const char *identicon = Identicon({pubKey.data(), pubKey.length()});
  d->bridge->invokePromiseCallback(callbackId, QVariantList{alias, identicon});
}
