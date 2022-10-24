(ns status-im.navigation2.roots
  (:require [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]))

(defn status-bar-options []
  (if platform/android?
    {:navigationBar {:backgroundColor colors/neutral-100}
     :statusBar     {:backgroundColor :transparent
                     :style           :light
                     :drawBehind      true}}
    {:statusBar {:style :light}}))

(defn roots []
  {:shell-stack
   {:root
    {:stack {:id       :shell-stack
             :children [{:component {:name    :chat-stack
                                     :id      :chat-stack
                                     :options (merge (status-bar-options)
                                                     {:topBar {:visible false}})}}]}}}})
