(ns legacy.status-im.ui.screens.profile.components.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.profile.components.styles :as styles]
    [utils.i18n :as i18n]))

;; settings items elements

(defn settings-item
  [{:keys [item-text label-kw value action-fn active? destructive? hide-arrow?
           accessibility-label icon icon-content]
    :or   {value "" active? true}}]
  [react/touchable-highlight
   (cond-> {:on-press action-fn
            :disabled (not active?)}
     accessibility-label
     (assoc :accessibility-label accessibility-label))
   [react/view styles/settings-item
    (when icon
      [react/view styles/settings-item-icon
       [icons/icon icon {:color colors/blue}]])
    [react/view styles/settings-item-text-wrapper
     [react/text
      {:style           (merge styles/settings-item-text
                               (when destructive?
                                 styles/settings-item-destructive)
                               (when-not active?
                                 styles/settings-item-disabled)
                               (when icon
                                 {:font-size 17}))
       :number-of-lines 1}
      (or item-text (i18n/label label-kw))]
     (when-not (string/blank? value)
       [react/text
        {:style           styles/settings-item-value
         :number-of-lines 1}
        value])]
    (if icon-content
      icon-content
      (when (and active? (not hide-arrow?))
        [icons/icon :main-icons/next {:color colors/gray-transparent-40}]))]])

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
