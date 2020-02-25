(ns status-im.hardwallet.export-key
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.hardwallet.common :as common]))

(fx/defn on-export-key-error
  {:events [:hardwallet.callback/on-export-key-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] export key error" error)
  (let [tag-was-lost? (= "Tag was lost." (:error error))]
    (cond tag-was-lost?
          (fx/merge cofx
                    {:db               (assoc-in db [:hardwallet :pin :status] nil)
                     :utils/show-popup {:title   (i18n/label :t/error)
                                        :content (i18n/label :t/cannot-read-card)}}
                    (common/set-on-card-connected :wallet.accounts/generate-new-keycard-account)
                    (navigation/navigate-to-cofx :keycard-connection-lost nil))
          (re-matches common/pin-mismatch-error (:error error))
          (fx/merge cofx
                    {:db (update-in db [:hardwallet :pin] merge {:status       :error
                                                                 :enter-step   :export-key
                                                                 :puk          []
                                                                 :current      []
                                                                 :original     []
                                                                 :confirmation []
                                                                 :sign         []
                                                                 :error-label  :t/pin-mismatch})}
                    (navigation/navigate-back)
                    (common/get-application-info (common/get-pairing db) nil))
          :else (common/show-wrong-keycard-alert cofx true))))

(fx/defn on-export-key-success
  {:events [:hardwallet.callback/on-export-key-success]}
  [{:keys [db] :as cofx} pubkey]
  (let [multiaccount-address (get-in db [:multiaccount :address])
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        callback-fn (get-in db [:hardwallet :on-export-success])
        pairings (get-in db [:hardwallet :pairings])
        event-to-dispatch (callback-fn pubkey)]
    (re-frame/dispatch event-to-dispatch)
    (fx/merge cofx
              (common/clear-on-card-connected))))
