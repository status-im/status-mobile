#include "appconfig.h"

#include <QLoggingCategory>


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
Q_LOGGING_CATEGORY(APPCONFIG, "AppConfig")

AppConfig AppConfig::appConfig;

AppConfig::AppConfig() {
}

AppConfig& AppConfig::inst() {
  return appConfig;
}

bool AppConfig::getLoggingEnabled() const {
  return loggingEnabled;
}

void AppConfig::setLoggingEnabled(bool enabled) {
  //qCDebug(APPCONFIG) << "### appconfig setLoggingEnabled " << enabled;
  QLoggingCategory::setFilterRules(getLoggingFilterRules(enabled));
  this->loggingEnabled = enabled;
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
