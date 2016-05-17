(ns syng-im.components.tabs.tabs
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text-input
                                              text
                                              image
                                              touchable-highlight]]
            [reagent.core :as r]
            [syng-im.components.tabs.styles :as st]
            [syng-im.components.tabs.tab :refer [tab]]))

(defn create-tab [index data selected-index]
  (let [data (merge data {:key            index
                          :index          index
                          :selected-index selected-index})]
    [tab data]))

(defview tabs [{:keys [style tab-list selected-index]}]
  (let [style (merge st/tabs style)]
    [view {:style style}
     (doall (map-indexed #(create-tab %1 %2 selected-index) tab-list))]))
