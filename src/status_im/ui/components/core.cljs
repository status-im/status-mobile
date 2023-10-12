(ns status-im.ui.components.core
  (:require [status-im.ui.components.animated-header :as animated-header]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.controls.view :as controls]
            [status-im.ui.components.header :as header]
            [status-im.ui.components.list.footer :as list-footer]
            [status-im.ui.components.list.header :as list-header]
            [status-im.ui.components.separator :as separator]
            [status-im.ui.components.text :as text]
            [status-im.ui.components.text-input :as text-input]))

(def text text/text)
(def header header/header)
(def animated-header animated-header/header)
(def text-input text-input/text-input)
(def button button/button)
(def list-header list-header/header)
(def list-footer list-footer/footer)

(def radio controls/radio)
(def separator separator/separator)
