#ifndef APPCONFIG_H
#define APPCONFIG_H

#include <QString>

// This class is intended to store app configuration
// modifiable from JS side
// Currently, only logging-related settings are here
// that are used by react-native-desktop-config module
class AppConfig {
public:

  static AppConfig& inst();

  bool getLoggingEnabled() const;
  void setLoggingEnabled(bool enable);

private:
  AppConfig();

  bool loggingEnabled = false;
  static AppConfig appConfig;

  QString getLoggingFilterRules(bool enabled) const;
};
#endif // APPCONFIG_H
