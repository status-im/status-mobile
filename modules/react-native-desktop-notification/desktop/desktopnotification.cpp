/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include "desktopnotification.h"
#include "bridge.h"
#include "eventdispatcher.h"

#include "libsnore/application.h"
#include "libsnore/snore.h"

#ifdef Q_OS_MAC
#include "libsnore/snore_static_plugins.h"
#endif

#include <QGuiApplication>
#include <QDebug>

Q_LOGGING_CATEGORY(NOTIFICATION, "RCTNotification")

#if defined(Q_OS_LINUX) || defined(Q_OS_WIN)
namespace SnorePlugin {}


using namespace SnorePlugin;
#if defined(Q_OS_LINUX)
Q_IMPORT_PLUGIN(Freedesktop)
#elif defined(Q_OS_WIN)
Q_IMPORT_PLUGIN(WindowsToast)
#endif
Q_IMPORT_PLUGIN(Snore)

static void loadSnoreResources()
{
    // prevent multiple symbols
     static const auto load = []() {
         Q_INIT_RESOURCE(snore);
         Q_INIT_RESOURCE(snore_notification);
     };
     load();
}

Q_COREAPP_STARTUP_FUNCTION(loadSnoreResources)
#endif // defined(Q_OS_LINUX) || defined(Q_OS_WIN)

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<DesktopNotification *>(); }
} registerMetaType;

const QString NewMessageAlert = QStringLiteral("NewMessage");
} // namespace

class DesktopNotificationPrivate {
public:
  Bridge *bridge = nullptr;
  Snore::Application snoreApp;
};

DesktopNotification::DesktopNotification(QObject *parent)
    : QObject(parent), d_ptr(new DesktopNotificationPrivate) {
    connect(qApp, &QGuiApplication::focusWindowChanged, this, [=](QWindow *focusWindow){
       m_appHasFocus = (focusWindow != nullptr);
    });

  if (Snore::SnoreCore::instance().pluginNames().isEmpty()) {
    Snore::SnoreCore::instance().loadPlugins(Snore::SnorePlugin::Backend);
  }

  qCDebug(NOTIFICATION) << "DesktopNotification::DesktopNotification List of all loaded Snore plugins:"
                        << Snore::SnoreCore::instance().pluginNames();

  Snore::Icon icon(":/icon.png");
  d_ptr->snoreApp = Snore::Application(QCoreApplication::applicationName(), icon);
  d_ptr->snoreApp.hints().setValue("windows-app-id", "StatusIm.Status.Desktop.1");
  d_ptr->snoreApp.addAlert(Snore::Alert(NewMessageAlert, icon));

  Snore::SnoreCore::instance().registerApplication(d_ptr->snoreApp);
  Snore::SnoreCore::instance().setDefaultApplication(d_ptr->snoreApp);

  qCDebug(NOTIFICATION) << "DesktopNotification::DesktopNotification Current notification backend:"
                        << Snore::SnoreCore::instance().primaryNotificationBackend();
}

DesktopNotification::~DesktopNotification() {
  Snore::SnoreCore::instance().deregisterApplication(d_ptr->snoreApp);
}

void DesktopNotification::setBridge(Bridge *bridge) {
  Q_D(DesktopNotification);
  d->bridge = bridge;
}

QString DesktopNotification::moduleName() { return "DesktopNotification"; }

QList<ModuleMethod *> DesktopNotification::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap DesktopNotification::constantsToExport() { return QVariantMap(); }

void DesktopNotification::displayNotification(QString title, QString body, bool prioritary) {
  Q_D(DesktopNotification);
  qCDebug(NOTIFICATION) << "::displayNotification";

  if (m_appHasFocus) {
      qCDebug(NOTIFICATION) << "Not displaying notification since an application window is active";
      return;
  }

  Snore::Notification notification(
      d_ptr->snoreApp, d_ptr->snoreApp.alerts()[NewMessageAlert], title,
      body, Snore::Icon::defaultIcon(),
      prioritary ? Snore::Notification::Prioritys::High : Snore::Notification::Prioritys::Normal);
  Snore::SnoreCore::instance().broadcastNotification(notification);
}

void DesktopNotification::setDockBadgeLabel(const QString label) {
  Snore::SnoreCore::instance().setDockBadgeLabel(label);
}
