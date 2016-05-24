(ns status-im.components.tabs.tabs
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text-input
                                                text
                                                image
                                                touchable-highlight
                                                linear-gradient]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]
            [status-im.components.tabs.tab :refer [tab]]))

(defn create-tab [index data selected-view-id]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id})]
    [tab data]))

(defview tabs [{:keys [style tab-list selected-view-id]}]
  (let [style (merge st/tabs style)]
    [view {:style style}
     [linear-gradient {:colors ["rgba(24, 52, 76, 0.01)" "rgba(24, 52, 76, 0.085)" "rgba(24, 52, 76, 0.165)"]
                       :style  st/top-gradient}]
     [view st/tabs-container
      (doall (map-indexed #(create-tab %1 %2 selected-view-id) tab-list))]]))
