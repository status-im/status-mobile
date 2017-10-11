(ns status-im.ui.screens.discover.popular-hashtags.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.components.react :as react]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.components.toolbar-new.view :as toolbar]))

;; TOOD(oskarth): These styles should be either generic or popular, not recent-*
(defview discover-all-hashtags []
  (letsubs [current-account [:get-current-account]
            popular-tags    [:get-popular-tags 10]
            {:keys [discoveries]} [:get-popular-discoveries 10]] ;uses the tags passed via :discover-search-tags state
    [react/view styles/discover-container
     [toolbar/toolbar2 {}
      toolbar/default-nav-back
      [react/view {} [react/text {} "All hashtags"]]]
     [components/tags-menu (map :name popular-tags)]
     [react/scroll-view styles/list-container
      [react/view styles/recent-container
       [react/view styles/recent-list
        (let [discoveries (map-indexed vector discoveries)]
          (for [[i {:keys [message-id] :as message}] discoveries]
            ^{:key (str "message-hashtag-" message-id)}
            [components/discover-list-item {:message         message
                                            :show-separator? (not= (inc i) (count discoveries))
                                            :current-account current-account}]))]]]]))
