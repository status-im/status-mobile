
/**
 * Copyright (C) 2016, Canonical Ltd.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

// #define BUILD_FOR_BUNDLE

#include <QCommandLineParser>
#include <QDirIterator>
#include <QFile>
#include <QFontDatabase>
#include <QGuiApplication>
#include <QProcess>
#include <QQuickView>
#include <QStandardPaths>
#include <QTimer>
#include <QUrl>

#include "attachedproperties.h"
#include "reactitem.h"
#include "rootview.h"
#include "utilities.h"

#include "exceptionglobalhandler.h"

#ifdef BUILD_FOR_BUNDLE
#include <QMutexLocker>

QStringList consoleOutputStrings;
bool ubuntuServerStarted = false;
QMutex consoleOutputMutex;
QProcess *g_ubuntuServerProcess = nullptr;
#endif

const int MAIN_WINDOW_WIDTH = 1024;
const int MAIN_WINDOW_HEIGHT = 768;
const QString CRASH_REPORT_EXECUTABLE = QStringLiteral("reportApp");
const QString CRASH_REPORT_EXECUTABLE_RELATIVE_PATH =
    QStringLiteral("/../reportApp");

// TODO: some way to change while running
class ReactNativeProperties : public QObject {
  Q_OBJECT
  Q_PROPERTY(bool liveReload READ liveReload WRITE setLiveReload NOTIFY
                 liveReloadChanged)
  Q_PROPERTY(QUrl codeLocation READ codeLocation WRITE setCodeLocation NOTIFY
                 codeLocationChanged)
  Q_PROPERTY(QString pluginsPath READ pluginsPath WRITE setPluginsPath NOTIFY
                 pluginsPathChanged)
  Q_PROPERTY(
      QString executor READ executor WRITE setExecutor NOTIFY executorChanged)
public:
  ReactNativeProperties(QObject *parent = 0) : QObject(parent) {
    m_codeLocation = m_packagerTemplate.arg(m_packagerHost).arg(m_packagerPort);
  }
  bool liveReload() const { return m_liveReload; }
  void setLiveReload(bool liveReload) {
    if (m_liveReload == liveReload)
      return;
    m_liveReload = liveReload;
    Q_EMIT liveReloadChanged();
  }
  QUrl codeLocation() const { return m_codeLocation; }
  void setCodeLocation(const QUrl &codeLocation) {
    if (m_codeLocation == codeLocation)
      return;
    m_codeLocation = codeLocation;
    Q_EMIT codeLocationChanged();
  }
  QString pluginsPath() const { return m_pluginsPath; }
  void setPluginsPath(const QString &pluginsPath) {
    if (m_pluginsPath == pluginsPath)
      return;
    m_pluginsPath = pluginsPath;
    Q_EMIT pluginsPathChanged();
  }
  QString executor() const { return m_executor; }
  void setExecutor(const QString &executor) {
    if (m_executor == executor)
      return;
    m_executor = executor;
    Q_EMIT executorChanged();
  }
  QString packagerHost() const { return m_packagerHost; }
  void setPackagerHost(const QString &packagerHost) {
    if (m_packagerHost == packagerHost)
      return;
    m_packagerHost = packagerHost;
    setCodeLocation(m_packagerTemplate.arg(m_packagerHost).arg(m_packagerPort));
  }
  QString packagerPort() const { return m_packagerPort; }
  void setPackagerPort(const QString &packagerPort) {
    if (m_packagerPort == packagerPort)
      return;
    m_packagerPort = packagerPort;
    setCodeLocation(m_packagerTemplate.arg(m_packagerHost).arg(m_packagerPort));
  }
  void setLocalSource(const QString &source) {
    if (m_localSource == source)
      return;

    // overrides packager*
    if (source.startsWith("file:")) {
      setCodeLocation(source);
    } else {
      QFileInfo fi(source);
      if (!fi.exists()) {
        qWarning() << "Attempt to set non-existent local source file";
        return;
      }
      setCodeLocation(QUrl::fromLocalFile(fi.absoluteFilePath()));
      setLiveReload(false);
    }
  }
Q_SIGNALS:
  void liveReloadChanged();
  void codeLocationChanged();
  void pluginsPathChanged();
  void executorChanged();

private:
  bool m_liveReload = false;
  QString m_packagerHost = "localhost";
  QString m_packagerPort = "8081";
  QString m_localSource;
  QString m_packagerTemplate =
      "http://%1:%2/index.desktop.bundle?platform=desktop&dev=true";
  QUrl m_codeLocation;
  QString m_pluginsPath;
#ifdef BUILD_FOR_BUNDLE
  QString m_executor = "RemoteServerConnection";
#else
  QString m_executor = "LocalServerConnection";
#endif
};

#ifdef BUILD_FOR_BUNDLE
void runUbuntuServer();
void saveMessage(QtMsgType type, const QMessageLogContext &context,
                 const QString &msg);

void writeLogsToFile();
#endif

void loadFontsFromResources() {

  QDirIterator it(":", QDirIterator::Subdirectories);
  while (it.hasNext()) {
    QString resourceFile = it.next();
    if (resourceFile.endsWith(".otf", Qt::CaseInsensitive) ||
        resourceFile.endsWith(".ttf", Qt::CaseInsensitive)) {
      QFontDatabase::addApplicationFont(resourceFile);
    }
  }
}

void exceptionPostHandledCallback() {
#ifdef BUILD_FOR_BUNDLE
  if (g_ubuntuServerProcess) {
    g_ubuntuServerProcess->kill();
  }
#endif
}

int main(int argc, char **argv) {

  QGuiApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
  QGuiApplication app(argc, argv);

  app.setApplicationName("StatusIm");

  QString appPath = QCoreApplication::applicationDirPath();
#ifndef BUILD_FOR_BUNDLE
  appPath.append(CRASH_REPORT_EXECUTABLE_RELATIVE_PATH);
#endif

  ExceptionGlobalHandler exceptionHandler(appPath + QDir::separator() +
                                              CRASH_REPORT_EXECUTABLE,
                                          exceptionPostHandledCallback);

  Q_INIT_RESOURCE(react_resources);

  loadFontsFromResources();

#ifdef BUILD_FOR_BUNDLE
  qInstallMessageHandler(saveMessage);
  runUbuntuServer();
#endif

  QQuickView view;
  ReactNativeProperties *rnp = new ReactNativeProperties(&view);
#ifdef BUILD_FOR_BUNDLE
  rnp->setCodeLocation("file:" + QGuiApplication::applicationDirPath() +
                       "/assets");
#endif

  utilities::registerReactTypes();

  QCommandLineParser p;
  p.setApplicationDescription("React Native host application");
  p.addHelpOption();
  p.addOptions({
      {{"R", "live-reload"}, "Enable live reload."},
      {{"H", "host"}, "Set packager host address.", rnp->packagerHost()},
      {{"P", "port"}, "Set packager port number.", rnp->packagerPort()},
      {{"L", "local"}, "Set path to the local packaged source", "not set"},
      {{"M", "plugins-path"}, "Set path to node modules", "./plugins"},
      {{"E", "executor"}, "Set Javascript executor", rnp->executor()},
  });
  p.process(app);
  rnp->setLiveReload(p.isSet("live-reload"));
  if (p.isSet("host"))
    rnp->setPackagerHost(p.value("host"));
  if (p.isSet("port"))
    rnp->setPackagerPort(p.value("port"));
  if (p.isSet("local"))
    rnp->setLocalSource(p.value("local"));
  if (p.isSet("plugins-path"))
    rnp->setPluginsPath(p.value("plugins-path"));
  if (p.isSet("executor"))
    rnp->setExecutor(p.value("executor"));

  view.rootContext()->setContextProperty("ReactNativeProperties", rnp);
  view.setSource(QUrl("qrc:///main.qml"));
  view.setResizeMode(QQuickView::SizeRootObjectToView);
  view.resize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
  view.show();

#ifdef BUILD_FOR_BUNDLE
  QTimer t;
  t.setInterval(500);
  QObject::connect(&t, &QTimer::timeout, [=]() { writeLogsToFile(); });
  t.start();
#endif

  return app.exec();
}

#ifdef BUILD_FOR_BUNDLE

QString getDataStoragePath() {
  QString dataStoragePath =
      QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
  QDir dir(dataStoragePath);
  if (!dir.exists()) {
    dir.mkpath(".");
  }
  return dataStoragePath;
}

void writeLogsToFile() {
  QMutexLocker locker(&consoleOutputMutex);
  QFile logFile(getDataStoragePath() + "/StatusIm.log");
  if (logFile.open(QIODevice::WriteOnly | QIODevice::Append)) {
    for (QString message : consoleOutputStrings) {
      logFile.write(message.toStdString().c_str());
    }
    consoleOutputStrings.clear();

    logFile.flush();
    logFile.close();
  }
}

void runUbuntuServer() {
  g_ubuntuServerProcess = new QProcess();
  g_ubuntuServerProcess->setWorkingDirectory(getDataStoragePath());
  g_ubuntuServerProcess->setProgram(QGuiApplication::applicationDirPath() +
                                    "/ubuntu-server");
  QObject::connect(g_ubuntuServerProcess, &QProcess::errorOccurred,
                   [=](QProcess::ProcessError) {
                     qDebug() << "process name: "
                              << g_ubuntuServerProcess->program();
                     qDebug() << "process error: "
                              << g_ubuntuServerProcess->errorString();
                   });

  QObject::connect(
      g_ubuntuServerProcess, &QProcess::readyReadStandardOutput, [=] {
        qDebug() << "ubuntu-server std: "
                 << g_ubuntuServerProcess->readAllStandardOutput().trimmed();
      });
  QObject::connect(
      g_ubuntuServerProcess, &QProcess::readyReadStandardError, [=] {
        QString output =
            g_ubuntuServerProcess->readAllStandardError().trimmed();
        qDebug() << "ubuntu-server err: " << output;
        if (output.contains("Server starting")) {
          ubuntuServerStarted = true;
        }
      });

  QObject::connect(QGuiApplication::instance(), &QCoreApplication::aboutToQuit,
                   [=]() {
                     qDebug() << "Kill ubuntu server";
                     g_ubuntuServerProcess->kill();
                   });

  qDebug() << "starting ubuntu server...";
  g_ubuntuServerProcess->start();
  qDebug() << "wait for started...";

  while (!ubuntuServerStarted) {
    QGuiApplication::processEvents();
  }

  qDebug() << "waiting finished";
}

void appendConsoleString(const QString &msg) {
  QMutexLocker locker(&consoleOutputMutex);
  consoleOutputStrings << msg;
}

void saveMessage(QtMsgType type, const QMessageLogContext &context,
                 const QString &msg) {

  QByteArray localMsg = msg.toLocal8Bit();
  QString message = localMsg + "\n";

  switch (type) {
  case QtDebugMsg:
    appendConsoleString(QString("Debug: %1 \n").arg(message));
    break;
  case QtInfoMsg:
    appendConsoleString(QString("Info: %1 \n").arg(message));
    break;
  case QtWarningMsg:
    appendConsoleString(QString("Warning: %1 \n").arg(message));
    break;
  case QtCriticalMsg:
    appendConsoleString(QString("Critical: %1 \n").arg(message));
    break;
  case QtFatalMsg:

    appendConsoleString(QString("Fatal: %1 \n").arg(message));
    writeLogsToFile();
    abort();
  }
}
#endif

#include "main.moc"
