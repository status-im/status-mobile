(ns status-im.ui.screens.advanced-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
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
   #_{:type                    :small
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
   #_{:type :divider}])

(defn- dev-mode-settings-data [chaos-mode?]
  [{:container-margin-top 8
    :type                 :section-header
    :title                :t/dev-mode-settings}
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
   [react/view {:height 24}]])

(defn- flat-list-data [network-name current-log-level current-fleet
                       dev-mode? chaos-mode?]
  (if dev-mode?
    (into
     (normal-mode-settings-data
      network-name current-log-level current-fleet dev-mode?)
     (dev-mode-settings-data chaos-mode?))
    ;; else
    (normal-mode-settings-data
     network-name current-log-level current-fleet dev-mode?)))

(views/defview advanced-settings []
  (views/letsubs [{:keys [chaos-mode?]} [:multiaccount]
                  network-name             [:network-name]
                  current-log-level        [:settings/current-log-level]
                  current-fleet            [:settings/current-fleet]]
    [react/view {:flex 1 :background-color colors/white}
     [toolbar/simple-toolbar
      (i18n/label :t/advanced)]
     [list/flat-list
      {:data      (flat-list-data
                   network-name current-log-level
                   current-fleet false chaos-mode?)

       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))
