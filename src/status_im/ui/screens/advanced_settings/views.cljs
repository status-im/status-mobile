(ns status-im.ui.screens.advanced-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :as views]))

(defn- normal-mode-settings-data [{:keys [network-name
                                          current-log-level
                                          waku-enabled
                                          waku-bloom-filter-mode
                                          current-fleet]}]
  [{:size                 :small
    :title                (i18n/label :t/network)
    :accessibility-label  :network-button
    :container-margin-top 8
    :on-press
    #(re-frame/dispatch [:navigate-to :network-settings])
    :accessory            :text
    :accessory-text       network-name
    :chevron              true}
   {:size                 :small
    :title                (i18n/label :t/network-info)
    :accessibility-label  :network-button
    :container-margin-top 8
    :on-press
    #(re-frame/dispatch [:navigate-to :network-info])
    :chevron              true}
   ;; TODO - uncomment when implemented
   ;; {:size                :small
   ;;  :title               :t/les-ulc
   ;;  :accessibility-label :log-level-settings-button
   ;;  :accessories         [:t/ulc-enabled :chevron]}
   {:size                :small
    :title               (i18n/label :t/log-level)
    :accessibility-label :log-level-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :log-level-settings])
    :accessory           :text
    :accessory-text      current-log-level
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/fleet)
    :accessibility-label :fleet-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :fleet-settings])
    :accessory           :text
    :accessory-text      current-fleet
    :chevron             true}
   {:size                :small
    :title               (i18n/label :t/bootnodes)
    :accessibility-label :bootnodes-settings-button
    :on-press
    #(re-frame/dispatch [:navigate-to :bootnodes-settings])
    :chevron             true}
   {:size                   :small
    :title                   (i18n/label :t/waku-enabled)
    :accessibility-label     :waku-enabled-settings-switch
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/waku-enabled-switched (not waku-enabled)])
    :accessory               :switch
    :active                  waku-enabled}
   {:size                   :small
    :title                   (i18n/label :t/waku-bloom-filter-mode)
    :accessibility-label     :waku-bloom-filter-mode-settings-switch
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/waku-bloom-filter-mode-switched (not waku-bloom-filter-mode)])
    :accessory               :switch
    :active                  waku-bloom-filter-mode}
   #_{:size                   :small
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
    ;; FIXME
    :type                 :section-header
    :title                (i18n/label :t/dev-mode-settings)}
   {:size                :small
    :title               (i18n/label :t/chaos-mode)
    :accessibility-label :chaos-mode-settings-switch
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/chaos-mode-switched (not chaos-mode?)])
    :accessory           :switch
    :active              chaos-mode?}
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

(defn- render-item [props]
  (if (= (:type props) :section-header)
    [quo/list-header (:title props)]
    [quo/list-item props]))

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
                   {:network-name           network-name
                    :current-log-level      current-log-level
                    :current-fleet          current-fleet
                    :dev-mode?              false
                    :waku-enabled           waku-enabled
                    :waku-bloom-filter-mode waku-bloom-filter-mode
                    :chaos-mode?            chaos-mode?})
       :key-fn    (fn [_ i] (str i))
       :render-fn render-item}]]))
