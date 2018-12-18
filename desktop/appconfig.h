#ifndef APPCONFIG_H
#define APPCONFIG_H

#include <QString>
#include <QVariant>
#include <QSettings>

// This class is intended to store app configuration
// modifiable from JS side
// Currently, only logging-related settings are here
// that are used by react-native-desktop-config module
class AppConfig {
public:

  static AppConfig& inst();

  QVariant getValue(const QString& name) const;
  void setValue(const QString& name, const QVariant& value);

  const static QString LOGGING_ENABLED;
private:
  AppConfig();

  static AppConfig appConfig;
  QSettings settings;

  QString getLoggingFilterRules(bool enabled) const;
  void processFx(const QString& name, const QVariant& value) const;
};
#endif // APPCONFIG_H

