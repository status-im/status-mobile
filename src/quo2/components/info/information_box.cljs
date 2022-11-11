(ns quo2.components.info.information-box
  (:require [quo2.theme :as theme]
            [react-native.core :as rn]
            [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.components.info.info-message :as info-message]))

(def themes
  {:light {:default     {:bg     colors/white
                         :border colors/neutral-20
                         :icon   colors/neutral-50
                         :text   colors/neutral-100}
           :informative {:bg     colors/primary-50-opa-5
                         :border colors/primary-50-opa-10
                         :icon   colors/primary-50
                         :text   colors/neutral-100}
           :error       {:bg     colors/danger-50-opa-5
                         :border colors/danger-50-opa-10
                         :icon   colors/danger-50
                         :text   colors/danger-50}
           :close-button colors/neutral-100}
   :dark  {:default     {:bg     colors/neutral-90
                         :border colors/neutral-70
                         :icon   colors/neutral-40
                         :text   colors/white}
           :informative {:bg     colors/primary-50-opa-5
                         :border colors/primary-50-opa-10
                         :icon   colors/white
                         :text   colors/white}
           :error       {:bg     colors/danger-50-opa-5
                         :border colors/danger-50-opa-10
                         :icon   colors/danger-50
                         :text   colors/danger-50}
           :close-button colors/white}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

(defn get-color-by-type [type key]
  (get-in themes [(theme/get-theme) type key]))

(defn information-box
  "[information-box opts \"message\"]
   opts
   {:type            :default/:informative/:error
    :closable?       true/false          ;; allow information box to be closed?
    :closed?         true/false          ;; information box's state
    :id              :information-box-id ;; unique id (required for closable? information box)
    :icon            :main-icons/info    ;; information box icon
    :style           style
    :button-label    \"PressMe\"         ;; add action button with label
    :on-button-press action              ;; (required for information box with button-label)
    :on-close        on-close            ;; (optional on-close call)"
  [{:keys [type closable? closed? id icon style button-label on-button-press on-close]} message]
  (let [background-color (get-color-by-type type :bg)
        border-color     (get-color-by-type type :border)
        icon-color       (get-color-by-type type :icon)
        text-color       (get-color-by-type type :text)
        include-button?  (not (string/blank? button-label))]
    (when-not closed?
      [rn/view {:accessibility-label (or id :information-box)
                :style               (merge {:background-color   background-color
                                             :border-color       border-color
                                             :border-width       1
                                             :border-radius      12
                                             :padding-top        (if include-button? 10 11)
                                             :padding-bottom     (if include-button? 12 11)
                                             :padding-horizontal 16} style)}
       [rn/view {:style {:flex-direction :row
                         :align-self     :flex-start}}
        [info-message/info-message {:size        :default
                                    :icon        icon
                                    :text-color  text-color
                                    :icon-color  icon-color} message]
        (when closable?
          [rn/touchable-opacity
           {:on-press            on-close
            :accessibility-label (str (or id "information-box") "-close-button")}
           [quo2.icons/icon :i/close {:size            12
                                      :color           (get-color :close-button)
                                      :container-style {:margin-top  4
                                                        :margin-left 8}}]])]
       (when include-button?
         [quo2.button/button {:type     :primary
                              :size     24
                              :on-press on-button-press
                              :style    {:margin-left 20
                                         :margin-top  8
                                         :align-self :flex-start}} button-label])])))
