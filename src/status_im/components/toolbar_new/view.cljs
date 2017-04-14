(ns status-im.components.toolbar-new.view
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                text-input
                                                image
                                                touchable-highlight]]
            [status-im.components.sync-state.gradient :refer [sync-state-gradient-view]]
            [status-im.components.styles :refer [icon-default
                                                 icon-search
                                                 color-gray4]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :as st]
            [status-im.accessibility-ids :as id]
            [status-im.utils.platform :refer [platform-specific]]
            [reagent.core :as r]))

(defn toolbar [{title                :title
                nav-action           :nav-action
                hide-nav?            :hide-nav?
                actions              :actions
                custom-action        :custom-action
                background-color     :background-color
                custom-content       :custom-content
                hide-border?         :hide-border?
                border-style         :border-style
                title-style          :title-style
                style                :style}]
  (let [style (merge (st/toolbar-wrapper background-color) style)]
    [view {:style style}
     [view st/toolbar
      [view (st/toolbar-nav-actions-container actions)
       (when-not hide-nav?
         (if nav-action
           [touchable-highlight {:on-press (:handler nav-action)}
            [view
             [image (:image nav-action)]]]
           [touchable-highlight {:on-press            #(dispatch [:navigate-back])
                                 :accessibility-label id/toolbar-back-button}
            [view
             [image {:source {:uri :icon_back_dark}
                     :style  icon-default}]]]))]
      (or custom-content
          [view {:style st/toolbar-title-container}
           [text {:style (merge st/toolbar-title-text title-style)
                  :font  :toolbar-title}
            title]])
      [view (st/toolbar-actions-container (count actions) custom-action)
       (if actions
         (for [{action-image   :image
                action-options :options
                action-handler :handler} actions]
           (with-meta
             (cond (= action-image :blank)
                   [view st/toolbar-action]
                   action-options
                   [context-menu
                    [view st/toolbar-action
                     [image action-image]]
                    action-options]
                   :else
                   [touchable-highlight {:on-press action-handler}
                    [view st/toolbar-action
                     [image action-image]]])
             {:key (str "action-" action-image)}))
         custom-action)]]
     [sync-state-gradient-view]
     (when-not hide-border?
       [view (merge st/toolbar-border-container border-style)
        [view st/toolbar-border]])]))

(def search-text-input (r/atom nil))

(defn- toolbar-search-submit [on-search-submit]
  (let [text @(subscribe [:get-in [:toolbar-search :text]])]
    (on-search-submit text)
    (dispatch [:set-in [:toolbar-search :text] nil])))

(defn- toolbar-with-search-content [{:keys [show-search?
                                            search-placeholder
                                            title
                                            custom-title
                                            on-search-submit]}]
  [view st/toolbar-with-search-content
   (if show-search?
     [text-input
      {:style                  st/toolbar-search-input
       :ref                    #(reset! search-text-input %)
       :auto-focus             true
       :placeholder            search-placeholder
       :placeholder-text-color color-gray4
       :on-change-text         #(dispatch [:set-in [:toolbar-search :text] %])
       :on-submit-editing      (when on-search-submit
                                 #(toolbar-search-submit on-search-submit))}]
     (or custom-title
         [view
          [text {:style st/toolbar-title-text
                 :font  :toolbar-title}
           title]]))])

(defn toolbar-with-search [{:keys [show-search?
                                   search-text
                                   search-key
                                   nav-action
                                   actions
                                   style
                                   on-search-submit]
                            :as   opts}]
  (let [toggle-search-fn #(do
                            (dispatch [:set-in [:toolbar-search :show] %])
                            (dispatch [:set-in [:toolbar-search :text] ""]))
        actions          (if show-search?
                           (if (pos? (count search-text))
                             [(act/close #(do
                                            (.clear @search-text-input)
                                            (dispatch [:set-in [:toolbar-search :text] ""])))]
                             [(act/search-icon)])
                           (into [(act/search #(toggle-search-fn search-key))] actions))]
    [toolbar {:style          style
              :nav-action     (if show-search?
                                (act/back #(toggle-search-fn nil))
                                nav-action)
              :custom-content [toolbar-with-search-content opts]
              :actions        actions}]))
