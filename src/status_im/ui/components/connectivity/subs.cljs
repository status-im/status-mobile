(ns status-im.ui.components.connectivity.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as utils.platform]
            [status-im.i18n :as i18n]))

(re-frame/reg-sub
 :connectivity/status-properties
 :<- [:offline?]
 :<- [:disconnected?]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 :<- [:mailserver/fetching?]
 (fn [[offline? disconnected? mailserver-connecting? mailserver-connection-error?
       mailserver-request-error? mailserver-fetching?]]
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

                       :else nil)]
     {:message (i18n/label (or error-label :t/connected))
      :connected? (nil? error-label)
      :connecting? (= error-label :t/connecting)
      :loading-indicator? mailserver-fetching?
      :on-press-fn #(cond
                      mailserver-connection-error?
                      (re-frame/dispatch
                       [:mailserver.ui/reconnect-mailserver-pressed])
                      mailserver-request-error?
                      (re-frame/dispatch
                       [:mailserver.ui/request-error-pressed]))})))
