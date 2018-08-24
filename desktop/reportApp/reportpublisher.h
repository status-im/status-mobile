/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#ifndef REPORTPUBLISHER
#define REPORTPUBLISHER

#include <QObject>
#include <QString>

class ReportPublisher : public QObject {
    Q_OBJECT

public:
    ReportPublisher(QString minidumpFilePath, QString crashedExecutablePath, QObject* parent = 0);

    Q_INVOKABLE void submit();
    Q_INVOKABLE void restartAndQuit();
    Q_INVOKABLE void quit();
    Q_INVOKABLE void showDirectory();
    Q_INVOKABLE QString resolveDataStoragePath();

private:

    bool prepareReportFiles(QString reportDirPath);

    QString m_minidumpFilePath;
    QString m_crashedExecutablePath;
    bool m_logFilesPrepared = false;
};


#endif // REPORTPUBLISHER
