(ns status-im.ui.screens.discover.recent-statuses.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]))

(defview discover-all-recent []
  (letsubs [discoveries     [:discover/recent-discoveries]
            tabs-hidden?    [:tabs-hidden?]
            current-account [:get-current-account]
            contacts        [:get-contacts]]
    [react/view styles/all-recent-container
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/recent)]]
     (when (seq discoveries)
       [react/scroll-view (styles/list-container tabs-hidden?)
        [react/view styles/status-list-outer
         [react/view styles/status-list-inner
          (let [discoveries (map-indexed vector discoveries)]
            (for [[i {:keys [message-id] :as message}] discoveries]
              ^{:key (str "message-recent-" message-id)}
              [components/discover-list-item-full
               {:message         message
                :show-separator? (not= (inc i) (count discoveries))
                :contacts        contacts
                :current-account current-account}]))]]])]))
