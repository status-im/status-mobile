(ns status-im2.common.theme.core
  (:require [quo.theme :as quo]
            [quo2.theme :as quo2]
            [react-native.core :as rn]))

(def device-theme (atom (rn/get-color-scheme)))

;; Note - don't use value returned by change listener
;; https://github.com/facebook/react-native/issues/28525
(defn add-device-theme-change-listener
  [callback]
  (rn/appearance-add-change-listener #(let [theme (rn/get-color-scheme)]
                                        (when-not (= theme @device-theme)
                                          (reset! device-theme theme)
                                          (callback (keyword theme))))))

(defn device-theme-dark?
  []
  (= @device-theme "dark"))

(defn set-theme
  [value]
  (quo/set-theme value)
  (quo2/set-theme value))
