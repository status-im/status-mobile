(ns status-im.ui.screens.advanced-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn- normal-mode-settings-data [network-name current-log-level
                                  current-fleet dev-mode?]
  [{:type                 :small
    :title                :t/network
    :accessibility-label  :network-button
    :container-margin-top 8
    :on-press
    #(re-frame/dispatch [:navigate-to :network-settings])
    :accessories          [network-name :chevron]}
   ;; TODO - uncomment when implemented
   ;; {:type                :small
   ;;  :title               :t/les-ulc
   ;;  :accessibility-label :log-level-settings-button
   ;;  :accessories         [:t/ulc-enabled :chevron]}
   {:type                :small
    :title               :t/log-level
    :accessibility-label :log-level-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :log-level-settings])
    :accessories         [current-log-level :chevron]}
   {:type                :small
    :title               :t/fleet
    :accessibility-label :fleet-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :fleet-settings])
    :accessories         [current-fleet :chevron]}
   {:type                :small
    :title               :t/bootnodes
    :accessibility-label :bootnodes-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :bootnodes-settings])
    :accessories         [:chevron]}
   {:type                    :small
    :title                   :t/dev-mode
    :accessibility-label     :dev-mode-settings-switch
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/dev-mode-switched (not dev-mode?)])
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       dev-mode?
       :on-value-change
       #(re-frame/dispatch
         [:multiaccounts.ui/dev-mode-switched (not dev-mode?)])
       :disabled    false}]]}
   {:type :divider}])

(defn- dev-mode-settings-data [settings chaos-mode? supported-biometric-auth]
  [{:container-margin-top 8
    :type                 :section-header
    :title                :t/dev-mode-settings}
   {:type                :small
    :title               :t/datasync
    :accessibility-label :datasync-settings-switch
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       (boolean (:datasync? settings))
       :on-value-change
       #(re-frame/dispatch [:multiaccounts.ui/toggle-datasync %])
       :disabled    false}]]
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/toggle-datasync
       ((complement boolean) (:datasync? settings))])}
   {:type                :small
    :title               :t/v1-messages
    :accessibility-label :v1-messages-settings-switch
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       (boolean (:v1-messages? settings))
       :on-value-change
       #(re-frame/dispatch [:multiaccounts.ui/toggle-v1-messages %])
       :disabled    false}]]
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/toggle-v1-messages
       ((complement boolean) (:v1-messages? settings))])}
   {:type                :small
    :title               :t/disable-discovery-topic
    :accessibility-label :discovery-topic-settings-switch
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       (boolean (:disable-discovery-topic? settings))
       :on-value-change
       #(re-frame/dispatch [:multiaccounts.ui/toggle-disable-discovery-topic %])
       :disabled    false}]]
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/toggle-disable-discovery-topic
       ((complement boolean) (:disable-discovery-topic? settings))])}
   {:type                :small
    :title               :t/chaos-mode
    :accessibility-label :chaos-mode-settings-switch
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/chaos-mode-switched (not chaos-mode?)])
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       chaos-mode?
       :on-value-change
       #(re-frame/dispatch
         [:multiaccounts.ui/chaos-mode-switched (not chaos-mode?)])
       :disabled    false}]]}
   {:type                    :small
    :title                   :t/biometric-auth-setting-label
    :container-margin-bottom 8
    :accessibility-label     :biometric-auth-settings-switch
    :disabled?               (not (some? supported-biometric-auth))
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       (boolean (:biometric-auth? settings))
       :on-value-change
       #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched %])
       :disabled    (not (some? supported-biometric-auth))}]]
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/biometric-auth-switched
       ((complement boolean) (:biometric-auth? settings))])}
   [react/view {:height 24}]])

(defn- flat-list-data [network-name current-log-level current-fleet
                       dev-mode? settings chaos-mode? supported-biometric-auth]
  (if dev-mode?
    (into
     (normal-mode-settings-data
      network-name current-log-level current-fleet dev-mode?)
     (dev-mode-settings-data
      settings chaos-mode? supported-biometric-auth))
    ;; else
    (normal-mode-settings-data
     network-name current-log-level current-fleet dev-mode?)))

(views/defview advanced-settings []
  (views/letsubs [{:keys
                   [chaos-mode?
                    dev-mode?
                    settings]
                   :as current-multiaccount} [:multiaccount]
                  settings                   [:multiaccount-settings]
                  network-name               [:network-name]
                  current-log-level          [:settings/current-log-level]
                  current-fleet              [:settings/current-fleet]
                  supported-biometric-auth   [:supported-biometric-auth]]
    [react/view {:flex 1 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/advanced)]
     [list/flat-list
      {:data      (flat-list-data
                   network-name current-log-level
                   current-fleet dev-mode? settings
                   chaos-mode? supported-biometric-auth)

       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))
