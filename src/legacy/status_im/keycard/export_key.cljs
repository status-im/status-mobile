(ns legacy.status-im.keycard.export-key
  (:require
    [legacy.status-im.keycard.common :as common]
    [utils.re-frame :as rf]))

(rf/defn on-export-key-success
  {:events [:keycard.callback/on-export-key-success]}
  [{:keys [db] :as cofx} pubkey]
  (let [callback-fn (get-in db [:keycard :on-export-success])]
    (rf/merge cofx
              {:dispatch (callback-fn pubkey)}
              (common/clear-pin)
              (common/hide-connection-sheet))))
