(ns status-im.contexts.network.effects
  (:require
    [native-module.core :as native-module]
    [react-native.net-info :as net-info]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.network/listen-to-network-info
 (fn []
   (net-info/add-net-info-listener
    #(rf/dispatch [:network/on-state-change
                   (js->clj % :keywordize-keys true)]))))

(rf/reg-fx
 :effects.network/notify-status-go
 (fn [network-type expensive?]
   (native-module/connection-change network-type expensive?)))
