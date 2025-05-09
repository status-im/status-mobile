(ns status-chat.components.chat.message
  (:require [clojure.string :as string]
            [quo.context :as quo.context]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn container
  [{:keys [customization-color]} & children]
  (let [theme (quo.context/use-theme)]
    (into [rn/view
           {:style {:max-width          (-> (rn/get-window) :width (* 0.7))
                    :align-self         :flex-start
                    :padding            8
                    :padding-horizontal 16
                    :background-color   (colors/resolve-color customization-color theme)
                    :border-radius      20
                    :margin-bottom      12}}
           children])))

(defn text
  [text-value]
  [quo/text
   {:weight :semi-bold
    :size   :paragraph-2} text-value])

(defn- mask-text
  [s]
  (string/replace s #"." "*"))

(defn hidden-text
  [text-value]
  [quo/text
   {:weight :bold
    :size   :paragraph-2}
   (mask-text text-value)])

(defn extra-content
  [{:keys [on-press]} & children]
  (into [rn/pressable
         {:on-press on-press
          :style    {:padding          12
                     :align-items      :center
                     :background-color colors/white-opa-30
                     :border-radius    20
                     :margin-top       12}}]
        children))
