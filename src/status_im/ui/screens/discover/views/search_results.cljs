(ns status-im.ui.screens.discover.views.search-results
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item
                                                scroll-view]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.ui.screens.discover.views.discover-list-item :refer [discover-list-item]]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.discover.styles :as st]
            [status-im.ui.screens.contacts.styles :as contacts-st]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defn title-content [tags]
  [scroll-view {:horizontal            true
                :bounces               false
                :flex                  1
                :contentContainerStyle st/tag-title-scroll}
   [view st/tag-title-container
    (for [tag (take 3 tags)]
      ^{:key (str "tag-" tag)}
      [view (merge (get-in platform-specific [:component-styles :discover :tag])
                   {:margin-left 2 :margin-right 2})
       [text {:style st/tag-title
              :font  :default}
        (str " #" tag)]])]])

(defview discover-search-results []
  (letsubs [discoveries [:get-popular-discoveries 250]
            tags [:get :discover-search-tags]
            current-account [:get-current-account]]
    (let [discoveries (:discoveries discoveries)
          datasource (to-datasource discoveries)]
      [view st/discover-tag-container
       [status-bar]
       [toolbar {:nav-action     (act/back #(dispatch [:navigate-back]))
                 :custom-content (title-content tags)
                 :style          st/discover-tag-toolbar}]
       (if (empty? discoveries)
         [view st/empty-view
          ;; todo change icon
          [vi/icon :icons/group_big {:style contacts-st/empty-contacts-icon}]
          [text {:style contacts-st/empty-contacts-text}
           (label :t/no-statuses-found)]]
         [list-view {:dataSource      datasource
                     :renderRow       (fn [row _ _]
                                        (list-item [discover-list-item
                                                    {:message         row
                                                     :current-account current-account}]))
                     :renderSeparator render-separator
                     :style           st/recent-list}])])))
