(ns status-im.ui.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(views/defview ^:no-theme with-activity-indicator
  [{:keys [timeout style enabled? preview]} comp]
  (views/letsubs
    [loading (reagent/atom true)]
    {:component-did-mount (fn []
                            (if (or (nil? timeout)
                                    (> 100 timeout))
                              (reset! loading false)
                              (utils/set-timeout #(reset! loading false)
                                                 timeout)))}
    (if (and (not enabled?) @loading)
      (or preview
          [react/view {:style (or style {:justify-content :center
                                         :align-items     :center})}
           [react/activity-indicator {:animating true}]])
      comp)))

(defn with-empty-preview [comp]
  [with-activity-indicator
   {:preview [react/view {}]}
   comp])

(views/defview ^:no-theme modal [view-id & components]
  (views/letsubs [signing? [:get-in [:wallet :send-transaction :signing?]]]
    [react/view styles/flex
     (apply vector react/view styles/flex components)
     (when (and platform/iphone-x? (not signing?))
       [react/view {:flex             0
                    :height           34
                    :background-color (if (#{:wallet-send-transaction-modal
                                             :wallet-transaction-sent-modal
                                             :wallet-transaction-fee} view-id)
                                        colors/blue
                                        colors/white)}])]))
