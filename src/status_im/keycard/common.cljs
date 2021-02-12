(ns status-im.keycard.common
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.keycard.nfc :as nfc]
            [status-im.i18n.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.ui.screens.keycard.keycard-interaction :as keycard-sheet]
            [status-im.utils.fx :as fx]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.bottom-sheet.core :as bottom-sheet]))

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
   (get-pairing db (get-in db [:keycard :application-info :key-uid])))
  ([db key-uid]
   (or
    (get-in db [:multiaccount :keycard-pairing])
    (get-in db [:keycard :secrets :pairing])
    (when key-uid
      (:keycard-pairing
       (find-multiaccount-by-key-uid db key-uid))))))

(re-frame/reg-fx
 :keycard/set-nfc-supported
 (fn [supported?]
   (nfc/set-nfc-supported? supported?)))

(fx/defn listen-to-hardware-back-button
  [{:keys [db]}]
  (when-not (get-in db [:keycard :back-button-listener])
    {:keycard/listen-to-hardware-back-button nil}))

(fx/defn remove-listener-to-hardware-back-button
  [{:keys [db]}]
  (when-let [listener (get-in db [:keycard :back-button-listener])]
    {:keycard/remove-listener-to-hardware-back-button listener}))

(fx/defn set-on-card-connected
  [{:keys [db]} on-connect]
  (log/debug "[keycard] set-on-card-connected" on-connect)
  {:db (-> db
           (assoc-in [:keycard :on-card-connected] on-connect)
           (assoc-in [:keycard :last-on-card-connected] nil))})

(fx/defn stash-on-card-connected
  [{:keys [db]}]
  (let [on-connect (get-in db [:keycard :on-card-connected])]
    (log/debug "[keycard] stash-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:keycard :last-on-card-connected] on-connect)
             (assoc-in [:keycard :on-card-connected] nil))}))

(fx/defn restore-on-card-connected
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:keycard :on-card-connected])
                    (get-in db [:keycard :last-on-card-connected]))]
    (log/debug "[keycard] restore-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:keycard :on-card-connected] on-connect)
             (assoc-in [:keycard :last-on-card-connect] nil))}))

(fx/defn clear-on-card-connected
  [{:keys [db]}]
  (log/debug "[keycard] clear-on-card-connected")
  {:db (-> db
           (assoc-in [:keycard :on-card-connected] nil)
           (assoc-in [:keycard :last-on-card-connected] nil))})

(fx/defn set-on-card-read
  [{:keys [db]} on-connect]
  (log/debug "[keycard] set-on-card-read" on-connect)
  {:db (-> db
           (assoc-in [:keycard :on-card-read] on-connect)
           (assoc-in [:keycard :last-on-card-read] nil))})

(fx/defn stash-on-card-read
  [{:keys [db]}]
  (let [on-connect (get-in db [:keycard :on-card-read])]
    (log/debug "[keycard] stash-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:keycard :last-on-card-read] on-connect)
             (assoc-in [:keycard :on-card-read] nil))}))

(fx/defn restore-on-card-read
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:keycard :on-card-read])
                    (get-in db [:keycard :last-on-card-read]))]
    (log/debug "[keycard] restore-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:keycard :on-card-read] on-connect)
             (assoc-in [:keycard :last-on-card-connect] nil))}))

(fx/defn clear-on-card-read
  [{:keys [db]}]
  (log/debug "[keycard] clear-on-card-read")
  {:db (-> db
           (assoc-in [:keycard :on-card-read] nil)
           (assoc-in [:keycard :last-on-card-read] nil))})

(defn keycard-sheet-content [on-cancel connected? params]
  (fn []
    (keycard-sheet/connect-keycard
     {:on-cancel     #(re-frame/dispatch on-cancel)
      :connected?    connected?
      :params        params
      :on-connect    ::on-card-connected
      :on-disconnect ::on-card-disconnected})))

(fx/defn show-connection-sheet
  [{:keys [db] :as cofx} {:keys [on-card-connected on-card-read handler]
                          {:keys [on-cancel]
                           :or   {on-cancel [::cancel-sheet-confirm]}}
                          :sheet-options}]
  (assert (keyword? on-card-connected))
  (assert (fn? handler))
  (let [connected? (get-in db [:keycard :card-connected?])]
    (log/debug "[keycard] show-sheet-with-connection-check"
               "card-connected?" connected?)
    (fx/merge
     cofx
     {:dismiss-keyboard true}
     (bottom-sheet/show-bottom-sheet
      {:view {:show-handle?       false
              :backdrop-dismiss?  false
              :disable-drag?      true
              :back-button-cancel false
              :content            (keycard-sheet-content on-cancel connected? nil)}})
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
            {:db (assoc-in db [:keycard :card-read-in-progress?] false)}
            (restore-on-card-connected)
            (restore-on-card-read)
            (bottom-sheet/hide-bottom-sheet)))

(fx/defn clear-pin
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (assoc-in db
                  [:keycard :pin]
                  {:status              nil
                   :login               (get-in db [:keycard :pin :original])
                   :export-key          []
                   :sign                []
                   :puk                 []
                   :current             []
                   :original            []
                   :confirmation        []
                   :error-label         nil
                   :on-verified         (get-in db [:keycard :pin :on-verified])
                   :on-verified-failure (get-in db [:keycard :pin :on-verified])})}))

(fx/defn cancel-sheet-confirm
  {:events [::cancel-sheet-confirm
            :keycard/back-button-pressed]}
  [{:keys [db] :as cofx}]
  (when-not (get-in db [:keycard :card-connected?])
    (fx/merge cofx
              (hide-connection-sheet)
              (clear-pin))))

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
  {:events [:keycard/add-listener-to-hardware-back-button]}
  [{:keys [db]} listener]
  {:db (assoc-in db [:keycard :back-button-listener] listener)})

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
  {:events [:keycard/navigate-to-enter-pin-screen]}
  [{:keys [db] :as cofx}]
  (let [key-uid               (get-in db [:keycard :application-info :key-uid])
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
                {:db (assoc-in db [:keycard :pin :current] [])}
                (navigation/navigate-to-cofx :enter-pin-settings nil))
      (unauthorized-operation cofx))))

(defn- tag-lost-exception? [code error]
  (or
   (= code "android.nfc.TagLostException")
   (= error "Tag was lost.")))

(fx/defn process-error [{:keys [db]} code error]
  (when-not (tag-lost-exception? code error)
    {:db (assoc-in db [:keycard :setup-step] :error)}))

(fx/defn get-keys-from-keycard
  [{:keys [db]}]
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])
        pairing (get-in db [:multiaccounts/multiaccounts key-uid :keycard-pairing])
        pin     (string/join (get-in db [:keycard :pin :login]))]
    (log/debug "[keycard] get-keys-from-keycard"
               "not nil pairing:" (boolean pairing)
               ", not empty pin:" (boolean (seq pin)))
    (when (and pairing
               (seq pin))
      {:db               (assoc-in db [:keycard :pin :status] :verifying)
       :keycard/get-keys {:pairing pairing
                          :pin     pin}})))

(fx/defn on-get-keys-success
  {:events [:keycard.callback/on-get-keys-success]}
  [{:keys [db] :as cofx} data]
  (let [{:keys [key-uid encryption-public-key whisper-private-key]
         :as   account-data}      (js->clj data :keywordize-keys true)
        {:keys [identicon name]} (get-in db [:multiaccounts/multiaccounts key-uid])
        key-uid                   (get-in db [:keycard :application-info :key-uid])
        multiaccount-data         (types/clj->json {:name       name
                                                    :key-uid    key-uid
                                                    :identicon identicon})
        save-keys?                (get-in db [:multiaccounts/login :save-password?])]
    (fx/merge cofx
              {:db
               (-> db
                   (assoc-in [:keycard :pin :status] nil)
                   (assoc-in [:keycard :pin :login] [])
                   (assoc-in [:keycard :multiaccount]
                             (update account-data :whisper-public-key ethereum/normalized-hex))
                   (assoc-in [:keycard :flow] nil)
                   (update :multiaccounts/login assoc
                           :password encryption-public-key
                           :key-uid key-uid
                           :identicon identicon
                           :name name))

               :keycard/get-application-info {:pairing (get-pairing db key-uid)}
               :keycard/login-with-keycard   {:multiaccount-data multiaccount-data
                                              :password          encryption-public-key
                                              :chat-key          whisper-private-key
                                              :key-uid           key-uid}}
              (when save-keys?
                (keychain/save-keycard-keys key-uid encryption-public-key whisper-private-key))
              (clear-on-card-connected)
              (clear-on-card-read)
              (hide-connection-sheet))))

(fx/defn on-get-keys-error
  {:events [:keycard.callback/on-get-keys-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] get keys error: " error)
  (let [tag-was-lost? (tag-lost? (:error error))
        key-uid       (get-in db [:keycard :application-info :key-uid])
        flow          (get-in db [:keycard :flow])]
    (if tag-was-lost?
      {:db (assoc-in db [:keycard :pin :status] nil)}
      (if (re-matches pin-mismatch-error (:error error))
        (fx/merge
         cofx
         {:keycard/get-application-info
          {:pairing (get-pairing db key-uid)}

          :db
          (update-in db [:keycard :pin] merge
                     {:status              :error
                      :login               []
                      :import-multiaccount []
                      :error-label         :t/pin-mismatch})}
         (hide-connection-sheet)
         (when (= flow :import)
           (navigation/navigate-to-cofx :keycard-recovery-pin nil)))
        (show-wrong-keycard-alert true)))))

;; Get application info

(fx/defn get-application-info
  {:events [:keycard/get-application-info]}
  [{:keys [db]} pairing on-card-read]
  (let [key-uid
        (when-not (:intro-wizard db)
          (get-in
           db [:keycard :application-info :key-uid]
           (get-in db [:multiaccounts/login :key-uid])))
        pairing' (or pairing (some->> key-uid (get-pairing db)))]
    (log/debug "[keycard] get-application-info"
               "pairing" pairing')
    {:keycard/get-application-info {:pairing    pairing'
                                    :on-success on-card-read}}))

(fx/defn frozen-keycard-popup
  [{:keys [db] :as cofx}]
  (if (:multiaccounts/login db)
    (fx/merge
     cofx
     {:db (assoc-in db [:keycard :pin :status] :frozen-card)}
     hide-connection-sheet)
    {:db (assoc db :popover/popover {:view :frozen-card})}))

(fx/defn on-get-application-info-success
  {:events [:keycard.callback/on-get-application-info-success]}
  [{:keys [db] :as cofx} info on-success]
  (let [{:keys [pin-retry-counter puk-retry-counter]} info
        view-id (:view-id db)

        {:keys [on-card-read]} (:keycard db)
        on-success' (or on-success on-card-read)
        enter-step  (get-in db [:keycard :pin :enter-step])]
    (log/debug "[keycard] on-get-application-info-success"
               "on-success" on-success'
               "pin-retry-counter" pin-retry-counter
               "puk-retry-counter" puk-retry-counter)
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:keycard :pin :enter-step] enter-step)
              (assoc-in [:keycard :application-info] info)
              (assoc-in [:keycard :application-info :applet-installed?] true)
              (assoc-in [:keycard :application-info-error] nil))}
     (stash-on-card-read)
     (when (and (zero? pin-retry-counter)
                (pos? puk-retry-counter)
                (not= enter-step :puk))
       (frozen-keycard-popup))
     (fn [{:keys [db] :as cofx}]
       (if (zero? puk-retry-counter)
         (fx/merge
          cofx
          {:db (assoc-in db [:keycard :pin :status] :blocked-card)}
          hide-connection-sheet)
         (when on-success'
           (dispatch-event cofx on-success')))))))

(fx/defn on-get-application-info-error
  {:events [:keycard.callback/on-get-application-info-error]}
  [{:keys [db] :as cofx} error]
  (let [on-card-read           (get-in db [:keycard :on-card-read])
        on-card-connected      (get-in db [:keycard :on-card-connected])
        last-on-card-connected (get-in db [:keycard :last-on-card-connected])
        login?                 (= on-card-read :keycard/login-with-keycard)
        tag-was-lost?          (tag-lost? (:error error))]
    (log/debug "[keycard] application info error"
               error
               on-card-connected
               last-on-card-connected)
    (when-not tag-was-lost?
      (if login?
        (fx/merge cofx
                  (clear-on-card-read)
                  (navigation/navigate-to-cofx :not-keycard nil))
        (fx/merge cofx
                  {:db (assoc-in db [:keycard :application-info-error] error)}

                  (when (contains?
                         #{last-on-card-connected on-card-connected}
                         :keycard/prepare-to-sign)
                    (show-wrong-keycard-alert true))

                  (when on-card-read
                    (dispatch-event on-card-read)))))))

(fx/defn on-card-connected
  {:events [::on-card-connected]}
  [{:keys [db] :as cofx} _]
  (let [instance-uid              (get-in db [:keycard :application-info :instance-uid])
        key-uid                   (get-in db [:keycard :application-info :key-uid])
        should-read-instance-uid? (nil? instance-uid)
        on-card-connected         (get-in db [:keycard :on-card-connected])
        on-card-read              (cond
                                    should-read-instance-uid? :keycard/get-application-info
                                    :else                     (get-in db [:keycard :on-card-read]))
        pairing                   (get-pairing db key-uid)]
    (log/debug "[keycard] on-card-connected" on-card-connected
               "on-card-read" on-card-read)
    (when on-card-connected
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:keycard :card-read-in-progress?] (boolean on-card-read)))}
                (when on-card-connected
                  (dispatch-event on-card-connected))
                (stash-on-card-connected)
                (when (and on-card-read
                           (nil? on-card-connected))
                  (get-application-info pairing on-card-read))))))

(fx/defn on-card-disconnected
  {:events [::on-card-disconnected]}
  [{:keys [db] :as cofx} _]
  (log/debug "[keycard] card disconnected")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :card-read-in-progress?] false))}
            (restore-on-card-connected)
            (restore-on-card-read)))

(defn keycard-multiaccount? [db]
  (boolean (get-in db [:multiaccount :keycard-pairing])))

(fx/defn verify-pin
  {:events [:keycard/verify-pin]}
  [{:keys [db] :as cofx} {:keys [pin-step on-card-connected on-failure on-success]}]
  (let [on-success (or on-success
                       (get-in db [:keycard :pin :on-verified]))
        on-failure (or on-failure
                       (get-in db [:keycard :pin :on-verified-failure]))
        pin-step   (or pin-step
                       (get-in db [:keycard :pin :step]))]
    (fx/merge
     cofx
     {:db (update-in db [:keycard :pin] assoc
                     :step pin-step
                     :on-verified on-success
                     :on-verified-failure on-failure)}
     (show-connection-sheet
      {:on-card-connected (or on-card-connected :keycard/verify-pin)
       :handler
       (fn [{:keys [db] :as cofx}]
         (let [pin     (vector->string (get-in db [:keycard :pin pin-step]))
               pairing (get-pairing db)]
           (fx/merge
            cofx
            {:db                    (assoc-in db [:keycard :pin :status] :verifying)
             :keycard/verify-pin {:pin     pin
                                  :pairing pairing}})))}))))

(fx/defn navigete-to-keycard-settings
  {:events [::navigate-to-keycard-settings]}
  [cofx]
  (navigation/navigate-reset
   cofx
   {:index  1
    :routes [{:name :my-profile}
             {:name :keycard-settings}]}))
