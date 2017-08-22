(ns status-im.components.tabs.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                animated-view
                                                text-input
                                                text
                                                image
                                                touchable-highlight]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]
            [status-im.components.animation :as anim]
            [status-im.utils.platform :as p]))

(defn tab [{:keys [view-id title icon-active icon-inactive selected-view-id]}]
  (let [active?   (= view-id selected-view-id)]
    [touchable-highlight {:style    st/tab
                          :disabled active?
                          :onPress  #(dispatch [:navigate-to-tab view-id])}
     [view {:style st/tab-container}
      (when-let [icon (if active? icon-active icon-inactive)]
        [view
         [image {:source {:uri icon}
                 :style  st/tab-icon}]])
      [view
       [text {:style (st/tab-title active?)
              :font  (if (and p/ios? active?) :medium :regular)}
        title]]]]))

(defn- create-tab [index data selected-view-id]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id})]
    [tab data]))

(defn- tabs-container [style children]
  (let [tabs-hidden?    (subscribe [:tabs-hidden?])
        shadows?        (get-in p/platform-specific [:tabs :tab-shadows?])]
    [animated-view {:style         (merge style
                                          (when-not shadows? st/tabs-container-line))
                    :pointerEvents (if @tabs-hidden? :none :auto)}
     children]))

(defn tabs [{:keys [style tab-list selected-view-id]}]
  [tabs-container style
   (into
     [view st/tabs-inner-container]
     (map-indexed #(create-tab %1 %2 selected-view-id) tab-list))])
