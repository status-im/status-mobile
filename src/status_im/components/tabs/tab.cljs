(ns status-im.components.tabs.tab
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.utils.platform :as p]
            [status-im.components.tabs.styles :as st]))

(defn tab [{:keys [view-id title icon-active icon-inactive selected-view-id prev-view-id]}]
  (let [active?   (= view-id selected-view-id)
        previous? (= view-id prev-view-id)]
    [touchable-highlight {:style    st/tab
                          :disabled active?
                          :onPress  #(dispatch [:navigate-to-tab view-id])}
     [view {:style st/tab-container}
      [view
       [image {:source {:uri (if active? icon-active icon-inactive)}
               :style  st/tab-icon}]]
      [view
       [text {:style (st/tab-title active?)
              :font  (if (and p/ios? active?) :medium :regular)}
        title]]]]))
