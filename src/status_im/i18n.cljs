(ns status-im.i18n
  (:require
    [status-im.translations.en :as en]))

(def i18n (js/require "react-native-i18n"))
(set! (.-fallbacks i18n) true)
(set! (.-defaultSeparator i18n) "/")

(set! (.-translations i18n) (clj->js {:en en/translations}))

(defn label [path & options]
  (.t i18n (name path) (clj->js options)))
