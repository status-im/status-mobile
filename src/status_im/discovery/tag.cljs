(ns status-im.discovery.tag
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [status-im.utils.logging :as log]
    [status-im.utils.listview :refer [to-datasource]]
    [status-im.components.react :refer [view text list-view list-item]]
    [status-im.components.toolbar :refer [toolbar]]
    [status-im.discovery.views.popular-list-item :refer [popular-list-item]]
    [status-im.discovery.styles :as st]))

(defn render-row [row _ _]
  (list-item [popular-list-item row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn title-content [tag]
  [view st/tag-title-container
   [view {:style st/tag-container}
    [text {:style st/tag-title} (str " #" tag)]]])

(defn discovery-tag []
  (let [tag         (subscribe [:get :current-tag])
        discoveries (subscribe [:get-discoveries-by-tag])]
    (log/debug "Got discoveries: " @discoveries)
    (fn []
      (let [items      @discoveries
            datasource (to-datasource items)]
        [view st/discovery-tag-container
         [toolbar {:nav-action {:image   {:source {:uri :icon_back}
                                          :style  st/icon-back}
                                :handler #(dispatch [:navigate-back])}
                   :title      "Add Participants"
                   :custom-content    (title-content @tag)
                   :action     {:image   {:source {:uri :icon_search}
                                          :style  st/icon-search}
                                :handler (fn [])}}]

         [list-view {:dataSource      datasource
                     :renderRow       render-row
                     :renderSeparator render-separator
                     :style           st/recent-list}]]))))
