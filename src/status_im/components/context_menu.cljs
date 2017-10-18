(ns status-im.components.context-menu
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [status-im.components.react :as rn]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn- get-property [name]
  (aget rn-dependencies/popup-menu name))

(defn- get-class [name]
  (rn/adapt-class (get-property name)))

(def menu (get-class "Menu"))
(def menu-context (get-class "MenuContext"))
(def menu-trigger (get-class "MenuTrigger"))
(def menu-options (get-class "MenuOptions"))
(def menu-option (get-class "MenuOption"))

(defn context-menu-options [custom-styles]
  {:customStyles {:optionsContainer
                  (merge {:elevation      2
                          :margin-top     0
                          :padding-top    8
                          :width          164
                          :padding-bottom 8}
                         (:optionsContainer custom-styles))
                  :optionWrapper
                  (merge {:padding-left    16
                          :padding-right   16
                          :justify-content :center
                          :height          48}
                         (:optionWrapper custom-styles))}})

(defn context-menu-text [destructive?]
  {:font-size   15
   :line-height 20
   :color       (if destructive? st/color-light-red st/text1-color)})

(def list-selection-fn (:list-selection-fn platform-specific))

(defn open-ios-menu [options]
  (list-selection-fn {:options  options
                      :callback (fn [index]
                                  (when (< index (count options))
                                    (when-let [handler (:value (nth options index))]
                                      (handler))))})
  nil)

(defn context-menu [trigger options & custom-styles trigger-style]
  (if ios?
    [rn/touchable-highlight {:style    trigger-style
                             :on-press #(open-ios-menu options)}
     [rn/view
      trigger]]
    [menu {:onSelect #(when % (do (%) nil))}
     [menu-trigger {:style trigger-style} trigger]
     [menu-options (context-menu-options custom-styles)
      (for [{:keys [style value destructive?] :as option} options]
        ^{:key option}
        [menu-option {:value value}
         [rn/text {:style (merge (context-menu-text destructive?) style)}
          (:text option)]])]]))
