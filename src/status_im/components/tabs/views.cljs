(ns status-im.components.tabs.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.tabs.styles :as styles]))

(defn- tab [{:keys [view-id title icon-active icon-inactive selected-view-id]}]
  (let [active? (= view-id selected-view-id)]
    [react/touchable-highlight {:style    styles/tab
                                :disabled active?
                                :on-press  #(dispatch [:navigate-to-tab view-id])}
     [react/view {:style styles/tab-container}
      (when-let [icon (if active? icon-active icon-inactive)]
        [react/view
         [vi/icon icon (styles/tab-icon active?)]])
      [react/view
       [react/text {:style (styles/tab-title active?)}
        title]]]]))

(defn- create-tab [index data selected-view-id]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id})]
    [tab data]))

(defview tabs-container [style children]
  (letsubs [tabs-hidden? [:tabs-hidden?]]
    [react/animated-view {:style         (merge style
                                                styles/tabs-container-line)
                          :pointer-events (if tabs-hidden? :none :auto)}
     children]))

(defn tabs [{:keys [style tab-list selected-view-id]}]
  [tabs-container style
   (into
     [react/view styles/tabs-inner-container]
     (map-indexed #(create-tab %1 %2 selected-view-id) tab-list))])
