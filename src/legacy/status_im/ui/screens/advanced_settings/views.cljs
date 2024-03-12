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
  [{:keys [current-log-level
           light-client-enabled?
           transactions-management-enabled?
           current-fleet
           webview-debug
           test-networks-enabled?
           goerli-enabled?
           peer-syncing-enabled?]}]
  (keep
   identity
   [{:size :small
     :title (i18n/label :t/log-level)
     :accessibility-label :log-level-settings-button
     :on-press
     #(re-frame/dispatch [:open-modal :log-level-settings])
     :accessory :text
     :accessory-text current-log-level
     :chevron true}
    {:size :small
     :title (i18n/label :t/fleet)
     :accessibility-label :fleet-settings-button
     :on-press
     #(re-frame/dispatch [:open-modal :fleet-settings])
     :accessory :text
     :accessory-text current-fleet
     :chevron true}
    {:size :small
     :title (i18n/label :t/wakuv2-settings)
     :accessibility-label :wakuv2-settings-button
     :on-press
     #(re-frame/dispatch [:wakuv2.ui/enter-settings-pressed])
     :chevron true}
    {:size :small
     :title (i18n/label :t/rpc-usage-info)
     :accessibility-label :rpc-usage-info
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:open-modal :rpc-usage-info])
     :chevron true}
    {:size :small
     :title (i18n/label :t/peers-stats)
     :accessibility-label :peers-stats
     :container-margin-top 8
     :on-press
     #(re-frame/dispatch [:open-modal :peers-stats])
     :chevron true}
    {:size :small
     :title (i18n/label :t/light-client-enabled)
     :accessibility-label :light-client-enabled
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch
       [:wakuv2.ui/toggle-light-client (not light-client-enabled?)])
     :accessory :switch
     :active light-client-enabled?}
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
     :title "Enable Goerli as test network"
     :accessibility-label :enable-sepolia-as-test-network
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch [:profile.settings/toggle-goerli-test-network])
     :accessory :switch
     :active goerli-enabled?}
    {:size :small
     :title "Peer syncing"
     :accessibility-label :peer-syncing
     :container-margin-bottom 8
     :on-press
     #(re-frame/dispatch [:profile.settings/toggle-peer-syncing])
     :accessory :switch
     :active peer-syncing-enabled?}
    {:size                :small
     :title               (i18n/label :t/set-currency)
     :accessibility-label :wallet-change-currency
     :on-press            #(hide-sheet-and-dispatch
                            [:open-modal :currency-settings])
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
  (views/letsubs [test-networks-enabled?           [:profile/test-networks-enabled?]
                  goerli-enabled?                  [:profile/goerli-enabled?]
                  light-client-enabled?            [:profile/light-client-enabled?]
                  webview-debug                    [:profile/webview-debug]
                  transactions-management-enabled? [:wallet-legacy/transactions-management-enabled?]
                  current-log-level                [:log-level/current-log-level]
                  current-fleet                    [:fleets/current-fleet]
                  peer-syncing-enabled?            [:profile/peer-syncing-enabled?]]
    [list/flat-list
     {:data      (flat-list-data
                  {:current-log-level                current-log-level
                   :transactions-management-enabled? transactions-management-enabled?
                   :light-client-enabled?            light-client-enabled?
                   :current-fleet                    current-fleet
                   :dev-mode?                        false
                   :webview-debug                    webview-debug
                   :test-networks-enabled?           test-networks-enabled?
                   :goerli-enabled?                  goerli-enabled?
                   :peer-syncing-enabled?            peer-syncing-enabled?})
      :key-fn    (fn [_ i] (str i))
      :render-fn render-item}]))
