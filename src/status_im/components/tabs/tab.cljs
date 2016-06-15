(ns status-im.components.tabs.tab
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text-input
                                                text
                                                image
                                                touchable-highlight]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]))

(defview tab [{:keys [view-id title icon selected-view-id]}]
  [touchable-highlight {:style    st/tab
                        :disabled (= view-id selected-view-id)
                        :onPress  #(dispatch [:navigate-to-tab view-id])}
   [view {:style st/tab-container}
    [image {:source {:uri icon}
            :style  st/tab-icon}]
    (when (= selected-view-id view-id)
      [text {:style st/tab-title} title])]])
