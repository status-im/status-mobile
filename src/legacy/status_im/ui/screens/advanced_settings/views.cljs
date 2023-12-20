(ns legacy.status-im.ui.screens.advanced-settings.views
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn- normal-mode-settings-data
  [{:keys [network-name
           current-log-level
           waku-bloom-filter-mode
           transactions-management-enabled?
           wakuv2-flag
           current-fleet
           webview-debug
           test-networks-enabled?]}]
  (keep
   identity
   [{:size :small
     :title (i18n/label :t/network)
     :accessibility-label :network-button
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:navigate-to :network-settings])
     :accessory :text
     :accessory-text network-name
     :chevron true}
    {:size :small
     :title (i18n/label :t/network-info)
     :accessibility-label :network-button
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:navigate-to :network-info])
     :chevron true}
    {:size :small
     :title (i18n/label :t/log-level)
     :accessibility-label :log-level-settings-button
     :on-press
     #(re-frame/dispatch [:navigate-to :log-level-settings])
     :accessory :text
     :accessory-text current-log-level
     :chevron true}
    {:size :small
     :title (i18n/label :t/fleet)
     :accessibility-label :fleet-settings-button
     :on-press
     #(re-frame/dispatch [:navigate-to :fleet-settings])
     :accessory :text
     :accessory-text current-fleet
     :chevron true}
    (if wakuv2-flag
      {:size :small
       :title (i18n/label :t/wakuv2-settings)
       :accessibility-label :wakuv2-settings-button
       :on-press
       #(re-frame/dispatch [:wakuv2.ui/enter-settings-pressed])
       :chevron true}
      {:size :small
       :title (i18n/label :t/bootnodes)
       :accessibility-label :bootnodes-settings-button
       :on-press
       #(re-frame/dispatch [:navigate-to :bootnodes-settings])
       :chevron true})
    {:size :small
     :title (i18n/label :t/rpc-usage-info)
     :accessibility-label :rpc-usage-info
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:navigate-to :rpc-usage-info])
     :chevron true}
    {:size :small
     :title (i18n/label :t/peers-stats)
     :accessibility-label :peers-stats
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:navigate-to :peers-stats])
     :chevron true}
    {:size :small
     :title (i18n/label :t/transactions-management-enabled)
     :accessibility-label :transactions-management-enabled
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch
       [:multiaccounts.ui/switch-transactions-management-enabled
        (not transactions-management-enabled?)])
     :accessory :switch
     :active transactions-management-enabled?}
    {:size :small
     :title "Webview debug"
     :accessibility-label :webview-debug-switch
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch
       [:profile.settings/change-webview-debug (not webview-debug)])
     :accessory :switch
     :active webview-debug}
    {:size :small
     :title "Testnet mode"
     :accessibility-label :test-networks-enabled
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch [:profile.settings/toggle-test-networks])
     :accessory :switch
     :active test-networks-enabled?}
    {:size :small
     :title (i18n/label :t/waku-bloom-filter-mode)
     :accessibility-label :waku-bloom-filter-mode-settings-switch
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch
       [:multiaccounts.ui/waku-bloom-filter-mode-switched (not waku-bloom-filter-mode)])
     :accessory :switch
     :active waku-bloom-filter-mode}
    {:size                :small
     :title               (i18n/label :t/set-currency)
     :accessibility-label :wallet-change-currency
     :on-press            #(hide-sheet-and-dispatch
                            [:navigate-to :currency-settings])
     :chevron             true}]))

(defn- flat-list-data
  [options]
  (normal-mode-settings-data options))

(defn- render-item
  [props]
  (if (= (:type props) :section-header)
    [quo/list-header (:title props)]
    [list.item/list-item props]))

(views/defview advanced-settings
  []
  (views/letsubs [{:keys [webview-debug
                          test-networks-enabled?]} [:profile/profile]
                  network-name                     [:network-name]
                  waku-bloom-filter-mode           [:waku/bloom-filter-mode]
                  wakuv2-flag                      [:waku/v2-flag]
                  transactions-management-enabled? [:wallet-legacy/transactions-management-enabled?]
                  current-log-level                [:log-level/current-log-level]
                  current-fleet                    [:fleets/current-fleet]]
    [list/flat-list
     {:data      (flat-list-data
                  {:network-name                     network-name
                   :current-log-level                current-log-level
                   :transactions-management-enabled? transactions-management-enabled?
                   :current-fleet                    current-fleet
                   :dev-mode?                        false
                   :wakuv2-flag                      wakuv2-flag
                   :waku-bloom-filter-mode           waku-bloom-filter-mode
                   :webview-debug                    webview-debug
                   :test-networks-enabled?           test-networks-enabled?})
      :key-fn    (fn [_ i] (str i))
      :render-fn render-item}]))
