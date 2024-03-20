(ns status-im.contexts.wallet.common.wizard
  (:require [status-im.contexts.wallet.send.flow-config :as wallet-flow]
            [utils.re-frame :as rf]))

(defn- wizard-find-next-screen
  [db flow-config current-screen]
  (->> flow-config
       (filter (fn [{:keys [skip-step? screen-id]}]
                 (and (not= screen-id current-screen)
                      (not (and (fn? skip-step?) (skip-step? db))))))
       first))

(rf/reg-event-fx
 :wallet/wizard-navigate-forward
 (fn [{:keys [db]} [{:keys [current-screen flow-id start-flow?]}]]
   (let [flow-config (case flow-id
                       :wallet-flow wallet-flow/steps
                       nil)
         next-screen (wizard-find-next-screen db flow-config current-screen)]
     {:fx [[:dispatch
            (if start-flow?
              [:open-modal (:screen-id next-screen)]
              [:navigate-to-within-stack [(:screen-id next-screen) current-screen]])]]})))
