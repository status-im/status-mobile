#ifndef DESKTOPSHORTCUTS_H
#define DESKTOPSHORTCUTS_H

#include "moduleinterface.h"

#include <QLoggingCategory>
#include <QMap>

Q_DECLARE_LOGGING_CATEGORY(SHORTCUTS)

class DesktopShortcutsPrivate;
class DesktopShortcuts : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

public:
    Q_INVOKABLE DesktopShortcuts(QObject* parent = 0);
    virtual ~DesktopShortcuts();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

    Q_INVOKABLE void registerShortcuts(const QStringList& shortcuts);

signals:
    void shortcutInvoked(const QString& shortcut);

public slots:
    void onShortcutInvoked(const QString& shortcut);

private:
    Bridge* bridge;

    QStringList registeredShortcuts;
    bool eventFilter(QObject* obj, QEvent* event) override;
};

#endif // DESKTOPSHORTCUTS_H
