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
    [status-im.discovery.views.discovery-list-item :refer [discovery-list-item]]
    [status-im.utils.platform :refer [platform-specific]]))

(defview discovery-popular-list [{:keys [tag contacts]}]
  [discoveries [:get-popular-discoveries 3 [tag]]]
  [view (merge st/popular-list-container
               (get-in platform-specific [:component-styles :discovery :popular]))
   [view st/row
    [view (get-in platform-specific [:component-styles :discovery :tag])
     [touchable-highlight {:on-press #(do (dispatch [:set :discovery-search-tags [tag]])
                                          (dispatch [:navigate-to :discovery-search-results]))}
      [view
       [text {:style st/tag-name
              :font  :medium}
        (str " #" (name tag))]]]]
    [view st/tag-count-container
     [text {:style st/tag-count
            :font  :default}
      (:total discoveries)]]]
   (let [discoveries (map-indexed vector (:discoveries discoveries))]
     (for [[i {:keys [message-id] :as discovery}] discoveries]
       ^{:key (str "message-" message-id)}
       [discovery-list-item discovery (not= (inc i) (count discoveries))]))])
