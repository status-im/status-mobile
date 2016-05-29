(ns status-im.i18n
  (:require
    [status-im.translations.en :as en]
    [status-im.utils.utils :as u]))

(def i18n (u/require "react-native-i18n"))
(set! (.-fallbacks i18n) true)
(set! (.-defaultSeparator i18n) "/")

(set! (.-translations i18n) (clj->js {:en en/translations}))

(defn label [path & options]
  (if (exists? i18n.t)
    (.t i18n (name path) (clj->js options))
    (name path)))
