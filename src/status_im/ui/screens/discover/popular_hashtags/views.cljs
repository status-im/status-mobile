(ns status-im.ui.screens.discover.popular-hashtags.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.ui.components.list.views :as list]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn render-tag [tag]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:discover/search-tag-results-view tag])}
   [react/view styles/tag-view
    [react/text {:style styles/tag-title
                 :font  :default}
     (str " #" tag)]]])

(defn tags-menu [tags]
  [react/view styles/tag-title-container
   [list/flat-list {:data                              tags
                    :key-fn                            (fn [_ i] (str i))
                    :render-fn                         render-tag
                    :horizontal                        true
                    :shows-horizontal-scroll-indicator false
                    :default-separator?                false}]])

(defview discover-all-popular-hashtags []
  (letsubs [current-account            [:get-current-account]
            contacts                   [:get-contacts]
            {:keys [discoveries tags]} [:discover/all-popular-hashtags]]
    [react/view styles/all-recent-container
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/popular-tags)]]
     [tags-menu (map name tags)]
     [react/scroll-view styles/list-container
      [react/view styles/status-list-outer
       [react/view styles/status-list-inner
        (let [discoveries (map-indexed vector discoveries)]
          (for [[i {:keys [message-id] :as message}] discoveries]
            ^{:key (str "message-hashtag-" message-id)}
            [components/discover-list-item-full
             {:message         message
              :show-separator? (not= (inc i) (count discoveries))
              :contacts        contacts
              :current-account current-account}]))]]]]))
