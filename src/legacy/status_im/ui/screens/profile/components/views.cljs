(ns legacy.status-im.ui.screens.profile.components.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.profile.components.styles :as styles]))

;; settings items elements

(defn settings-switch-item
  [{:keys [label-kw value action-fn active?]
    :or   {active? true}}]
  [react/view styles/settings-item
   [react/view styles/settings-item-text-wrapper
    [react/i18n-text {:style styles/settings-item-text :key label-kw}]]
   [react/switch
    {:track-color     #js {:true colors/blue :false colors/gray-lighter}
     :value           (boolean value)
     :on-value-change action-fn
     :disabled        (not active?)}]])
