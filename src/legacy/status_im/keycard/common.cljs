(ns legacy.status-im.keycard.common
  (:require
    [clojure.string :as string]
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.keycard.nfc :as nfc]
    [legacy.status-im.popover.core :as popover]
    [legacy.status-im.ui.screens.keycard.keycard-interaction :as keycard-sheet]
    [legacy.status-im.utils.deprecated-types :as types]
    [legacy.status-im.utils.keychain.core :as keychain]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def default-pin "000000")

(def pin-mismatch-error #"Unexpected error SW, 0x63C(\d+)|wrongPIN\(retryCounter: (\d+)\)")

(defn pin-retries
  [error]
  (when-let [matched-error (re-matches pin-mismatch-error error)]
    (js/parseInt (second (filter some? matched-error)))))

(rf/defn dispatch-event
  [_ event]
  {:dispatch-n [[event]]})

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
    :profile/profile

    (and (not paired?)
         (zero? free-pairing-slots))
    :no-pairing-slots))

(defn tag-lost?
  [error]
  (or
   (= error "Tag was lost.")
   (= error "NFCError:100")
   (re-matches #".*NFCError:100.*" error)))

(defn find-multiaccount-by-keycard-instance-uid
  [db keycard-instance-uid]
  (when keycard-instance-uid
    (->> (:profile/profiles-overview db)
         vals
         (filter #(= keycard-instance-uid (:keycard-instance-uid %)))
         first)))

(defn find-multiaccount-by-key-uid
  [db key-uid]
  (when key-uid
    (->> (:profile/profiles-overview db)
         vals
         (filter #(= (address/normalized-hex key-uid) (:key-uid %)))
         first)))

(defn get-pairing
  ([db]
   (get-pairing db (get-in db [:keycard :application-info :key-uid])))
  ([db key-uid]
   (or
    (get-in db [:profile/profile :keycard-pairing])
    (get-in db [:keycard :secrets :pairing])
    (when key-uid
      (:keycard-pairing
       (find-multiaccount-by-key-uid db key-uid))))))

(re-frame/reg-fx
 :keycard/set-nfc-supported
 (fn [supported?]
   (nfc/set-nfc-supported? supported?)))

(rf/defn listen-to-hardware-back-button
  [{:keys [db]}]
  (when-not (get-in db [:keycard :back-button-listener])
    {:keycard/listen-to-hardware-back-button nil}))

(rf/defn remove-listener-to-hardware-back-button
  [{:keys [db]}]
  (when-let [listener (get-in db [:keycard :back-button-listener])]
    {:keycard/remove-listener-to-hardware-back-button listener}))

(rf/defn set-on-card-connected
  [{:keys [db]} on-connect]
  (log/debug "[keycard] set-on-card-connected" on-connect)
  {:db (-> db
           (assoc-in [:keycard :on-card-connected] on-connect)
           (assoc-in [:keycard :last-on-card-connected] nil))})

(rf/defn stash-on-card-connected
  [{:keys [db]}]
  (let [on-connect (get-in db [:keycard :on-card-connected])]
    (log/debug "[keycard] stash-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:keycard :last-on-card-connected] on-connect)
             (assoc-in [:keycard :on-card-connected] nil))}))

(rf/defn restore-on-card-connected
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:keycard :on-card-connected])
                    (get-in db [:keycard :last-on-card-connected]))]
    (log/debug "[keycard] restore-on-card-connected" on-connect)
    {:db (-> db
             (assoc-in [:keycard :on-card-connected] on-connect)
             (assoc-in [:keycard :last-on-card-connect] nil))}))

(rf/defn clear-on-card-connected
  [{:keys [db]}]
  (log/debug "[keycard] clear-on-card-connected")
  {:db (-> db
           (assoc-in [:keycard :on-card-connected] nil)
           (assoc-in [:keycard :last-on-card-connected] nil))})

(rf/defn set-on-card-read
  [{:keys [db]} on-connect]
  (log/debug "[keycard] set-on-card-read" on-connect)
  {:db (-> db
           (assoc-in [:keycard :on-card-read] on-connect)
           (assoc-in [:keycard :last-on-card-read] nil))})

(rf/defn stash-on-card-read
  [{:keys [db]}]
  (let [on-connect (get-in db [:keycard :on-card-read])]
    (log/debug "[keycard] stash-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:keycard :last-on-card-read] on-connect)
             (assoc-in [:keycard :on-card-read] nil))}))

(rf/defn restore-on-card-read
  [{:keys [db]}]
  (let [on-connect (or
                    (get-in db [:keycard :on-card-read])
                    (get-in db [:keycard :last-on-card-read]))]
    (log/debug "[keycard] restore-on-card-read" on-connect)
    {:db (-> db
             (assoc-in [:keycard :on-card-read] on-connect)
             (assoc-in [:keycard :last-on-card-connect] nil))}))

(rf/defn clear-on-card-read
  [{:keys [db]}]
  (log/debug "[keycard] clear-on-card-read")
  {:db (-> db
           (assoc-in [:keycard :on-card-read] nil)
           (assoc-in [:keycard :last-on-card-read] nil))})

(defn keycard-sheet-content
  [on-cancel connected? params]
  (fn []
    [keycard-sheet/connect-keycard
     {:on-cancel     #(re-frame/dispatch on-cancel)
      :connected?    connected?
      :params        params
      :on-connect    ::on-card-connected
      :on-disconnect ::on-card-disconnected}]))

(rf/defn show-connection-sheet-component
  [{:keys [db] :as cofx}
   {:keys [on-card-connected on-card-read handler]
    {:keys [on-cancel]
     :or   {on-cancel [::cancel-sheet-confirm]}}
    :sheet-options}]
  (assert (keyword? on-card-connected))
  (assert (fn? handler))
  (let [connected? (get-in db [:keycard :card-connected?])]
    (log/debug "[keycard] show-sheet-with-connection-check"
               "card-connected?"
               connected?)
    (rf/merge
     cofx
     (bottom-sheet/show-bottom-sheet-old
      {:view {:transparent        platform/ios?
              :show-handle?       false
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

(rf/defn show-connection-sheet
  [{:keys [db] :as cofx} args]
  (let [nfc-running? (get-in db [:keycard :nfc-running?])]
    (log/debug "show connection; already running?" nfc-running?)
    (if nfc-running?
      (show-connection-sheet-component cofx args)
      {:keycard/start-nfc-and-show-connection-sheet args})))

(rf/defn on-nfc-ready-for-sheet
  {:events [:keycard.callback/show-connection-sheet]}
  [cofx args]
  (log/debug "on-nfc-ready-for-sheet")
  (show-connection-sheet-component cofx args))

(rf/defn hide-connection-sheet-component
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc-in db [:keycard :card-read-in-progress?] false)}
            (restore-on-card-connected)
            (restore-on-card-read)
            (bottom-sheet/hide-bottom-sheet-old)))

(rf/defn hide-connection-sheet
  [cofx]
  (log/debug "hide-connection-sheet")
  {:keycard/stop-nfc-and-hide-connection-sheet nil})

(rf/defn on-nfc-ready-to-close-sheet
  {:events [:keycard.callback/hide-connection-sheet]}
  [cofx]
  (log/debug "on-nfc-ready-to-close-sheet")
  (hide-connection-sheet-component cofx))

(rf/defn clear-pin
  [{:keys [db] :as cofx}]
  (rf/merge
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

(rf/defn cancel-sheet-confirm
  {:events [::cancel-sheet-confirm
            :keycard/back-button-pressed]}
  [{:keys [db] :as cofx}]
  (when-not (get-in db [:keycard :card-connected?])
    (rf/merge cofx
              (hide-connection-sheet)
              (clear-pin))))

(rf/defn cancel-sheet
  {:events [::cancel-sheet]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/keycard-cancel-setup-title)
                          :content             (i18n/label :t/keycard-cancel-setup-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [::cancel-sheet-confirm])
                          :on-cancel           #()}})

(rf/defn on-add-listener-to-hardware-back-button
  "Adds listener to hardware back button on Android.
  During keycard setup we show user a warning that setup will be cancelled
  when back button pressed. This prevents user from going back during setup
  flow as some of the actions changing keycard step could not be repeated."
  {:events [:keycard/add-listener-to-hardware-back-button]}
  [{:keys [db]} listener]
  {:db (assoc-in db [:keycard :back-button-listener] listener)})

(rf/defn show-wrong-keycard-alert
  [_]
  (log/debug "show-wrong-keycard-alert")
  {:effects.utils/show-popup {:title   (i18n/label :t/wrong-card)
                              :content (i18n/label :t/wrong-card-text)}})

(rf/defn unauthorized-operation
  [cofx]
  (rf/merge cofx
            {:effects.utils/show-popup {:title   ""
                                        :content (i18n/label :t/keycard-unauthorized-operation)}}
            (clear-on-card-connected)
            (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings])))

(rf/defn navigate-to-enter-pin-screen
  {:events [:keycard/navigate-to-enter-pin-screen]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc-in db [:keycard :pin :current] [])}
            (navigation/navigate-to :enter-pin-settings nil)))

(defn- tag-lost-exception?
  [code error]
  (or
   (= code "android.nfc.TagLostException")
   (= error "Tag was lost.")
   (= error "NFCError:100")))

(rf/defn process-error
  [{:keys [db]} code error]
  (when-not (tag-lost-exception? code error)
    {:db (assoc-in db [:keycard :setup-step] :error)}))

(rf/defn get-keys-from-keycard
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/login :key-uid])
        pin     (string/join (get-in db [:keycard :pin :login]))]
    (log/debug "[keycard] get-keys-from-keycard"
               ", not empty pin:"
               (boolean (seq pin)))
    (when (seq pin)
      {:db               (assoc-in db [:keycard :pin :status] :verifying)
       :keycard/get-keys {:pin pin}})))

(rf/defn on-get-keys-success
  {:events [:keycard.callback/on-get-keys-success]}
  [{:keys [db] :as cofx} data]
  (let [{:keys [key-uid encryption-public-key whisper-private-key]
         :as   account-data}
        (js->clj data :keywordize-keys true)
        {:keys [name]} (get-in db [:profile/profiles-overview key-uid])
        key-uid (get-in db [:keycard :application-info :key-uid])
        multiaccount-data (types/clj->json {:name    name
                                            :key-uid key-uid})
        save-keys? (get-in db [:profile/login :save-password?])]
    (rf/merge cofx
              {:db
               (-> db
                   (assoc-in [:keycard :pin :status] nil)
                   (assoc-in [:keycard :pin :login] [])
                   (update-in [:keycard :application-info]
                              assoc
                              :puk-retry-counter 5
                              :pin-retry-counter 3)
                   (assoc-in [:keycard :profile/profile]
                             (update account-data :whisper-public-key address/normalized-hex))
                   (assoc-in [:keycard :flow] nil)
                   (update :profile/login assoc
                           :password      encryption-public-key
                           :key-uid       key-uid
                           :name          name))

               :keycard/login-with-keycard {:multiaccount-data multiaccount-data
                                            :password          encryption-public-key
                                            :chat-key          whisper-private-key
                                            :key-uid           key-uid}}
              (when save-keys?
                (keychain/save-keycard-keys key-uid encryption-public-key whisper-private-key))
              (clear-on-card-connected)
              (clear-on-card-read)
              (hide-connection-sheet))))

(rf/defn blocked-or-frozen-keycard-popup
  [{:keys [db] :as cofx} card-status]
  (rf/merge
   cofx
   {:db (assoc-in db [:keycard :pin :status] card-status)}
   (hide-connection-sheet)
   ; do not try to display the popover if it is already open or
   ; we are in the login interface (which has a different handling)
   (when-not (or (:profile/login db) (:popover/popover db))
     (popover/show-popover {:view card-status}))))

(rf/defn blocked-keycard-popup
  [cofx]
  (blocked-or-frozen-keycard-popup cofx :blocked-card))

(rf/defn frozen-keycard-popup
  [cofx]
  (blocked-or-frozen-keycard-popup cofx :frozen-card))

(rf/defn on-get-keys-error
  {:events [:keycard.callback/on-get-keys-error]}
  [{:keys [db] :as cofx} error]
  (log/debug "[keycard] get keys error: " error)
  (let [tag-was-lost?     (tag-lost? (:error error))
        key-uid           (get-in db [:keycard :application-info :key-uid])
        flow              (get-in db [:keycard :flow])
        pin-retries-count (pin-retries (:error error))]
    (if tag-was-lost?
      {:db (assoc-in db [:keycard :pin :status] nil)}
      (if-not (nil? pin-retries-count)
        (rf/merge
         cofx
         {:db (-> db
                  (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries-count)
                  (update-in [:keycard :pin]
                             assoc
                             :status              :error
                             :login               []
                             :import-multiaccount []
                             :error-label         :t/pin-mismatch))}
         (hide-connection-sheet)
         (when (zero? pin-retries-count) (frozen-keycard-popup)))
        (show-wrong-keycard-alert)))))

(rf/defn factory-reset
  {:events [:keycard/factory-reset]}
  [{:keys [db]} on-card-read]
  (log/debug "[keycard] factory-reset")
  {:db                    (update db :keycard dissoc :factory-reset-card?)
   :keycard/factory-reset {:on-success on-card-read}})

;; Get application info

(rf/defn update-pairings
  [{:keys [db]} instance-uid pairing]
  (let [paired-on (datetime/timestamp)
        pairings  (-> (get-in db [:keycard :pairings])
                      (assoc instance-uid {:pairing pairing :paired-on paired-on}))]
    {:keycard/persist-pairings pairings
     :db                       (assoc-in db [:keycard :pairings] pairings)}))

(rf/defn get-application-info
  {:events [:keycard/get-application-info]}
  [{:keys [db]} on-card-read]
  (log/debug "[keycard] get-application-info")
  {:keycard/get-application-info {:on-success on-card-read}})

(rf/defn on-get-application-info-success
  {:events [:keycard.callback/on-get-application-info-success]}
  [{:keys [db] :as cofx} info on-success]
  (let [{:keys [pin-retry-counter puk-retry-counter instance-uid new-pairing]} info
        view-id                                                                (:view-id db)
        {:keys [on-card-read]}                                                 (:keycard db)
        on-success'                                                            (or on-success
                                                                                   on-card-read)
        enter-step                                                             (get-in db
                                                                                       [:keycard :pin
                                                                                        :enter-step])]
    (log/debug "[keycard] on-get-application-info-success"
               "on-success"        on-success'
               "pin-retry-counter" pin-retry-counter
               "puk-retry-counter" puk-retry-counter)
    (rf/merge
     cofx
     {:db (-> db
              (assoc-in [:keycard :pin :enter-step] enter-step)
              (assoc-in [:keycard :application-info] info)
              (assoc-in [:keycard :application-info :applet-installed?] true)
              (assoc-in [:keycard :application-info-error] nil))}
     (stash-on-card-read)
     (when new-pairing
       (update-pairings instance-uid new-pairing))
     (when (and (zero? pin-retry-counter)
                (pos? puk-retry-counter)
                (not= enter-step :puk))
       (frozen-keycard-popup))
     (fn [{:keys [db] :as cofx}]
       (if (zero? puk-retry-counter)
         (blocked-keycard-popup cofx)
         (when on-success'
           (dispatch-event cofx on-success')))))))

(rf/defn on-get-application-info-error
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
        (rf/merge cofx
                  (clear-on-card-read)
                  (navigation/navigate-to :not-keycard nil))
        (rf/merge cofx
                  {:db (assoc-in db [:keycard :application-info-error] error)}

                  (when (contains?
                         #{last-on-card-connected on-card-connected}
                         :keycard/prepare-to-sign)
                    (show-wrong-keycard-alert))

                  (when on-card-read
                    (dispatch-event on-card-read)))))))

(rf/defn on-card-connected
  {:events [::on-card-connected]}
  [{:keys [db] :as cofx} _]
  (let [instance-uid              (get-in db [:keycard :application-info :instance-uid])
        key-uid                   (get-in db [:keycard :application-info :key-uid])
        should-read-instance-uid? (nil? instance-uid)
        on-card-connected         (get-in db [:keycard :on-card-connected])
        on-card-read              (cond
                                    should-read-instance-uid? :keycard/get-application-info
                                    :else                     (get-in db [:keycard :on-card-read]))]
    (log/debug "[keycard] on-card-connected" on-card-connected
               "on-card-read"                on-card-read)
    (when on-card-connected
      (rf/merge cofx
                {:db (-> db
                         (assoc-in [:keycard :card-read-in-progress?] (boolean on-card-read)))}
                (when on-card-connected
                  (dispatch-event on-card-connected))
                (stash-on-card-connected)
                (when (and on-card-read
                           (nil? on-card-connected))
                  (get-application-info on-card-read))))))

(rf/defn on-card-disconnected
  {:events [::on-card-disconnected]}
  [{:keys [db] :as cofx} _]
  (log/debug "[keycard] card disconnected")
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :card-read-in-progress?] false))}
            (restore-on-card-connected)
            (restore-on-card-read)))

(defn keycard-multiaccount?
  [db]
  (boolean (get-in db [:profile/profile :keycard-pairing])))

(rf/defn verify-pin
  {:events [:keycard/verify-pin]}
  [{:keys [db] :as cofx} {:keys [pin-step on-card-connected on-failure on-success]}]
  (let [on-success (or on-success
                       (get-in db [:keycard :pin :on-verified]))
        on-failure (or on-failure
                       (get-in db [:keycard :pin :on-verified-failure]))
        pin-step   (or pin-step
                       (get-in db [:keycard :pin :step]))]
    (rf/merge
     cofx
     {:db (update-in db
                     [:keycard :pin]
                     assoc
                     :step                pin-step
                     :on-verified         on-success
                     :on-verified-failure on-failure)}
     (show-connection-sheet
      {:on-card-connected (or on-card-connected :keycard/verify-pin)
       :handler
       (fn [{:keys [db] :as cofx}]
         (let [pin (vector->string (get-in db [:keycard :pin pin-step]))]
           (rf/merge
            cofx
            {:db                 (assoc-in db [:keycard :pin :status] :verifying)
             :keycard/verify-pin {:pin pin}})))}))))

(rf/defn navigete-to-keycard-settings
  {:events [::navigate-to-keycard-settings]}
  [cofx]
  (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings]))
