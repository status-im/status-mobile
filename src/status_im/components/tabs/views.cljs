(ns status-im.components.tabs.views
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.react :as react]
            [status-im.components.tabs.styles :as tabs.styles]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview]]))

(defn- tab [{:keys [view-id title icon-active icon-inactive style-active selected-view-id on-press]}]
  (let [active? (= view-id selected-view-id)
        text-only? (nil? icon-active)]
    [react/touchable-highlight {:style    (merge tabs.styles/tab (if (and active? style-active) style-active))
                                :disabled active?
                                :on-press  #(if on-press (on-press view-id) (dispatch [:navigate-to-tab view-id]))}
     [react/view {:style tabs.styles/tab-container}
      (when-let [icon (if active? icon-active icon-inactive)]
        [react/view
         [vi/icon icon (tabs.styles/tab-icon active?)]])
      [react/view
       [react/text (merge (if text-only? {:uppercase? (get-in platform/platform-specific [:uppercase?])})
                          {:style (tabs.styles/tab-title active? text-only?)})
        title]]]]))

(defn- create-tab [index data selected-view-id on-press style-active]
  (let [data (merge data {:key              index
                          :index            index
                          :style-active     style-active
                          :selected-view-id selected-view-id
                          :on-press         on-press})]
    [tab data]))

(defview tabs-container [style children]
  (letsubs [tabs-hidden? [:tabs-hidden?]]
    [react/animated-view {:style          style
                          :pointer-events (if tabs-hidden? :none :auto)}
     children]))

(defn tabs [{:keys [style style-tab-active tab-list selected-view-id on-press]}]
  [tabs-container style
   (into
    [react/view tabs.styles/tabs-inner-container]
    (map-indexed #(create-tab %1 %2 selected-view-id on-press style-tab-active) tab-list))])
