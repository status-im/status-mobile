(ns status-im.components.toolbar-new.view
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :as rn]
            [status-im.components.sync-state.gradient :refer [sync-state-gradient-view]]
            [status-im.components.styles :as st]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.styles :as tst]
            [status-im.components.icons.vector-icons :as vi]
            [reagent.core :as r]))

(defn vec-icon [icon]
  ((comp vec flatten vector) vi/icon icon))

(defn nav-button
  [{:keys [handler accessibility-label image icon]}]
  [rn/touchable-highlight
   (merge {:style    tst/toolbar-button
           :on-press handler}
          (when accessibility-label
            {:accessibility-label accessibility-label}))
   [rn/view
    (if icon
      [vec-icon icon]
      [rn/image image])]])

(defn text-action [handler title]
  [rn/text {:style tst/toolbar-right-action :onPress handler}
   title])

(defn toolbar [{:keys [title
                       nav-action
                       hide-nav?
                       actions
                       custom-action
                       background-color
                       custom-content
                       hide-border?
                       border-style
                       title-style
                       style]}]
  (let [style (merge (tst/toolbar-wrapper background-color) style)]
    [rn/view {:style style}
     [rn/view tst/toolbar
      (when-not hide-nav?
        [rn/view (tst/toolbar-nav-actions-container actions)
         [nav-button (or nav-action act/default-back)]])
      (or custom-content
          [rn/view {:style tst/toolbar-title-container}
           [rn/text {:style (merge tst/toolbar-title-text title-style)
                     :font  :toolbar-title}
            title]])
      [rn/view (tst/toolbar-actions-container (count actions) custom-action)
       (if actions
         (for [{:keys [image icon options handler]} actions]
           (with-meta
             (cond (= image :blank)
                   [rn/view tst/toolbar-action]

                   options
                   [context-menu
                    [rn/view tst/toolbar-action [vec-icon icon]]
                    options
                    nil
                    tst/toolbar-button]

                   :else
                   [rn/touchable-highlight {:style    tst/toolbar-button
                                            :on-press handler}
                    [rn/view tst/toolbar-action
                     (if icon
                       [vec-icon icon]
                       [rn/image image])]])
             {:key (str "action-" (or icon image))}))
         custom-action)]]
     [sync-state-gradient-view]
     (when-not hide-border?
       [rn/view (merge tst/toolbar-border-container border-style)
        [rn/view tst/toolbar-border]])]))

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
  [rn/view tst/toolbar-with-search-content
   (if show-search?
     [rn/text-input
      {:style                  tst/toolbar-search-input
       :ref                    #(reset! search-text-input %)
       :auto-focus             true
       :placeholder            search-placeholder
       :placeholder-text-color st/color-gray4
       :on-change-text         #(dispatch [:set-in [:toolbar-search :text] %])
       :on-submit-editing      (when on-search-submit
                                 #(toolbar-search-submit on-search-submit))}]
     (or custom-title
         [rn/view
          [rn/text {:style tst/toolbar-title-text
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
                             [act/search-icon])
                           (into [(act/search #(toggle-search-fn search-key))] actions))]
    [toolbar {:style          style
              :nav-action     (if show-search?
                                (act/back #(toggle-search-fn nil))
                                nav-action)
              :custom-content [toolbar-with-search-content opts]
              :actions        actions}]))
