/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include "rctmapboxglmanager.h"

#include <QDebug>
#include <QQmlComponent>
#include <QQmlProperty>
#include <QQuickItem>
#include <QString>
#include <QVariant>

#include "attachedproperties.h"
#include "bridge.h"
#include "layout/flexbox.h"
#include "propertyhandler.h"
#include "utilities.h"

using namespace utilities;

namespace {
struct RegisterQMLMetaType {
    RegisterQMLMetaType() {
        qRegisterMetaType<RCTMapboxGLManager*>();
    }
} registerMetaType;
} // namespace

class RCTMapboxGLManagerPrivate {};

RCTMapboxGLManager::RCTMapboxGLManager(QObject* parent) : ViewManager(parent), d_ptr(new RCTMapboxGLManagerPrivate) {}

RCTMapboxGLManager::~RCTMapboxGLManager() {}

QString RCTMapboxGLManager::moduleName() {
    return "RCTMapboxGLManager";
}

ViewManager* RCTMapboxGLManager::viewManager() {
    return this;
}

void RCTMapboxGLManager::configureView(QQuickItem* view) const {
    ViewManager::configureView(view);
}

QVariantMap RCTMapboxGLManager::constantsToExport() {
    return QVariantMap{{"mapStyles", QVariantMap{{"emerald", ""}}},
                       {"userTrackingMode", QVariantMap{{"none", 1}}}};
} 

void RCTMapboxGLManager::setAccessToken(QString accessToken, const ModuleInterface::ListArgumentBlock& resolve, const ModuleInterface::ListArgumentBlock& reject) {
    resolve(bridge(), QVariantList());
}

#include "rctmapboxglmanager.moc"

