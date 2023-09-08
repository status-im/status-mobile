(ns quo.core
  (:require [quo.components.animated-header :as animated-header]
            [quo.components.button.view :as button]
            [quo.components.controls.view :as controls]
            [quo.components.header :as header]
            [quo.components.list.footer :as list-footer]
            [quo.components.list.header :as list-header]
            [quo.components.list.index :as list-index]
            [quo.components.list.item :as list-item]
            [quo.components.separator :as separator]
            [quo.components.text :as text]
            [quo.components.text-input :as text-input]
            [quo.components.tooltip :as tooltip]
            [quo.design-system.colors :as colors]))

(def text text/text)
(def header header/header)
(def animated-header animated-header/header)
(def text-input text-input/text-input)
(def tooltip tooltip/tooltip)
(def button button/button)
(def list-header list-header/header)
(def list-footer list-footer/footer)
(def list-item list-item/list-item)
(def list-index list-index/index)
(def switch controls/switch)
(def radio controls/radio)
(def checkbox controls/checkbox)
(def separator separator/separator)
(def get-color colors/get-color)
