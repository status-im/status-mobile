/**
 * Copyright (c) 2017-present, Status Research and Development GmbH.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

import QtQuick 2.4
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3

Rectangle {
  id: root
  width: 384
  height: 640

  ColumnLayout {
      anchors.centerIn: parent
      Text {
        Layout.alignment: Qt.AlignCenter
        text: "Oh, no! StatusIm application just crashed!"
        font.bold: true
        font.pointSize: 25
      }
      Text {
        Layout.alignment: Qt.AlignCenter
        Layout.topMargin: 20
        text: "Please report us crash log files to allow us fix the issue!"
        font.bold: true
        font.pointSize: 20
      }
      RowLayout {
          Layout.alignment: Qt.AlignCenter
          Layout.topMargin: 40
          spacing: 25

          Button {
            Layout.minimumWidth: 150
            text: "Report (highly appreciated)"
            onClicked: reportPublisher.submit()
          }

          Button {
            text: "Restart and Quit"
            onClicked: reportPublisher.restartAndQuit()
          }

          Button {
            text: "Just Quit"
            onClicked: reportPublisher.quit()
          }
      }
      RowLayout {
          Layout.alignment: Qt.AlignCenter
          Layout.topMargin: 100

          TextEdit {
              readOnly: true
              Layout.maximumWidth: 500
              wrapMode: TextEdit.Wrap
              selectByMouse: true
              font.pointSize: 12
              text: "Log files directory:\n" + reportPublisher.resolveDataStoragePath()
          }

          Button {
            text: "View"
            onClicked: reportPublisher.showDirectory()
          }
      }
  }
}

