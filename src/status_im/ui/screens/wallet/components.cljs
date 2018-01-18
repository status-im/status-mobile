(ns status-im.ui.screens.wallet.components
  "
  Higher-level components used in the wallet screens.
  "
  (:require [status-im.utils.utils :as utils]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.styles :as styles]))

;; Wallet tab has a different coloring scheme (dark) that forces color changes (background, text)
;; It might be replaced by some theme mechanism

(defn text-input [props text]
  [react/text-input (utils/deep-merge {:placeholder-text-color colors/white-lighter-transparent
                                       :selection-color        colors/white
                                       :style                  {:color          colors/white
                                                                :font-size      15
                                                                :height         52
                                                                :letter-spacing -0.2}}
                                      props)
   text])

(defn- toolbar [action title]
  [toolbar/toolbar {:style styles/toolbar}
   [toolbar/nav-button action]
   [toolbar/content-title {:color :white}
    title]])

(defn simple-screen [title content]
  [react/view {:flex 1 :background-color colors/blue}
   [status-bar/status-bar {:type :wallet}]
   [toolbar (actions/back-white actions/default-handler)
    title]
   content])

(defn- cartouche-content [{:keys [disabled?]} content]
  [react/view {:style (styles/cartouche-content-wrapper disabled?)}
   [react/view {:flex 1}
    content]])

(defn cartouche [{:keys [disabled? on-press icon] :or {icon :icons/forward} :as m} header content]
  [react/view {:style styles/cartouche-container}
   [react/text {:style styles/cartouche-header}
    header]
   (if (or disabled? (nil? on-press))
     [cartouche-content m content]
     [react/touchable-highlight {:on-press on-press}
      [react/view
       [cartouche-content m
        (if-not (true? disabled?)
          [react/view styles/cartouche-icon-wrapper
           [react/view {:flex 1} ;; Let content shrink if needed
            content]
           [vector-icons/icon icon {:color :white}]]
          content)]]])])

(defn- cartouche-primary-text [s]
  [react/text {:style styles/cartouche-primary-text}
   s])

(defn- cartouche-secondary-text [s]
  [react/text {:style styles/cartouche-secondary-text}
   s])

(defn cartouche-text-content [primary secondary]
  [react/view {:flex-direction     :row
               :justify-content    :space-between
               :padding-horizontal 15
               :padding-vertical   15}
   [cartouche-primary-text primary]
   [cartouche-secondary-text secondary]])
