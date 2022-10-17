(ns status-im.ui.components.topnav
  (:require [quo2.components.buttons.button :as quo2.button]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.utils.config :as config]
            [status-im.utils.handlers :refer [<sub]]))

(defn qr-scanner []
  [quo2.button/button
   {:icon                true
    :size                32
    :type                :grey
    :style               {:margin-left 12}
    :accessibility-label :scan-qr-code-button
    :on-press #(re-frame/dispatch [::qr-scanner/scan-code
                                   {:title   (i18n/label :t/add-bootnode)
                                    :handler :bootnodes.callback/qr-code-scanned}])}
   :main-icons2/scanner])

(defn qr-code []
  [quo2.button/button
   {:icon                true
    :type                :grey
    :size                32
    :style               {:margin-left 12}
    :accessibility-label :contact-qr-code-button}
   :main-icons2/qr-code])

(defn notifications-button []
  (let [notif-count (<sub [:activity.center/notifications-count])]
    [:<>
     [quo2.button/button {:icon                true
                          :type                :grey
                          :size                32
                          :style               {:margin-left 12}
                          :accessibility-label :notifications-button
                          :on-press #(do
                                       (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                       (if config/new-activity-center-enabled?
                                         (re-frame/dispatch [:navigate-to :activity-center])
                                         (re-frame/dispatch [:navigate-to :notifications-center])))}
      :main-icons2/notifications]
     (when (pos? notif-count)
       [react/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                    :pointer-events :none}
        [react/view {:style               styles/counter-public
                     :accessibility-label :notifications-unread-badge}]])]))
