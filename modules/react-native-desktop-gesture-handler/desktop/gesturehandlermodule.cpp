/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include "gesturehandlermodule.h"
#include "bridge.h"
#include "eventdispatcher.h"

#include <QGuiApplication>
#include <QQuickItem>
#include <QScreen>

namespace {
struct RegisterQMLMetaType {
  RegisterQMLMetaType() { qRegisterMetaType<GestureHandlerModule *>(); }
} registerMetaType;
} // namespace


class GestureHandlerModulePrivate {
public:
    Bridge* bridge = nullptr;
};

GestureHandlerModule::GestureHandlerModule(QObject* parent) : QObject(parent), d_ptr(new GestureHandlerModulePrivate) {}

GestureHandlerModule::~GestureHandlerModule() {}

void GestureHandlerModule::setBridge(Bridge* bridge) {
    Q_D(GestureHandlerModule);
    d->bridge = bridge;
}

QString GestureHandlerModule::moduleName() {
    return "RNGestureHandlerModule";
}

QList<ModuleMethod*> GestureHandlerModule::methodsToExport() {
    return QList<ModuleMethod*>{};
}

QVariantMap GestureHandlerModule::constantsToExport() {
    Q_D(GestureHandlerModule);

    QVariantMap directionValues{{"RIGHT", 1}, {"LEFT", 2}, {"UP", 4}, {"DOWN", 8}};

    //    QRect screenGeometry = screen->geometry();
    //    QVariantMap screenValues{{"fontScale", 8},
    //                             {"width", screenGeometry.width()},
    //                             {"height", screenGeometry.height()},
    //                             {"scale", screen->devicePixelRatio()}};

    //    QVariantMap values{{"screen", screenValues}, {"window", windowValues}};

    return QVariantMap{{"Direction", directionValues}};
}

void GestureHandlerModule::handleSetJSResponder(int viewTag, void* blockNativeResponder) {}

void GestureHandlerModule::handleClearJSResponder() {}

void GestureHandlerModule::createGestureHandler(const QString& handlerName, int handlerTag, void* config) {}
void GestureHandlerModule::attachGestureHandler(int handlerTag, int viewTag) {}
void GestureHandlerModule::updateGestureHandler(int handlerTag, void* config) {}
void GestureHandlerModule::dropGestureHandler(int handlerTag) {}
