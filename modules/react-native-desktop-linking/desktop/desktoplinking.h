#ifndef DESKTOPLINKING_H
#define DESKTOPLINKING_H

#include "moduleinterface.h"

#include <QLoggingCategory>
#include <QVariantMap>

Q_DECLARE_LOGGING_CATEGORY(LINKING)

class DesktopLinkingPrivate;
class DesktopLinking : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

    Q_DECLARE_PRIVATE(DesktopLinking)

public:
    Q_INVOKABLE DesktopLinking(QObject* parent = 0);
    ~DesktopLinking();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

signals:
    void urlOpened(QString path);
    void fileOpened(QString path);

public slots:
    void handleURL(const QString url);

private:
    QScopedPointer<DesktopLinkingPrivate> d_ptr;
    bool eventFilter(QObject* obj, QEvent* event) override;

};

#endif // DESKTOPLINKING_H
