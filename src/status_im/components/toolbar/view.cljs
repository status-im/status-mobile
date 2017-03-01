(ns status-im.components.toolbar.view
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                text-input
                                                image
                                                touchable-highlight]]
            [status-im.components.sync-state.gradient :refer [sync-state-gradient-view]]
            [status-im.components.styles :refer [icon-back
                                                 icon-search]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :as st]
            [status-im.accessibility-ids :as id]
            [status-im.utils.platform :refer [platform-specific]]))

(defn toolbar [{title                :title
                nav-action           :nav-action
                hide-nav?            :hide-nav?
                actions              :actions
                custom-action        :custom-action
                background-color     :background-color
                custom-content       :custom-content
                style                :style}]
  (let [style (merge (st/toolbar-wrapper background-color) style)]
    [view {:style style}
     [view st/toolbar
      [view (st/toolbar-nav-actions-container actions)
       (when-not hide-nav?
         (if nav-action
           [touchable-highlight {:on-press (:handler nav-action)}
            [view (get-in platform-specific [:component-styles :toolbar-nav-action])
             [image (:image nav-action)]]]
           [touchable-highlight {:on-press            #(dispatch [:navigate-back])
                                 :accessibility-label id/toolbar-back-button}
            [view (get-in platform-specific [:component-styles :toolbar-nav-action])
             [image {:source {:uri :icon_back}
                     :style  icon-back}]]]))]
      (or custom-content
          [view {:style st/toolbar-title-container}
           [text {:style st/toolbar-title-text
                  :font  :toolbar-title}
            title]])
      [view (st/toolbar-actions-container (count actions) custom-action)
       (if actions
         (for [{action-image   :image
                action-handler :handler} actions]
           ^{:key (str "action-" action-image)}
           [touchable-highlight {:on-press action-handler}
            [view st/toolbar-action
             [image action-image]]])
         custom-action)]]
     [sync-state-gradient-view]]))

(defn- toolbar-search-submit [on-search-submit]
  (let [text @(subscribe [:get-in [:toolbar-search :text]])]
    (on-search-submit text)
    (dispatch [:set-in [:toolbar-search :text] nil])))

(defn- toolbar-with-search-content [{:keys [show-search?
                                            search-placeholder
                                            title
                                            on-search-submit]}]
  [view st/toolbar-with-search-content
   (if show-search?
     [text-input
      {:style             st/toolbar-search-input
       :auto-focus        true
       :placeholder       search-placeholder
       :return-key-type   "search"
       :on-blur           #(dispatch [:set-in [:toolbar-search :show] nil])
       :on-change-text    #(dispatch [:set-in [:toolbar-search :text] %])
       :on-submit-editing #(toolbar-search-submit on-search-submit)}]
     [view
      [text {:style st/toolbar-with-search-title
             :font  :toolbar-title}
       title]])])

(defn toolbar-with-search [{:keys [show-search?
                                   search-key
                                   nav-action
                                   actions
                                   style
                                   on-search-submit]
                            :as   opts}]
  (let [toggle-search-fn #(dispatch [:set-in [:toolbar-search :show] %])
        actions          (if-not show-search?
                           (into actions [(act/search #(toggle-search-fn search-key))]))]
    [toolbar {:style          (merge st/toolbar-with-search style)
              :nav-action     (if show-search?
                                (act/back #(toggle-search-fn nil))
                                nav-action)
              :custom-content [toolbar-with-search-content opts]
              :actions        actions}]))
