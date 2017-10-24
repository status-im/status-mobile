(ns status-im.ui.screens.discover.search-results.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.contacts.styles :as contacts-styles]))

;; TOOD(oskarth): Refactor this, very similar to discover-all-hashtags view
(defview discover-search-results []
  (letsubs [{:keys [discoveries total]} [:get-popular-discoveries 250]
            tags            [:get :discover-search-tags]
            contacts        [:get-contacts]
            current-account [:get-current-account]]
      [react/view styles/discover-tag-container
       [toolbar/toolbar {}
        toolbar/default-nav-back
        [toolbar/content-title (str "#" (first tags) " " total)]]
       (if (empty? discoveries)
         [react/view styles/empty-view
          [vi/icon :icons/group-big {:style contacts-styles/empty-contacts-icon}]
          [react/text {:style contacts-styles/empty-contacts-text}
           (i18n/label :t/no-statuses-found)]]
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
                  :current-account current-account}]))]]])]))
