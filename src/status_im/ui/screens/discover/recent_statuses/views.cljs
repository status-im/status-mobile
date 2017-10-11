(ns status-im.ui.screens.discover.recent-statuses.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.components.toolbar-new.view :as toolbar]))

(defview discover-all-recent []
  (letsubs [discoveries     [:get-recent-discoveries]
            tabs-hidden?    [:tabs-hidden?]
            current-account [:get-current-account]]
    [react/view styles/discover-container
     [toolbar/toolbar2 (i18n/label :t/recent)]
     (when (seq discoveries)
       [react/scroll-view styles/list-container
        [react/view styles/recent-container
         [react/view styles/recent-list
          (let [discoveries (map-indexed vector discoveries)]
            (for [[i {:keys [message-id] :as message}] discoveries]
              ^{:key (str "message-recent-" message-id)}
              [components/discover-list-item {:message         message
                                              :show-separator? (not= (inc i) (count discoveries))
                                              :current-account current-account}]))]]])]))
