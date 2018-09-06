/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

#include <QDebug>
#include <QGuiApplication>
#include <QQmlContext>
#include <QQuickView>

#include "reportpublisher.h"

const int MAIN_WINDOW_WIDTH = 1024;
const int MAIN_WINDOW_HEIGHT = 768;
const int INPUT_ARGUMENTS_COUNT = 6;

const int MINIDUMP_FILE_PATH_ARG_INDEX = 1;
const int CRASHED_EXECUTABLE_PATH_ARG_INDEX = 2;
const int LOGS_PATH_INDEX = 5;

int main(int argc, char **argv) {

  QGuiApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
  QGuiApplication app(argc, argv);

  if (argc != INPUT_ARGUMENTS_COUNT) {
    return 1;
  }

  app.setApplicationName("Crash Report");

  ReportPublisher reportPublisher(argv[MINIDUMP_FILE_PATH_ARG_INDEX],
                                  argv[CRASHED_EXECUTABLE_PATH_ARG_INDEX],
                                  argv[LOGS_PATH_INDEX]);

  QQuickView view;
  view.rootContext()->setContextProperty("reportPublisher", &reportPublisher);
  view.setSource(QUrl("qrc:///main.qml"));
  view.setResizeMode(QQuickView::SizeRootObjectToView);
  view.resize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
  view.show();

  return app.exec();
}
