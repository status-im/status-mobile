(ns status-im.contexts.wallet.common.wizard.events
  (:require [status-im.contexts.wallet.common.wizard.utils :as wizard-utils]
            [utils.re-frame :as rf]))

(rf/reg-event-fx
 :wallet/wizard-navigate-forward
 (fn [{:keys [db]} [{:keys [current-screen flow-id start-flow?]}]]
   (let [next-screen (wizard-utils/wizard-find-next-screen db flow-id current-screen)]
     {:fx [[:dispatch
            (if start-flow?
              [:open-modal (:screen-id next-screen)]
              [:navigate-to-within-stack [(:screen-id next-screen) current-screen]])]]})))
