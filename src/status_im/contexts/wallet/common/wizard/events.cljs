(ns status-im.contexts.wallet.common.wizard.events
  (:require [status-im.contexts.wallet.bridge.flow-config :as wallet-bridge-flow]
            [status-im.contexts.wallet.send.flow-config :as wallet-send-flow]
            [utils.re-frame :as rf]))

(defn- wizard-find-next-screen
  [db flow-id current-screen]
  (let [flow-config (case flow-id
                      :wallet-send-flow   wallet-send-flow/steps
                      :wallet-bridge-flow wallet-bridge-flow/steps
                      nil)]
    (first (filter (fn [{:keys [skip-step? screen-id]}]
                     (and (not= screen-id current-screen)
                          (not (and (fn? skip-step?) (skip-step? db)))))
                   flow-config))))

(rf/reg-event-fx
 :wallet/wizard-navigate-forward
 (fn [{:keys [db]} [{:keys [current-screen flow-id start-flow?]}]]
   (let [next-screen (wizard-find-next-screen db flow-id current-screen)]
     {:fx [[:dispatch
            (if start-flow?
              [:open-modal (:screen-id next-screen)]
              [:navigate-to-within-stack [(:screen-id next-screen) current-screen]])]]})))
