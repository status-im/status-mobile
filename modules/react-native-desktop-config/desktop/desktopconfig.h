#ifndef DESKTOPCONFIG_H
#define DESKTOPCONFIG_H

#include "moduleinterface.h"

#include <QLoggingCategory>

Q_DECLARE_LOGGING_CATEGORY(CONFIG)

class Bridge;
class DesktopConfig : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

public:
    Q_INVOKABLE DesktopConfig(QObject* parent = 0);
    virtual ~DesktopConfig();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

    Q_INVOKABLE void getLoggingEnabled(double callback);
    Q_INVOKABLE void setLoggingEnabled(bool enable);

private:
    Bridge* bridge = nullptr;
};

#endif // DESKTOPCONFIG_H
