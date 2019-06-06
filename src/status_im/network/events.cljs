(ns status-im.network.events
  (:require [status-im.network.core :as network]
            [status-im.utils.handlers :as handlers]
            [re-frame.core :as re-frame]
            [status-im.utils.utils :as utils]))

(handlers/register-handler-fx
 :network.ui/save-network-pressed
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx]
   (network/save-network cofx)))

(handlers/register-handler-fx
 :network.ui/input-changed
 (fn [cofx [_ input-key value]]
   (network/set-input cofx input-key value)))

(handlers/register-handler-fx
 :network.ui/add-network-pressed
 (fn [cofx]
   (network/edit cofx)))

(handlers/register-handler-fx
 :network.callback/non-rpc-network-saved
 (fn [_ _]
   {:ui/close-application nil}))

(handlers/register-handler-fx
 :network.ui/save-non-rpc-network-pressed
 (fn [cofx [_ network]]
   (network/save-non-rpc-network cofx network)))

(handlers/register-handler-fx
 :network.ui/save-rpc-network-pressed
 (fn [cofx [_ network]]
   (network/save-rpc-network cofx network)))

(handlers/register-handler-fx
 :network.ui/remove-network-confirmed
 (fn [cofx [_ network]]
   (network/remove-network cofx network [:navigate-back])))

(handlers/register-handler-fx
 :network.ui/connect-network-pressed
 (fn [cofx [_ network-id]]
   (network/connect cofx {:network-id network-id
                          :on-failure (fn [{:keys [reason]} _]
                                        (utils/show-popup "Error" (str reason)))})))

(handlers/register-handler-fx
 :network.ui/delete-network-pressed
 (fn [cofx [_ network]]
   (network/delete cofx {:network network})))

(handlers/register-handler-fx
 :network.ui/network-entry-pressed
 (fn [cofx [_ network]]
   (network/open-network-details cofx network)))
