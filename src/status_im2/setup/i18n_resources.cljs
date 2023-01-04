(ns status-im2.setup.i18n-resources
  (:require [i18n.i18n :as i18n]))

(defonce loaded-languages
         (atom
          (conj #{:en} i18n/default-device-language)))
