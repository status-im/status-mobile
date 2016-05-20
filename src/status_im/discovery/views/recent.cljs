(ns status-im.discovery.views.recent
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [subscribe]]
    [status-im.components.react :refer [view list-view list-item]]
    [status-im.utils.listview :refer [to-datasource]]
    [status-im.discovery.styles :as st]
    [status-im.discovery.views.popular-list-item
     :refer [popular-list-item]]))

(defn render-row [row _ _]
  (list-item [popular-list-item row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defview discovery-recent []
  [discoveries [:get :discoveries]]
  [list-view {:dataSource      (to-datasource discoveries)
              :renderRow       render-row
              :renderSeparator render-separator
              :style           st/recent-list}])
