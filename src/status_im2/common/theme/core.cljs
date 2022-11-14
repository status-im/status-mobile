(ns status-im2.common.theme.core
  (:require [react-native.core :as rn]))

(def initial-mode (atom (rn/get-color-scheme)))

;; Note - don't use value returned by change listener
;; https://github.com/facebook/react-native/issues/28525
(defn add-mode-change-listener [callback]
  (rn/appearance-add-change-listener #(let [mode (rn/get-color-scheme)]
                                        (when-not (= mode @initial-mode)
                                          (reset! initial-mode mode)
                                          (callback (keyword mode))))))

(defn dark-mode? []
  (= @initial-mode "dark"))