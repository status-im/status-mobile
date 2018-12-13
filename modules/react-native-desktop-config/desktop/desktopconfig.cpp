#include "desktopconfig.h"
#include "bridge.h"

#include <QCoreApplication>
#include <QDebug>
#include "../../../desktop/appconfig.h"

Q_LOGGING_CATEGORY(DESKTOPCONFIG, "DesktopConfig")

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<DesktopConfig *>(); }
} registerMetaType;
} // namespace


DesktopConfig::DesktopConfig(QObject *parent)
    : QObject(parent) {
}

DesktopConfig::~DesktopConfig() {
}

void DesktopConfig::setBridge(Bridge *bridge) {
  this->bridge = bridge;
}

QString DesktopConfig::moduleName() { return "DesktopConfigManager"; }

QList<ModuleMethod *> DesktopConfig::methodsToExport() {
  return QList<ModuleMethod *>{};
}

QVariantMap DesktopConfig::constantsToExport() { return QVariantMap(); }

void DesktopConfig::getLoggingEnabled(double callback) {
  bridge->invokePromiseCallback(callback, QVariantList{AppConfig::inst().getLoggingEnabled()});
}

void DesktopConfig::setLoggingEnabled(bool enable) {
  //qCDebug(DESKTOPCONFIG) << "### setLoggingEnabled " << enable;
  AppConfig::inst().setLoggingEnabled(enable);
}

