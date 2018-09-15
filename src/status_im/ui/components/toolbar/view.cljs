(ns status-im.ui.components.toolbar.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.utils.core :as utils]
            [status-im.ui.components.common.common :as components.common]))

;; Navigation item

(defn nav-item
  [{:keys [handler accessibility-label style] :or {handler #(re-frame/dispatch [:navigate-back])}} item]
  [react/touchable-highlight
   (merge {:on-press handler}
          (when accessibility-label
            {:accessibility-label accessibility-label}))
   [react/view {:style style}
    item]])

(defn nav-button
  [{:keys [icon icon-opts unread-messages?] :as props}]
  [nav-item (merge {:style (styles/nav-item-button unread-messages?)} props)
   [vector-icons/icon icon (if unread-messages?
                             (assoc icon-opts :color :active)
                             icon-opts)]])

(defview nav-button-with-count [props]
  (letsubs [unread-messages-number [:get-chats-unread-messages-number]]
    (let [unread-messages? (pos? unread-messages-number)]
      [react/view {:flex-direction :row}
       [nav-button (assoc props :unread-messages? unread-messages?)]
       (when unread-messages?
         [nav-item (merge {:style styles/counter-container} props)
          [components.common/counter unread-messages-number]])])))

(defn nav-text
  ([text] (nav-text nil text))
  ([{:keys [handler] :as props} text]
   [react/text (utils/deep-merge {:style    (merge styles/item styles/item-text)
                                  :on-press (or handler #(re-frame/dispatch [:navigate-back]))}
                                 props)
    text]))

(defn nav-clear-text
  ([text] (nav-clear-text nil text))
  ([props text]
   (nav-text (merge props styles/item-text-white-background) text)))

(def default-nav-back [nav-button actions/default-back])

(defn nav-back-count
  ([]
   [nav-button-with-count actions/default-back])
  ([{:keys [home?]}]
   [nav-button-with-count (if home? actions/home-back actions/default-back)]))

(defn default-done
  "Renders a touchable icon on Android or a label or iOS."
  [{:keys [icon] :as props}]
  (if platform/ios?
    [react/view
     [nav-text props
      (i18n/label :t/done)]]
    [react/view
     [nav-button (merge props {:icon (or icon :icons/close)})]]))

;; Content

(defn content-wrapper [content]
  [react/view {:style styles/toolbar-container}
   content])

(defn content-title
  ([title] (content-title nil title))
  ([title-style title]
   (content-title title-style title nil nil))
  ([title-style title subtitle-style subtitle]
   (content-title title-style title subtitle-style subtitle nil))
  ([title-style title subtitle-style subtitle additional-text-props]
   [react/view {:style styles/toolbar-title-container}
    [react/text (merge {:style (merge styles/toolbar-title-text title-style)
                        :font :toolbar-title :numberOfLines 1 :ellipsizeMode :tail}
                       additional-text-props) title]
    (when subtitle
      [react/text {:style subtitle-style}
       subtitle])]))

;; Actions

(defn text-action [{:keys [style handler disabled? accessibility-label]} title]
  [react/text (cond-> {:style      (merge styles/item styles/item-text style
                                          (when disabled? styles/toolbar-text-action-disabled))
                       :on-press   (when-not disabled? handler)
                       :uppercase? true}
                accessibility-label
                (assoc :accessibility-label accessibility-label))
   title])

(def blank-action [react/view {:style (merge styles/item styles/toolbar-action)}])

(defn- icon-action [icon {:keys [overlay-style] :as icon-opts} handler]
  [react/touchable-highlight {:on-press handler}
   [react/view {:style (merge styles/item styles/toolbar-action)}
    (when overlay-style
      [react/view overlay-style])
    [vector-icons/icon icon (merge {:container-style styles/action-default} icon-opts)]]])

(defn- option-actions [icon icon-opts options]
  [icon-action icon icon-opts
   #(list-selection/show {:options options})])

(defn actions [v]
  [react/view {:style styles/toolbar-actions}
   (for [{:keys [image icon icon-opts options handler]} v]
     (with-meta
       (cond (= image :blank)
             blank-action

             options
             [option-actions icon icon-opts options]

             :else
             [icon-action icon icon-opts handler])
       {:key (str "action-" (or image icon))}))])

(defn toolbar
  ([props nav-item content-item] (toolbar props nav-item content-item nil))
  ([{:keys [background-color style flat?]}
    nav-item
    content-item
    action-items]
   [react/view {:style (merge (styles/toolbar background-color flat?) style)}
    [react/view styles/ios-content-item
     content-item]
    (when nav-item
      [react/view {:style styles/toolbar-nav-actions-container}
       nav-item])
    [react/view components.styles/flex]
    action-items]))

(defn platform-agnostic-toolbar
  ([props nav-item content-item] (platform-agnostic-toolbar props nav-item content-item [actions [{:image :blank}]]))
  ([{:keys [background-color style flat?]}
    nav-item
    content-item
    action-items]
   [react/view {:style (merge (styles/toolbar background-color flat?) style)}
    (when nav-item
      [react/view {:style (styles/toolbar-nav-actions-container 0)}
       nav-item])
    content-item
    action-items]))

(defn simple-toolbar
  "A simple toolbar composed of a nav-back item and a single line title."
  ([] (simple-toolbar nil))
  ([title] (toolbar nil default-nav-back [content-title title])))
