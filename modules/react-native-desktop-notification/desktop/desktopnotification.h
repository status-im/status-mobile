/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#ifndef DESKTOPNOTIFICATION_H
#define DESKTOPNOTIFICATION_H

#include "moduleinterface.h"

#include <QVariantMap>

class DesktopNotificationPrivate;
class DesktopNotification : public QObject, public ModuleInterface {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)

    Q_DECLARE_PRIVATE(DesktopNotification)

public:
    Q_INVOKABLE DesktopNotification(QObject* parent = 0);
    ~DesktopNotification();

    void setBridge(Bridge* bridge) override;

    QString moduleName() override;
    QList<ModuleMethod*> methodsToExport() override;
    QVariantMap constantsToExport() override;

    Q_INVOKABLE void sendNotification(QString text);
private:
    QScopedPointer<DesktopNotificationPrivate> d_ptr;
};

#endif // DESKTOPNOTIFICATION_H
