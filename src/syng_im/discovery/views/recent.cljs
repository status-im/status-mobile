(ns syng-im.discovery.views.recent
  (:require
    [re-frame.core :refer [subscribe]]
    [syng-im.components.react :refer [view list-view list-item]]
    [syng-im.utils.listview :refer [to-datasource]]
    [syng-im.discovery.styles :as st]
    [syng-im.discovery.views.popular-list-item
     :refer [popular-list-item]]))

(defn render-row [row _ _]
  (list-item [popular-list-item row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn discovery-recent []
  (let [discoveries (subscribe [:get :discoveries])]
    (fn []
      ;; todo fetch more on :onEndReached
      [list-view {:dataSource      (to-datasource @discoveries)
                  :renderRow       render-row
                  :renderSeparator render-separator
                  :style           st/recent-list}])))
