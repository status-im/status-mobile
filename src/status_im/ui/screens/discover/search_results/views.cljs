(ns status-im.ui.screens.discover.search-results.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.utils.listview :refer [to-datasource]]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.contacts.styles :as contacts-styles]
            [status-im.components.toolbar-new.view :as toolbar]))

(defn render-separator [_ row-id _]
  (react/list-item [react/view {:style styles/row-separator
                                :key   row-id}]))


(defview discover-search-results []
  (letsubs [{:keys [discoveries total]} [:get-popular-discoveries 250]
            tags            [:get :discover-search-tags]
            current-account [:get-current-account]]
    (let [datasource (to-datasource discoveries)]
      [react/view styles/discover-tag-container
       [status-bar/status-bar]
       [toolbar/toolbar2 {}
        toolbar/default-nav-back
        [react/view {:flex-direction  :row
                     :justify-content :flex-start}
         [react/text {} (str "#" (first tags) " " total)]]]
       (if (empty? discoveries)
         [react/view styles/empty-view
          [vi/icon :icons/group-big {:style contacts-styles/empty-contacts-icon}]
          [react/text {:style contacts-styles/empty-contacts-text}
           (i18n/label :t/no-statuses-found)]]
         ;TODO (goranjovic) replace this with status-im.components.list.views
         ;as per https://github.com/status-im/status-react/issues/1840
         [react/list-view {:dataSource      datasource
                           :renderRow       (fn [row _ _]
                                              (react/list-item [components/discover-list-item
                                                                {:message         row
                                                                 :current-account current-account}]))
                           :renderSeparator render-separator
                           :style           styles/recent-list}])])))
