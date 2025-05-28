(ns status-im.contexts.browser.constants
  (:require [react-native.core :as rn]))

(def footer-height 80)
(def browser-width (-> (rn/get-window) :width))
(def browser-height "100%")

(def default-chain-id 1)
