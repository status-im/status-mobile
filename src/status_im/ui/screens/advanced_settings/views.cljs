(ns status-im.ui.screens.advanced-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]))

(defn- normal-mode-settings-data [{:keys [network-name
                                          current-log-level
                                          waku-enabled
                                          waku-bloom-filter-mode
                                          current-fleet
                                          dev-mode?]}]
  [{:type                 :small
    :title                :t/network
    :accessibility-label  :network-button
    :container-margin-top 8
    :on-press
    #(re-frame/dispatch [:navigate-to :network-settings])
    :accessories          [network-name :chevron]}
   {:type                 :small
    :title                :t/network-info
    :accessibility-label  :network-button
    :container-margin-top 8
    :on-press
    #(re-frame/dispatch [:navigate-to :network-info])
    :accessories          [:chevron]}
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
    :title                   :t/waku-enabled
    :accessibility-label     :waku-enabled-settings-switch
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/waku-enabled-switched (not waku-enabled)])
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       waku-enabled
       :on-value-change
       #(re-frame/dispatch
         [:multiaccounts.ui/waku-enabled-switched (not waku-enabled)])
       :disabled    false}]]}
   {:type                    :small
    :title                   :t/waku-bloom-filter-mode
    :accessibility-label     :waku-bloom-filter-mode-settings-switch
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/waku-bloom-filter-mode-switched (not waku-bloom-filter-mode)])
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       waku-bloom-filter-mode
       :on-value-change
       #(re-frame/dispatch
         [:multiaccounts.ui/waku-bloom-filter-mode-switched (not waku-bloom-filter-mode)])
       :disabled    false}]]}
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

(defn- flat-list-data [{:keys [dev-mode?
                               chaos-mode?]
                        :as options}]
  (if dev-mode?
    (into
     (normal-mode-settings-data options)
     (dev-mode-settings-data chaos-mode?))
    ;; else
    (normal-mode-settings-data options)))

(views/defview advanced-settings []
  (views/letsubs [{:keys [chaos-mode?]} [:multiaccount]
                  network-name             [:network-name]
                  waku-enabled             [:waku/enabled]
                  waku-bloom-filter-mode   [:waku/bloom-filter-mode]
                  current-log-level        [:log-level/current-log-level]
                  current-fleet            [:fleets/current-fleet]]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/advanced}]
     [list/flat-list
      {:data      (flat-list-data
                   {:network-name network-name
                    :current-log-level current-log-level
                    :current-fleet current-fleet
                    :dev-mode? false
                    :waku-enabled waku-enabled
                    :waku-bloom-filter-mode waku-bloom-filter-mode
                    :chaos-mode? chaos-mode?})

       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))
