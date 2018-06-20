(ns status-im.ui.screens.wallet.collectibles.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]))

(defmulti load-collectible-fx (fn [symbol _] symbol))

(defmethod load-collectible-fx :default [_ _] nil)

(defmulti load-collectibles-fx (fn [_ symbol _ _] symbol))

(defmethod load-collectibles-fx :default [web3 symbol i address]
  {:load-collectibles [web3 symbol i address]})

(defmulti render-collectible (fn [symbol _] symbol))

(defmethod render-collectible :default [symbol {:keys [id name]}]
  [react/view {:style styles/default-collectible}
   [react/text (str (clojure.core/name symbol) " #" (or id name))]])

(defview display-collectible []
  (letsubs [{:keys [name symbol]} [:get-screen-params]]
    (let [collectibles @(re-frame/subscribe [:collectibles symbol])]
      [react/view {:style component.styles/flex}
       (if (seq collectibles)
         [react/view {:style component.styles/flex}
          [status-bar/status-bar]
          [toolbar/toolbar {}
           toolbar/default-nav-back
           [toolbar/content-title name]]
          [list/flat-list {:data      collectibles
                           :key-fn    (comp str :id)
                           :render-fn #(render-collectible symbol %)}]]
         [react/view {:style styles/loading-indicator}
          [react/activity-indicator {:animating true :size :large :color colors/blue}]])])))
