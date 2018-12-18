#ifndef DESKTOPCONFIG_H
#define DESKTOPCONFIG_H

#include "moduleinterface.h"
#include <QVariant>
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

    Q_INVOKABLE void getValue(const QString& name, double callback);
    Q_INVOKABLE void setValue(const QString& name, const QVariant& value);

private:
    Bridge* bridge = nullptr;
};

#endif // DESKTOPCONFIG_H
