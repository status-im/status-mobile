(ns status-im.components.context-menu
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view touchable-highlight]]))

(def react-native-popup-menu (js/require "react-native-popup-menu"))

(defn get-property [name]
  (aget react-native-popup-menu name))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(def menu (get-class "Menu"))
(def menu-context (get-class "MenuContext"))
(def menu-trigger (get-class "MenuTrigger"))
(def menu-options (get-class "MenuOptions"))
(def menu-option (get-class "MenuOption"))

(defn context-menu [trigger options]
  (if ios?
    [touchable-highlight
     {:on-press #(dispatch [:open-context-menu (:list-selection-fn platform-specific) options])} ;TODO: temporary, should be better way
     [view
      trigger]]
    [menu {:onSelect #(when % (do (%) nil))}
     [menu-trigger trigger]
     [menu-options st/context-menu
      (for [option options]
        ^{:key option}
        [menu-option option])]]))