(ns status-im.discovery.views.popular-list
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [status-im.components.react :refer [view
                                      list-view
                                      list-item
                                      touchable-highlight
                                      text]]
    [status-im.discovery.styles :as st]
    [status-im.utils.listview :refer [to-datasource]]
    [status-im.discovery.views.discovery-list-item :refer [discovery-list-item]]))

(defview discovery-popular-list [{:keys [tag count contacts]}]
  [discoveries [:get-discoveries-by-tags [tag] 3]]
  [view st/popular-list-container
   [view st/row
    [view st/tag-name-container
     [touchable-highlight {:onPress #(dispatch [:show-discovery-tag tag])}
      [view
       [text {:style st/tag-name
              :font  :medium}
        (str " #" (name tag))]]]]
    [view st/tag-count-container
     [text {:style st/tag-count
            :font  :default}
      count]]]
   (for [{:keys [message-id] :as discovery} discoveries]
     ^{:key (str "message-" message-id)}
     [discovery-list-item discovery])])
