(ns status-im.navigation2.roots
  (:require [quo.theme :as theme]
            [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]))

(defn status-bar-options []
  (if platform/android?
    {:navigationBar {:backgroundColor colors/neutral-80}
     :statusBar     {:backgroundColor (colors/theme-colors colors/neutral-5 colors/neutral-95)
                     :style           (if (theme/dark?) :light :dark)}}
    {:statusBar {:style (if (theme/dark?) :light :dark)}}))

(defn roots []
  {:home-stack
   {:root
    {:stack {:id       :home-stack
             :children [{:component {:name    :chat-stack
                                     :id      :chat-stack
                                     :options (merge (status-bar-options)
                                                     {:topBar {:visible false}})}}]}}}})
