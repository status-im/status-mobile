(ns status-im.components.context-menu
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view touchable-highlight text]]))

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

(def list-selection-fn (:list-selection-fn platform-specific))

(defn open-ios-menu [options]
  (let [cancel-option {:text (label :t/cancel)}
        options       (conj options cancel-option)]
    (list-selection-fn {:options  options
                        :callback (fn [index]
                                    (when (< index (count options))
                                      (when-let [handler (:value (nth options index))]
                                        (handler))))}))
  nil)

(defn context-menu [trigger options & [customStyles]]
  (if ios?
    [touchable-highlight
     {:on-press #(open-ios-menu options)}
     [view
      trigger]]
    [menu {:onSelect #(when % (do (%) nil))}
     [menu-trigger trigger]
     [menu-options (st/context-menu-options customStyles)
      (for [{:keys [style value destructive?] :as option} options]
        ^{:key option}
        [menu-option {:value value}
         [text {:style (merge (st/context-menu-text destructive?) style)}
          (:text option)]])]]))
