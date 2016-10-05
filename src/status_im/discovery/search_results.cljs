(ns status-im.discovery.search-results
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.components.react :refer [view text list-view list-item]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.discovery.views.discovery-list-item :refer [discovery-list-item]]
            [status-im.discovery.styles :as st]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn title-content [tags]
  [view st/tag-title-container
   (for [tag (take 3 tags)]
     ^{:key (str "tag-" tag)}
     [view {:style st/tag-container}
      [text {:style st/tag-title
             :font  :default}
       (str " #" tag)]])])

(defview discovery-search-results []
  [discoveries [:get-discovery-search-results]
   tags [:get :discovery-search-tags]]
  (let [datasource (to-datasource discoveries)]
    [view st/discovery-tag-container
     [toolbar {:nav-action     {:image   {:source {:uri :icon_back}
                                          :style  st/icon-back}
                                :handler #(dispatch [:navigate-back])}
               :custom-content (title-content tags)
               :actions        [{:image   {:source {:uri :icon_search}
                                           :style  st/icon-search}
                                 :handler (fn [])}]}]

     [list-view {:dataSource      datasource
                 :renderRow       (fn [row _ _]
                                    (list-item [discovery-list-item row]))
                 :renderSeparator render-separator
                 :style           st/recent-list}]]))
