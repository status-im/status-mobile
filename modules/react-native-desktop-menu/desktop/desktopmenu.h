#ifndef DESKTOPMENU_H
#define DESKTOPMENU_H

#include "moduleinterface.h"

#include <QLoggingCategory>
#include <QVariantMap>

Q_DECLARE_LOGGING_CATEGORY(MENU)

class DesktopMenuPrivate;
class DesktopMenu : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

    Q_DECLARE_PRIVATE(DesktopMenu)

public:
    Q_INVOKABLE DesktopMenu(QObject* parent = 0);
    virtual ~DesktopMenu();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

    Q_INVOKABLE void show(const QStringList& items, double callback);

private:
    QScopedPointer<DesktopMenuPrivate> d_ptr;
};

#endif // DESKTOPMENU_H
