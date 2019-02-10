#include "appconfig.h"

#include <QLoggingCategory>

#include <QSettings>

const QStringList loggingCategories = 
  {"UIManager", 
    "Flexbox", 
    "WebSocketModule", 
    "Networking", 
    "ViewManager", 
    "RCTNotification", 
    "default",
    "RCTStatus",
    "jsserver",
    "status"};

const QString SETTINGS_GROUP_NAME = "im/status";
const QString AppConfig::LOGGING_ENABLED = "logging_enabled";

Q_LOGGING_CATEGORY(APPCONFIG, "AppConfig")

AppConfig AppConfig::appConfig;

AppConfig::AppConfig() 
: settings("Status.im", "StatusDesktop") {
  settings.beginGroup(SETTINGS_GROUP_NAME);

  // Set default values
  if (settings.value(LOGGING_ENABLED).isNull()) {
    settings.setValue(LOGGING_ENABLED, false);
  }

  QStringList keys = settings.allKeys();
  for (int i = 0; i < keys.size(); ++i) {
    processFx(keys[i], settings.value(keys[i]));
  }
}

AppConfig& AppConfig::inst() {
  return appConfig;
}

QVariant AppConfig::getValue(const QString& name) const {
  return settings.value(name);
}

void AppConfig::setValue(const QString& name, const QVariant& value) {
  processFx(name, value);
  settings.setValue(name, value);
}

// This fn is for processing side-effects of a particular value
void AppConfig::processFx(const QString& name, const QVariant& value) const {
  //qCDebug(APPCONFIG) << "### processFx group" << settings.group() << " " << name << ": " << value;
  if (name == LOGGING_ENABLED) {
    bool enabled = value.toBool();
    //qCDebug(APPCONFIG) << "### processFx" << name << ": " << value << ": " << enabled;
    QLoggingCategory::setFilterRules(getLoggingFilterRules(enabled));
  }
}

QString AppConfig::getLoggingFilterRules(bool enabled) const {
   if (enabled) {
     return "UIManager=false\nFlexbox=false\nViewManager=false\nNetworking=false\nWebSocketModule=false";
   }
   else {
     QString filterRules;
     for (int i = 0; i < loggingCategories.size(); ++i) {
       filterRules += (loggingCategories.at(i) + "=false\n");
     }
     return filterRules;
   }
}

