/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#ifndef RCTMAPBOXGLMANAGER_H
#define RCTMAPBOXGLMANAGER_H

#include "moduleinterface.h"
#include "componentmanagers/viewmanager.h"

class RCTMapboxGLManagerPrivate;
class RCTMapboxGLManager : public ViewManager {
    Q_OBJECT
    Q_INTERFACES(ModuleInterface)
    Q_DECLARE_PRIVATE(RCTMapboxGLManager)

public:
    Q_INVOKABLE RCTMapboxGLManager(QObject* parent = 0);
    ~RCTMapboxGLManager();

    virtual ViewManager* viewManager() override;
    virtual QString moduleName() override;
    virtual QVariantMap constantsToExport() override;

    Q_INVOKABLE REACT_PROMISE void setAccessToken(QString accessToken, const ModuleInterface::ListArgumentBlock& resolve, const ModuleInterface::ListArgumentBlock& reject);

private:
    virtual void configureView(QQuickItem* view) const override;

    QScopedPointer<RCTMapboxGLManagerPrivate> d_ptr;
};

#endif // RCTMAPBOXGLMANAGER_H
