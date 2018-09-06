/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include "reportpublisher.h"

#include <QApplication>
#include <QDebug>
#include <QDesktopServices>
#include <QDir>
#include <QDirIterator>
#include <QFile>
#include <QProcess>
#include <QUrl>

const QString REPORT_SUBMIT_URL =
    QStringLiteral("https://goo.gl/forms/0705ZN0EMW3xLDpI2");

ReportPublisher::ReportPublisher(QString minidumpFilePath,
                                 QString crashedExecutablePath,
                                 QString logsPath, QObject *parent)
    : QObject(parent), m_minidumpFilePath(minidumpFilePath),
      m_logsPath(logsPath), m_crashedExecutablePath(crashedExecutablePath) {}

void ReportPublisher::submit() {
  QDesktopServices::openUrl(QUrl(REPORT_SUBMIT_URL));
  showDirectory();
}

void ReportPublisher::restartAndQuit() {
  QString appPath = m_crashedExecutablePath;

#ifdef Q_OS_MACOS
  QFileInfo crashedExecutableFileInfo(m_crashedExecutablePath);
  QString fullPath = crashedExecutableFileInfo.dir().absolutePath();
  const QString bundleExtension = QStringLiteral(".app");
  if (fullPath.contains(bundleExtension)) {
    appPath = fullPath.left(fullPath.indexOf(bundleExtension) +
                            bundleExtension.size());
  }
  QString cmd = QString("open %1").arg(appPath);
#elif defined(Q_OS_LINUX)
  QString cmd = appPath;
#endif

  QProcess::startDetached(cmd);

  qApp->quit();
}

void ReportPublisher::quit() { qApp->quit(); }

void ReportPublisher::showDirectory() {
  QString dataStoragePath = resolveDataStoragePath();
  if (!m_logFilesPrepared) {
    m_logFilesPrepared = prepareReportFiles(dataStoragePath);
  }
  QDesktopServices::openUrl(
      QUrl("file://" + dataStoragePath, QUrl::TolerantMode));
}

bool ReportPublisher::prepareReportFiles(QString reportDirPath) {
  QFileInfo minidumpFileInfo(m_minidumpFilePath);
  QFileInfo crashedExecutableFileInfo(m_crashedExecutablePath);
  if (!minidumpFileInfo.exists() || !crashedExecutableFileInfo.exists())
    return false;

  return QFile::copy(m_minidumpFilePath,
                     reportDirPath + QDir::separator() + "crash.dmp") &&
         QFile::copy(m_crashedExecutablePath,
                     reportDirPath + QDir::separator() +
                         crashedExecutableFileInfo.fileName()) &&
         prepareLogFiles(reportDirPath);
}

bool ReportPublisher::prepareLogFiles(QString reportDirPath) {
  if (reportDirPath.isEmpty())
    return true;

  QDirIterator filesIterator(m_logsPath, QStringList() << "*.log", QDir::Files);
  while (filesIterator.hasNext()) {
    QFileInfo logFile(filesIterator.next());
    QFile::copy(logFile.absoluteFilePath(),
                reportDirPath + QDir::separator() + logFile.fileName());
  }
  return true;
}

QString ReportPublisher::resolveDataStoragePath() {
  QFileInfo minidumpFileInfo(m_minidumpFilePath);
  QString dataStoragePath =
      QStandardPaths::writableLocation(QStandardPaths::AppDataLocation) +
      QDir::separator() + minidumpFileInfo.baseName();
  QDir dir(dataStoragePath);
  if (!dir.exists()) {
    dir.mkpath(".");
  }
  return dataStoragePath;
}
