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
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]))

;; Navigation item

(defn nav-item
  [{:keys [handler accessibility-label style] :or {handler #(re-frame/dispatch [:navigate-back])}} item]
  [react/touchable-highlight
   (merge {:on-press handler
           :style styles/touchable-area}
          (when accessibility-label
            {:accessibility-label accessibility-label}))
   [react/view {:style style}
    item]])

(defn nav-button
  [{:keys [icon icon-opts] :as props}]
  [nav-item props
   [vector-icons/icon icon icon-opts]])

(defn nav-text
  ([text] (nav-text nil text))
  ([{:keys [handler] :as props} text]
   [react/text (utils/deep-merge {:style    styles/item-text
                                  :on-press (or handler #(re-frame/dispatch [:navigate-back]))}
                                 props)
    text]))

(defn nav-clear-text
  ([text] (nav-clear-text nil text))
  ([props text]
   (nav-text (merge props styles/item-text-white-background) text)))

(def nav-back-home [nav-button actions/home-back])
(def default-nav-back [nav-button actions/default-back])
(def default-nav-close [nav-button actions/default-close])

;; Content

(defn content-wrapper [content]
  [react/view {:style {:flex 1}}
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
                        :numberOfLines 1
                        :ellipsizeMode :tail}
                       additional-text-props) title]
    (when subtitle
      [react/text {:style subtitle-style}
       subtitle])]))

;; Actions

(defn text-action [{:keys [style handler disabled? accessibility-label]} title]
  [react/touchable-highlight {:on-press (when-not disabled?
                                          handler)
                              :style styles/touchable-area}
   [react/text (cond-> {:style (merge styles/item-text
                                      style
                                      (when disabled?
                                        styles/toolbar-text-action-disabled))}
                 accessibility-label
                 (assoc :accessibility-label accessibility-label))
    title]])

(def blank-action [react/view {:style {:flex 1}}])

(defn- icon-action [icon {:keys [overlay-style] :as icon-opts} handler]
  [react/touchable-highlight {:on-press handler
                              :style styles/touchable-area}
   [react/view
    (when overlay-style
      [react/view overlay-style])
    [vector-icons/icon icon icon-opts]]])

(defn- option-actions [icon icon-opts options]
  [icon-action icon icon-opts
   #(list-selection/show {:options options})])

(defn actions [v]
  [react/view {:style {:flex-direction :row}}
   (for [{:keys [image icon icon-opts options handler]} v]
     (with-meta
       (cond (= image :blank)
             blank-action

             options
             [option-actions icon icon-opts options]

             :else
             [icon-action icon icon-opts handler])
       {:key (str "action-" (or image icon))}))])

;;TODO remove
(defn toolbar
  ([props nav-item content-item] (toolbar props nav-item content-item nil))
  ([{:keys [style border-bottom-color transparent? browser? chat?]}
    nav-item
    content-item
    action-items]
   [react/view {:style (cond-> {:height styles/toolbar-height}
                         ;; i.e. for qr code scanner
                         (not transparent?)
                         (assoc :border-bottom-color (or border-bottom-color
                                                         colors/gray-lighter)
                                :border-bottom-width 1)
                         transparent?
                         (assoc :background-color :transparent
                                :z-index          1)
                         :always
                         (merge style))}
    (when content-item
      (cond
        browser?
        content-item

        chat?
        [react/view
         {:position :absolute
          :right    56
          :left     56
          :top      10}
         content-item]

        :else
        [react/view {:position        :absolute
                     :left            56
                     :right           56
                     :height          styles/toolbar-height
                     :justify-content :center
                     :align-items     :center}
         content-item]))
    (when nav-item
      [react/view {:style {:position        :absolute
                           :left            0
                           :height          styles/toolbar-height
                           :justify-content :center
                           :align-items     :center}}
       nav-item])
    [react/view {:position        :absolute
                 :right           0
                 :height          styles/toolbar-height
                 :justify-content :center
                 :align-items     :center}
     action-items]]))

;;TODO remove
(defn simple-toolbar
  "A simple toolbar composed of a nav-back item and a single line title."
  ([] (simple-toolbar nil))
  ([title] (simple-toolbar title false))
  ([title modal?] (toolbar nil (if modal? default-nav-close default-nav-back) [content-title title])))
