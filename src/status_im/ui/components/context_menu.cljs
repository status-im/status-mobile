(ns status-im.ui.components.context-menu
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.react :as react]
            [goog.object :as object]
            [reagent.core :as r]
            [status-im.ui.components.styles :as st]
            [status-im.utils.platform :as platform]))

(defn- get-property [name]
  (object/get rn-dependencies/popup-menu name))

(defn- get-class [name]
  (react/adapt-class (get-property name)))

(def menu (get-class "Menu"))
(def menu-context (get-class "MenuContext"))
(def menu-trigger (get-class "MenuTrigger"))
(def menu-options (get-class "MenuOptions"))
(def menu-option (get-class "MenuOption"))

(defn- context-menu-options [custom-styles]
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

(defn- context-menu-text [destructive?]
  {:font-size   15
   :line-height 20
   :color       (if destructive? st/color-light-red st/text1-color)})

(defn context-menu [trigger options & custom-styles trigger-style]
  (if platform/ios?
    [react/touchable-highlight {:style    trigger-style
                                :on-press #(action-sheet/show options)}
     [react/view
      trigger]]
    [menu {:onSelect #(when % (do (%) nil))}
     [menu-trigger {:style trigger-style} trigger]
     [menu-options (context-menu-options custom-styles)
      (for [{:keys [style action destructive?] :as option} options]
        ^{:key option}
        [menu-option {:value action}
         [react/text {:style (merge (context-menu-text destructive?) style)}
          (:label option)]])]]))
