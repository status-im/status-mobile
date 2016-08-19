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

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defview discovery-popular-list [{:keys [tag count contacts platform-specific]}]
  [discoveries [:get-discoveries-by-tags [tag] 3]]
  [view st/popular-list-container
   [view st/row
    [view st/tag-name-container
     [touchable-highlight {:onPress #(dispatch [:show-discovery-tag tag])}
      [view
       [text {:style             st/tag-name
              :platform-specific platform-specific
              :font              :medium}
        (str " #" (name tag))]]]]
    [view st/tag-count-container
     [text {:style             st/tag-count
            :platform-specific platform-specific
            :font              :default}
      count]]]
   (for [{:keys [msg-id] :as discovery} discoveries]
     ^{:key (str "message-" msg-id)}
     [discovery-list-item discovery platform-specific])])
