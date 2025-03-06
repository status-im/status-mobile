(ns legacy.status-im.ui.components.core
  (:require
    [legacy.status-im.ui.components.button.view :as button]
    [legacy.status-im.ui.components.controls.view :as controls]
    [legacy.status-im.ui.components.header :as header]
    [legacy.status-im.ui.components.list.header :as list-header]
    [legacy.status-im.ui.components.separator :as separator]
    [legacy.status-im.ui.components.text :as text]
    [legacy.status-im.ui.components.text-input :as text-input]))

(def text text/text)
(def header header/header)
(def text-input text-input/text-input)
(def button button/button)
(def list-header list-header/header)
(def radio controls/radio)
(def separator separator/separator)
