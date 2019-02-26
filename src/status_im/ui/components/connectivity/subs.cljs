(ns status-im.ui.components.connectivity.subs
  (:require
   [re-frame.core :as re-frame]
   [status-im.utils.platform :as utils.platform]
   [status-im.ui.screens.mobile-network-settings.utils :as mobile-network-utils]))

(re-frame/reg-sub
 :connectivity/status-properties
 :<- [:offline?]
 :<- [:disconnected?]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 :<- [:mailserver/fetching?]
 :<- [:get :network/type]
 :<- [:get :account/account]
 (fn [[offline? disconnected? mailserver-connecting? mailserver-connection-error?
       mailserver-request-error? mailserver-fetching? network-type account]]
   (let [wallet-offline? (and offline?
                              ;; There's no wallet of desktop
                              (not utils.platform/desktop?))
         error-label (cond
                       (and wallet-offline?
                            disconnected?)
                       :t/offline

                       wallet-offline?
                       :t/wallet-offline

                       disconnected?
                       :t/disconnected

                       mailserver-connecting?
                       :t/connecting

                       mailserver-connection-error?
                       :t/mailserver-reconnect

                       mailserver-request-error?
                       :t/mailserver-request-error-status

                       (and (mobile-network-utils/cellular? network-type)
                            (not (:syncing-on-mobile-network? account)))
                       :mobile-network

                       :else nil)]
     {:message (or error-label :t/connected)
      :connected? (and (nil? error-label) (not= :mobile-network error-label))
      :connecting? (= error-label :t/connecting)
      :loading-indicator? mailserver-fetching?
      :on-press-fn #(cond
                      mailserver-connection-error?
                      (re-frame/dispatch
                       [:mailserver.ui/reconnect-mailserver-pressed])
                      mailserver-request-error?
                      (re-frame/dispatch
                       [:mailserver.ui/request-error-pressed])

                      (= :mobile-network error-label)
                      (re-frame/dispatch

                       [:mobile-network/show-offline-sheet]))})))
