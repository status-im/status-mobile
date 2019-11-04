(ns status-im.hardwallet.common
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.keycard.keycard-interaction :as keycard-sheet]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.ui.components.bottom-sheet.events :as bottom-sheet]
            [status-im.utils.keychain.core :as keychain]
            [status-im.hardwallet.nfc :as nfc]))

(def default-pin "000000")

(def pin-mismatch-error #"Unexpected error SW, 0x63C\d+")

(fx/defn dispatch-event
  [_ event]
  {:dispatch [event]})

(defn vector->string
  "Converts numbers stored in vector into string,
  e.g. [1 2 3 4 5 6] -> \"123456\""
  [v]
  (apply str v))

(defn hardwallet-supported? []
  (and config/hardwallet-enabled?
       platform/android?
       (nfc/nfc-supported?)))

(defn get-card-state
  [{:keys [has-master-key?
           applet-installed?
           initialized?
           free-pairing-slots
           paired?]}]
  (cond

    (not applet-installed?)
    :blank

    (not initialized?)
    :pre-init

    (not has-master-key?)
    :init

    has-master-key?
    :multiaccount

    (and (not paired?)
         (zero? free-pairing-slots))
    :no-pairing-slots))

(defn tag-lost? [error]
  (= error "Tag was lost."))

(defn find-multiaccount-by-keycard-instance-uid
  [db keycard-instance-uid]
  (when keycard-instance-uid
    (->> (:multiaccounts/multiaccounts db)
         vals
         (filter #(= keycard-instance-uid (:keycard-instance-uid %)))
         first)))

(defn find-multiaccount-by-key-uid
  [db key-uid]
  (when key-uid
    (->> (:multiaccounts/multiaccounts db)
         vals
         (filter #(= (ethereum/normalized-hex key-uid) (:key-uid %)))
         first)))

(defn get-pairing
  ([db]
   (get-pairing db (get-in db [:hardwallet :application-info :key-uid])))
  ([db key-uid]
   (or
    (get-in db [:multiaccount :keycard-pairing])
    (get-in db [:hardwallet :secrets :pairing])
    (when key-uid
      (:keycard-pairing
       (find-multiaccount-by-key-uid db key-uid))))))

(re-frame/reg-fx
 :hardwallet/set-nfc-supported
 (fn [supported?]
   (nfc/set-nfc-supported? supported?)))

(fx/defn listen-to-hardware-back-button
  [{:keys [db]}]
  (when-not (get-in db [:hardwallet :back-button-listener])
    {:hardwallet/listen-to-hardware-back-button nil}))

(fx/defn remove-listener-to-hardware-back-button
  [{:keys [db]}]
  (when-let [listener (get-in db [:hardwallet :back-button-listener])]
    {:hardwallet/remove-listener-to-hardware-back-button listener}))

(fx/defn set-on-card-connected
  [{:keys [db]} on-connect]
  (log/debug "[hardwallet] set-on-card-connected" on-connect)
  {:db (-> db
           (assoc-in [:hardwallet :on-card-connected] on-connect)
           (assoc-in [:hardwallet :last-on-card-connected] nil))})

(fx/defn stash-on-card-connected
  [{:keys [db]}]
  (let [on-connect (get-in db [:hardwallet :on-card-connected])]
    (log/debug "[hardwallet] stash-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:hardwallet :last-on-card-connected] on-connect)
             (assoc-in [:hardwallet :on-card-connected] nil))}))

(fx/defn restore-on-card-connected
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:hardwallet :on-card-connected])
                    (get-in db [:hardwallet :last-on-card-connected]))]
    (log/debug "[hardwallet] restore-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:hardwallet :on-card-connected] on-connect)
             (assoc-in [:hardwallet :last-on-card-connect] nil))}))

(fx/defn clear-on-card-connected
  [{:keys [db]}]
  (log/debug "[hardwallet] clear-on-card-connected")
  {:db (-> db
           (assoc-in [:hardwallet :on-card-connected] nil)
           (assoc-in [:hardwallet :last-on-card-connected] nil))})

(fx/defn set-on-card-read
  [{:keys [db]} on-connect]
  (log/debug "[hardwallet] set-on-card-read" on-connect)
  {:db (-> db
           (assoc-in [:hardwallet :on-card-read] on-connect)
           (assoc-in [:hardwallet :last-on-card-read] nil))})

(fx/defn stash-on-card-read
  [{:keys [db]}]
  (let [on-connect (get-in db [:hardwallet :on-card-read])]
    (log/debug "[hardwallet] stash-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:hardwallet :last-on-card-read] on-connect)
             (assoc-in [:hardwallet :on-card-read] nil))}))

(fx/defn restore-on-card-read
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:hardwallet :on-card-read])
                    (get-in db [:hardwallet :last-on-card-read]))]
    (log/debug "[hardwallet] restore-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:hardwallet :on-card-read] on-connect)
             (assoc-in [:hardwallet :last-on-card-connect] nil))}))

(fx/defn clear-on-card-read
  [{:keys [db]}]
  (log/debug "[hardwallet] clear-on-card-read")
  {:db (-> db
           (assoc-in [:hardwallet :on-card-read] nil)
           (assoc-in [:hardwallet :last-on-card-read] nil))})

(defn keycard-sheet-content [on-cancel connected?]
  (fn []
    (keycard-sheet/connect-keycard
     {:on-cancel     #(re-frame/dispatch on-cancel)
      :connected?    connected?
      :on-connect    ::on-card-connected
      :on-disconnect ::on-card-disconnected})))

(fx/defn show-connection-sheet
  [{:keys [db] :as cofx} {:keys [on-card-connected on-card-read handler]
                          {:keys [on-cancel]
                           :or   {on-cancel [::cancel-sheet-confirm]}}
                          :sheet-options}]
  (assert (keyword? on-card-connected))
  (assert (fn? handler))
  (let [connected? (get-in db [:hardwallet :card-connected?])]
    (log/debug "[hardwallet] show-sheet-with-connection-check"
               "card-connected?" connected?)
    (fx/merge
     cofx
     {:dismiss-keyboard true}
     (bottom-sheet/show-bottom-sheet
      {:view {:show-handle?       false
              :backdrop-dismiss?  false
              :disable-drag?      true
              :back-button-cancel false
              :content            (keycard-sheet-content on-cancel connected?)}})
     (when on-card-read
       (set-on-card-read on-card-read))
     (set-on-card-connected on-card-connected)
     (when connected?
       (stash-on-card-connected))
     (when connected?
       handler))))

(fx/defn hide-connection-sheet
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :card-read-in-progress?] false)}
            (restore-on-card-connected)
            (restore-on-card-read)
            (bottom-sheet/hide-bottom-sheet)))

(fx/defn clear-pin
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update-in db
                            [:hardwallet :pin]
                            merge
                            {:status       nil
                             :login        (get-in db [:hardwallet :pin :original])
                             :export-key   []
                             :sign         []
                             :puk          []
                             :current      []
                             :original     []
                             :confirmation []
                             :error-label  nil})}))

(fx/defn cancel-sheet-confirm
  {:events [::cancel-sheet-confirm
            :hardwallet/back-button-pressed]}
  [cofx]
  (fx/merge cofx
            (hide-connection-sheet)
            (clear-pin)))

(fx/defn cancel-sheet
  {:events [::cancel-sheet]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/keycard-cancel-setup-title)
                          :content             (i18n/label :t/keycard-cancel-setup-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [::cancel-sheet-confirm])
                          :on-cancel           #()}})

(fx/defn on-add-listener-to-hardware-back-button
  "Adds listener to hardware back button on Android.
  During keycard setup we show user a warning that setup will be cancelled
  when back button pressed. This prevents user from going back during setup
  flow as some of the actions changing keycard step could not be repeated."
  {:events [:hardwallet/add-listener-to-hardware-back-button]}
  [{:keys [db]} listener]
  {:db (assoc-in db [:hardwallet :back-button-listener] listener)})

(fx/defn show-wrong-keycard-alert
  [_ card-connected?]
  (when card-connected?
    {:utils/show-popup {:title   (i18n/label :t/wrong-card)
                        :content (i18n/label :t/wrong-card-text)}}))

(fx/defn unauthorized-operation
  [cofx]
  (fx/merge cofx
            {:utils/show-popup {:title   ""
                                :content (i18n/label :t/keycard-unauthorized-operation)}}
            (clear-on-card-connected)
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn navigate-to-enter-pin-screen
  {:events [:hardwallet/navigate-to-enter-pin-screen]}
  [{:keys [db] :as cofx}]
  (let [key-uid               (get-in db [:hardwallet :application-info :key-uid])
        multiaccount-key-uid  (get-in db [:multiaccount :key-uid])
        keycard-multiaccount? (boolean (get-in db [:multiaccount :keycard-pairing]))]
    ;; TODO(Ferossgp): If last oeperation was with wrong card,
    ;; it does not mean that current operation will be with the same card.
    ;; Because key-uid is stored from latest application-info read user can't
    ;; start the new operation cause account key-uid is not equal to the one from old read
    ;; Ideally application info should not be stored in db and only checked when need
    ;; thus we can ensure that we have always the right card info and not outdated one.
    (if (or (nil? keycard-multiaccount?)
            (and key-uid
                 (= key-uid multiaccount-key-uid)))
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :pin :current] [])}
                (navigation/navigate-to-cofx :enter-pin-settings nil))
      (unauthorized-operation cofx))))

(defn- tag-lost-exception? [code error]
  (or
   (= code "android.nfc.TagLostException")
   (= error "Tag was lost.")))

(fx/defn process-error [{:keys [db]} code error]
  (when-not (tag-lost-exception? code error)
    {:db (assoc-in db [:hardwallet :setup-step] :error)}))

(fx/defn get-keys-from-keycard
  [{:keys [db]}]
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])
        pairing (get-in db [:multiaccounts/multiaccounts key-uid :keycard-pairing])
        pin     (string/join (get-in db [:hardwallet :pin :login]))]
    (when (and pairing
               (seq pin))
      {:db                  (assoc-in db [:hardwallet :pin :status] :verifying)
       :hardwallet/get-keys {:pairing pairing
                             :pin     pin}})))

(fx/defn on-get-keys-success
  {:events [:hardwallet.callback/on-get-keys-success]}
  [{:keys [db] :as cofx} data]
  (let [{:keys [key-uid encryption-public-key whisper-private-key]
         :as   account-data}      (js->clj data :keywordize-keys true)
        {:keys [photo-path name]} (get-in db [:multiaccounts/multiaccounts key-uid])
        key-uid                   (get-in db [:hardwallet :application-info :key-uid])
        multiaccount-data         (types/clj->json {:name       name
                                                    :key-uid    key-uid
                                                    :photo-path photo-path})
        save-keys?                (get-in db [:multiaccounts/login :save-password?])]
    (fx/merge cofx
              {:db
               (-> db
                   (assoc-in [:hardwallet :pin :status] nil)
                   (assoc-in [:hardwallet :pin :login] [])
                   (assoc-in [:hardwallet :multiaccount]
                             (update account-data :whisper-public-key ethereum/normalized-hex))
                   (assoc-in [:hardwallet :flow] nil)
                   (update :multiaccounts/login assoc
                           :password encryption-public-key
                           :key-uid key-uid
                           :photo-path photo-path
                           :name name))

               :hardwallet/get-application-info {:pairing (get-pairing db key-uid)}
               :hardwallet/login-with-keycard   {:multiaccount-data multiaccount-data
                                                 :password          encryption-public-key
                                                 :chat-key          whisper-private-key}}
              (when save-keys?
                (keychain/save-hardwallet-keys key-uid encryption-public-key whisper-private-key))
              (clear-on-card-connected)
              (clear-on-card-read)
              (hide-connection-sheet))))

(fx/defn on-get-keys-error
  {:events [:hardwallet.callback/on-get-keys-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] get keys error: " error)
  (let [tag-was-lost? (tag-lost? (:error error))
        key-uid       (get-in db [:hardwallet :application-info :key-uid])
        flow          (get-in db [:hardwallet :flow])]
    (if tag-was-lost?
      {:db (assoc-in db [:hardwallet :pin :status] nil)}
      (if (re-matches pin-mismatch-error (:error error))
        (fx/merge cofx
                  {:hardwallet/get-application-info {:pairing (get-pairing db key-uid)}
                   :db                              (update-in db [:hardwallet :pin] merge {:status              :error
                                                                                            :login               []
                                                                                            :import-multiaccount []
                                                                                            :error-label         :t/pin-mismatch})}
                  (hide-connection-sheet)
                  (when (= flow :import)
                    (navigation/navigate-to-cofx :keycard-recovery-pin nil)))
        (show-wrong-keycard-alert true)))))

;; Get application info

(fx/defn get-application-info
  {:events [:hardwallet/get-application-info]}
  [{:keys [db]} pairing on-card-read]
  (let [key-uid  (get-in db [:hardwallet :application-info :key-uid])
        pairing' (or pairing (some->> key-uid (get-pairing db)))]
    (log/debug "[hardwallet] get-application-info"
               "pairing" pairing')
    {:hardwallet/get-application-info {:pairing    pairing'
                                       :on-success on-card-read}}))

(fx/defn on-get-application-info-success
  {:events [:hardwallet.callback/on-get-application-info-success]}
  [{:keys [db] :as cofx} info on-success]
  (let [info' (-> info
                  (js->clj :keywordize-keys true)
                  (update :key-uid ethereum/normalized-hex))
        {:keys [pin-retry-counter puk-retry-counter]} info'
        view-id (:view-id db)

        {:keys [on-card-read]} (:hardwallet db)
        on-success' (or on-success on-card-read)
        enter-step (if (zero? pin-retry-counter)
                     :puk
                     (get-in db [:hardwallet :pin :enter-step]))]
    (log/debug "[hardwallet] on-get-application-info-success"
               "on-success" on-success')
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :pin :enter-step] enter-step)
                       (update-in [:hardwallet :pin :error-label] #(if (= :puk enter-step)
                                                                     :t/enter-puk-code-description
                                                                     %))
                       (assoc-in [:hardwallet :application-info] info')
                       (assoc-in [:hardwallet :application-info :applet-installed?] true)
                       (assoc-in [:hardwallet :application-info-error] nil))}
              (stash-on-card-read)
              (if (zero? puk-retry-counter)
                {:utils/show-popup {:title   (i18n/label :t/error)
                                    :content (i18n/label :t/keycard-blocked)}}
                (when on-success'
                  (dispatch-event on-success'))))))

(fx/defn on-get-application-info-error
  {:events [:hardwallet.callback/on-get-application-info-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] application info error " error)
  (let [on-card-read      (get-in db [:hardwallet :on-card-read])
        on-card-connected (get-in db [:hardwallet :on-card-connected])
        login?            (= on-card-read :hardwallet/login-with-keycard)
        tag-was-lost?     (tag-lost? (:error error))]
    (when-not tag-was-lost?
      (if login?
        (fx/merge cofx
                  (clear-on-card-read)
                  (navigation/navigate-to-cofx :not-keycard nil))
        (fx/merge cofx
                  {:db (assoc-in db [:hardwallet :application-info-error] error)}

                  (when (= on-card-connected :hardwallet/prepare-to-sign)
                    (show-wrong-keycard-alert true))

                  (when on-card-read
                    (dispatch-event on-card-read)))))))

(fx/defn on-card-connected
  {:events [::on-card-connected]}
  [{:keys [db] :as cofx} _]
  (let [instance-uid              (get-in db [:hardwallet :application-info :instance-uid])
        key-uid                   (get-in db [:hardwallet :application-info :key-uid])
        should-read-instance-uid? (nil? instance-uid)
        on-card-connected         (get-in db [:hardwallet :on-card-connected])
        on-card-read              (cond
                                    should-read-instance-uid? :hardwallet/get-application-info
                                    :else                     (get-in db [:hardwallet :on-card-read]))
        pairing                   (get-pairing db key-uid)]
    (log/debug "[hardwallet] on-card-connected" on-card-connected
               "on-card-read" on-card-read)
    (when on-card-connected
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:hardwallet :card-read-in-progress?] (boolean on-card-read)))}
                (when on-card-connected
                  (dispatch-event on-card-connected))
                (stash-on-card-connected)
                (when (and on-card-read
                           (nil? on-card-connected))
                  (get-application-info pairing on-card-read))))))

(fx/defn on-card-disconnected
  {:events [::on-card-disconnected]}
  [{:keys [db] :as cofx} _]
  (log/debug "[hardwallet] card disconnected")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :card-read-in-progress?] false))}
            (restore-on-card-connected)
            (restore-on-card-read)))

(defn keycard-multiaccount? [db]
  (boolean (get-in db [:multiaccount :keycard-pairing])))

(fx/defn verify-pin
  {:events [:hardwallet/verify-pin]}
  [{:keys [db] :as cofx} {:keys [pin-step on-card-connected on-failure on-success]}]
  (let [on-success (or on-success
                       (get-in db [:hardwallet :pin :on-verified]))
        on-failure (or on-failure
                       (get-in db [:hardwallet :pin :on-verified-failure]))]
    (fx/merge
     cofx
     {:db (update-in db [:hardwallet :pin] assoc
                     :on-verified on-success
                     :on-verified-failure on-failure)}
     (show-connection-sheet
      {:on-card-connected (or on-card-connected :hardwallet/verify-pin)
       :handler
       (fn [{:keys [db] :as cofx}]
         (let [pin     (vector->string (get-in db [:hardwallet :pin pin-step]))
               pairing (get-pairing db)]
           (fx/merge
            cofx
            {:db                    (assoc-in db [:hardwallet :pin :status] :verifying)
             :hardwallet/verify-pin {:pin        pin
                                     :pairing    pairing}})))}))))
