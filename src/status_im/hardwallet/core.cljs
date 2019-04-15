(ns status-im.hardwallet.core
  (:require [re-frame.core :as re-frame]
            status-im.hardwallet.fx
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [status-im.utils.types :as types]
            [status-im.accounts.create.core :as accounts.create]
            [status-im.node.core :as node]
            [status-im.utils.datetime :as utils.datetime]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.utils.ethereum.core :as ethereum]
            [clojure.string :as string]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.accounts.recover.core :as accounts.recover]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.ethereum.mnemonic :as mnemonic]))

(def default-pin "000000")

(defn- find-account-by-keycard-instance-uid
  [db keycard-instance-uid]
  (when keycard-instance-uid
    (->> (:accounts/accounts db)
         vals
         (filter #(= keycard-instance-uid (:keycard-instance-uid %)))
         first)))

(defn get-pairing
  ([db]
   (get-pairing db (get-in db [:hardwallet :application-info :instance-uid])))
  ([db instance-uid]
   (or
    (get-in db [:account/account :keycard-pairing])
    (get-in db [:hardwallet :secrets :pairing])
    (when instance-uid
      (:keycard-pairing
       (find-account-by-keycard-instance-uid db instance-uid))))))

(fx/defn navigate-back-button-clicked
  [{:keys [db] :as cofx}]
  (let [screen-before (second (get db :navigation-stack))
        navigate-to-browser? (contains? #{:wallet-sign-message-modal
                                          :wallet-send-transaction-modal
                                          :wallet-send-modal-stack} screen-before)]
    (if navigate-to-browser?
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] nil)}
                (models.wallet/discard-transaction)
                (navigation/navigate-to-cofx :browser nil))
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] nil)}
                (navigation/navigate-back)))))

(fx/defn remove-pairing-from-account
  [{:keys [db]} {:keys [remove-instance-uid?]}]
  (let [account (cond-> (:account/account db)
                  true (assoc :keycard-pairing nil
                              :keycard-paired-on nil)
                  remove-instance-uid? (assoc :keycard-instance-uid nil))]
    {:db                 (-> db
                             (assoc :account/account account)
                             (assoc-in [:accounts/accounts (:address account)] account))
     :data-store/base-tx [(accounts-store/save-account-tx account)]}))

(defn hardwallet-supported? [{:keys [db]}]
  (and config/hardwallet-enabled?
       platform/android?
       (get-in db [:hardwallet :nfc-supported?])))

(fx/defn unauthorized-operation
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :on-card-connected] nil)
             :utils/show-popup {:title   ""
                                :content (i18n/label :t/keycard-unauthorized-operation)}}
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn show-no-keycard-applet-alert [_]
  {:utils/show-confirmation {:title               (i18n/label :t/no-keycard-applet-on-card)
                             :content             (i18n/label :t/keycard-applet-install-instructions)
                             :cancel-button-text  ""
                             :confirm-button-text :t/okay}})

(fx/defn show-keycard-has-account-alert
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                      (assoc-in db [:hardwallet :setup-step] nil)
             :utils/show-confirmation {:title               nil
                                       :content             (i18n/label :t/keycard-has-account-on-it)
                                       :cancel-button-text  ""
                                       :confirm-button-text :t/okay}}))

(defn- card-state->setup-step [state]
  (case state
    :not-paired :pair
    :no-pairing-slots :no-slots
    :begin))

(defn- get-card-state
  [{:keys [has-master-key?
           applet-installed?
           initialized?
           free-pairing-slots
           paired?]}]
  (cond

    (and (not paired?)
         (zero? free-pairing-slots))
    :no-pairing-slots

    (and (not paired?)
         has-master-key?
         (pos? free-pairing-slots))
    :not-paired

    (not applet-installed?)
    :blank

    (not initialized?)
    :pre-init

    (not has-master-key?)
    :init

    has-master-key?
    :account))

(fx/defn set-setup-step
  [{:keys [db]} card-state]
  {:db (assoc-in db [:hardwallet :setup-step] (card-state->setup-step card-state))})

(fx/defn check-card-state
  [{:keys [db] :as cofx}]
  (let [app-info (get-in db [:hardwallet :application-info])
        card-state (get-card-state app-info)
        setup-running? (boolean (get-in db [:hardwallet :setup-step]))
        db' (assoc-in db [:hardwallet :card-state] card-state)]
    (if setup-running?
      (fx/merge cofx
                {:db db'}
                (set-setup-step card-state)
                (if (= :pre-init card-state)
                  (navigation/navigate-to-cofx :hardwallet-setup nil)
                  (navigation/navigate-to-cofx :hardwallet-authentication-method nil))
                (when (= card-state :blank)
                  (show-no-keycard-applet-alert))
                (when (= card-state :account)
                  (show-keycard-has-account-alert)))
      {:db db'})))

(fx/defn navigate-to-keycard-settings
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :pin :on-verified] nil)
                     (assoc-in [:hardwallet :on-card-connected] nil)
                     (assoc-in [:hardwallet :setup-step] nil))}
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn navigate-to-enter-pin-screen
  [{:keys [db] :as cofx}]
  (let [keycard-instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        account-instance-uid (get-in db [:account/account :keycard-instance-uid])]
    (if (or (nil? account-instance-uid)
            (and keycard-instance-uid
                 (= keycard-instance-uid account-instance-uid)))
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :pin :current] [])}
                (navigation/navigate-to-cofx :enter-pin nil))
      (unauthorized-operation cofx))))

(fx/defn navigate-to-authentication-method
  [{:keys [db] :as cofx}]
  (if (hardwallet-supported? cofx)
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :flow] :create)}
              (navigation/navigate-to-cofx :hardwallet-authentication-method nil))
    (accounts.create/navigate-to-create-account-screen cofx)))

(fx/defn navigate-to-recover-method
  [{:keys [db] :as cofx}]
  (if (hardwallet-supported? cofx)
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :flow] :import)}
              (navigation/navigate-to-cofx :hardwallet-authentication-method nil))
    (accounts.recover/navigate-to-recover-account-screen cofx)))

(fx/defn password-option-pressed
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:hardwallet :flow]) :create)
    (accounts.create/navigate-to-create-account-screen cofx)
    (accounts.recover/navigate-to-recover-account-screen cofx)))

(defn settings-screen-did-load
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:hardwallet :pin :on-verified] nil)
           (assoc-in [:hardwallet :on-card-connected] nil)
           (assoc-in [:hardwallet :setup-step] nil))})

(defn reset-card-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :reset-card :disabled?] false)})

(defn enter-pin-screen-did-load
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:hardwallet :pin :sign] [])
           (assoc-in [:hardwallet :pin :login] [])
           (assoc-in [:hardwallet :pin :current] []))})

(defn hardwallet-connect-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :card-read-in-progress?] false)})

(defn accounts-screen-did-load
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:hardwallet :setup-step] nil)
           (dissoc :accounts/login))})

(defn authentication-method-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :setup-step] nil)})

(fx/defn on-register-card-events
  [{:keys [db]} listeners]
  {:db (update-in db [:hardwallet :listeners] merge listeners)})

(fx/defn get-keys-from-keycard
  [{:keys [db]}]
  (let [account-address (get-in db [:accounts/login :address])
        pairing (get-in db [:accounts/accounts account-address :keycard-pairing])
        pin (string/join (get-in db [:hardwallet :pin :login]))]
    (when (and pairing
               (not (empty? pin)))
      {:db                  (-> db
                                (assoc-in [:hardwallet :pin :status] :verifying))
       :hardwallet/get-keys {:pairing pairing
                             :pin     pin}})))

(fx/defn login-with-keycard
  [{:keys [db] :as cofx} auto-login?]
  (let [account-login-address (get-in db [:accounts/login :address])
        account-was-manually-selected? account-login-address
        account-instance-uid (get-in db [:accounts/accounts account-login-address :keycard-instance-uid])
        keycard-instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        account (find-account-by-keycard-instance-uid db keycard-instance-uid)
        account-mismatch? (if account-was-manually-selected?
                            (not= account-instance-uid keycard-instance-uid)
                            (nil? account))
        pairing (:keycard-pairing account)]

    (cond
      (empty? keycard-instance-uid)
      (fx/merge cofx
                {:utils/show-popup {:title   (i18n/label :t/no-account-on-card)
                                    :content (i18n/label :t/no-account-on-card-text)}}
                (navigation/navigate-to-cofx :accounts nil))

      account-mismatch?
      (fx/merge cofx
                {:db               (dissoc db :accounts/login)
                 :utils/show-popup {:title   (i18n/label (if auto-login? :t/account-not-listed :t/wrong-card))
                                    :content (i18n/label (if auto-login? :t/account-not-listed-text :t/wrong-card-text))}}
                (navigation/navigate-to-cofx :accounts nil))

      (empty? pairing)
      {:utils/show-popup {:title (i18n/label :t/error)
                          :content (i18n/label :t/no-pairing-on-device)}}

      auto-login?
      (fx/merge cofx
                {:db (-> db
                         (assoc :accounts/login (select-keys account [:address :name :photo-path]))
                         (assoc-in [:hardwallet :pin :enter-step] :login))}
                (navigation/navigate-to-cofx :enter-pin nil))

      :else
      (get-keys-from-keycard cofx))))

(fx/defn clear-on-card-read
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :on-card-read] nil)})

(fx/defn dispatch-event
  [_ event]
  {:dispatch [event]})

(fx/defn show-wrong-keycard-alert
  [_ card-connected?]
  (when card-connected?
    {:utils/show-popup {:title   (i18n/label :t/wrong-card)
                        :content (i18n/label :t/wrong-card-text)}}))

(fx/defn on-get-application-info-success
  [{:keys [db] :as cofx} info on-success]
  (let [info' (js->clj info :keywordize-keys true)
        {:keys [pin-retry-counter puk-retry-counter instance-uid]} info'
        connect-screen? (= (:view-id db) :hardwallet-connect)
        {:keys [card-state on-card-read]} (:hardwallet db)
        on-success' (or on-success on-card-read)
        accounts-screen? (= :accounts (:view-id db))
        auto-login? (and accounts-screen?
                         (not= on-success :hardwallet/auto-login))
        setup-starting? (= :begin (get-in db [:hardwallet :setup-step]))
        enter-step (if (zero? pin-retry-counter)
                     :puk
                     (get-in db [:hardwallet :pin :enter-step]))]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :pin :enter-step] enter-step)
                       (assoc-in [:hardwallet :application-info] info')
                       (assoc-in [:hardwallet :application-info :applet-installed?] true)
                       (assoc-in [:hardwallet :application-info-error] nil))}
              (when auto-login?
                (login-with-keycard true))
              (when-not connect-screen?
                (clear-on-card-read))
              (when setup-starting?
                (check-card-state))
              (if (zero? puk-retry-counter)
                {:utils/show-popup {:title   (i18n/label :t/error)
                                    :content (i18n/label :t/keycard-blocked)}}
                (when on-success'
                  (dispatch-event on-success'))))))

(fx/defn on-get-application-info-error
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] application info error " error)
  (let [on-card-read (get-in db [:hardwallet :on-card-read])
        on-card-connected (get-in db [:hardwallet :on-card-connected])
        connect-screen? (= (:view-id db) :hardwallet-connect)
        login? (= on-card-read :hardwallet/login-with-keycard)]
    (if login?
      (fx/merge cofx
                {:utils/show-popup {:title   (i18n/label :t/wrong-card)
                                    :content (i18n/label :t/wrong-card-text)}}
                (clear-on-card-read)
                (navigation/navigate-to-cofx :accounts nil))
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :application-info-error] error)}
                (when (= on-card-connected :hardwallet/prepare-to-sign)
                  (show-wrong-keycard-alert true))
                (when-not connect-screen?
                  (clear-on-card-read))
                (when on-card-read
                  (dispatch-event on-card-read))))))

(fx/defn set-nfc-support
  [{:keys [db]} supported?]
  {:db (assoc-in db [:hardwallet :nfc-supported?] supported?)})

(fx/defn set-nfc-enabled
  [{:keys [db]} enabled?]
  {:db (assoc-in db [:hardwallet :nfc-enabled?] enabled?)})

(fx/defn status-hardwallet-option-pressed [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:hardwallet/check-nfc-enabled    nil
             :hardwallet/register-card-events nil
             :db                              (-> db
                                                  (assoc-in [:hardwallet :setup-step] :begin)
                                                  (assoc-in [:hardwallet :on-card-connected] nil)
                                                  (assoc-in [:hardwallet :on-card-read] :hardwallet/check-card-state)
                                                  (assoc-in [:hardwallet :pin :on-verified] nil))}
            (navigation/navigate-to-cofx :hardwallet-connect nil)))

(fx/defn success-button-pressed [cofx]
  (navigation/navigate-to-cofx cofx :home nil))

(fx/defn change-pin-pressed
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pin-retry-counter (get-in db [:hardwallet :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/navigate-to-enter-pin-screen)
                       (assoc-in [:hardwallet :pin] {:enter-step   enter-step
                                                     :current      []
                                                     :puk          []
                                                     :original     []
                                                     :confirmation []
                                                     :status       nil
                                                     :error-label  nil
                                                     :on-verified  :hardwallet/proceed-to-change-pin}))}
              (if card-connected?
                (navigate-to-enter-pin-screen)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn proceed-to-change-pin
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:hardwallet :pin :enter-step] :original)
           (assoc-in [:hardwallet :pin :status] nil))})

(fx/defn unpair-card-pressed
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/unpair-card)
                          :content             (i18n/label :t/unpair-card-confirmation)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [:keycard-settings.ui/unpair-card-confirmed])
                          :on-cancel           #()}})

(fx/defn unpair-card-confirmed
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pin-retry-counter (get-in db [:hardwallet :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/navigate-to-enter-pin-screen)
                       (assoc-in [:hardwallet :pin] {:enter-step  enter-step
                                                     :current     []
                                                     :puk         []
                                                     :status      nil
                                                     :error-label nil
                                                     :on-verified :hardwallet/unpair}))}
              (if card-connected?
                (navigate-to-enter-pin-screen)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(defn- vector->string [v]
  "Converts numbers stored in vector into string,
  e.g. [1 2 3 4 5 6] -> \"123456\""
  (apply str v))

(fx/defn unpair
  [{:keys [db]}]
  (let [pin (vector->string (get-in db [:hardwallet :pin :current]))
        pairing (get-pairing db)]
    {:hardwallet/unpair {:pin     pin
                         :pairing pairing}}))

(fx/defn unpair-and-delete
  [{:keys [db]}]
  (let [pin (vector->string (get-in db [:hardwallet :pin :current]))
        pairing (get-pairing db)]
    {:hardwallet/unpair-and-delete {:pin     pin
                                    :pairing pairing}}))

(fx/defn on-delete-success
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                              (-> db
                                                  (assoc-in [:hardwallet :secrets] nil)
                                                  (assoc-in [:hardwallet :application-info] nil)
                                                  (assoc-in [:hardwallet :on-card-connected] nil)
                                                  (assoc-in [:hardwallet :pin] {:status      nil
                                                                                :error-label nil
                                                                                :on-verified nil}))
             :utils/show-popup                {:title   ""
                                               :content (i18n/label :t/card-reseted)}}
            (remove-pairing-from-account {:remove-instance-uid? true})
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn on-delete-error
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] delete error" error)
  (fx/merge cofx
            {:db                              (-> db
                                                  (assoc-in [:hardwallet :on-card-connected] nil)
                                                  (assoc-in [:hardwallet :pin] {:status      nil
                                                                                :error-label nil
                                                                                :on-verified nil}))
             :hardwallet/get-application-info nil
             :utils/show-popup                {:title   ""
                                               :content (i18n/label :t/something-went-wrong)}}
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn reset-card-pressed
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (if card-connected?
      (navigation/navigate-to-cofx cofx :reset-card nil)
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] :hardwallet/navigate-to-reset-card-screen)}
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn delete-card
  [{:keys [db] :as cofx}]
  (let [keycard-instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        account-instance-uid (get-in db [:account/account :keycard-instance-uid])]
    (if (or (nil? account-instance-uid)
            (and keycard-instance-uid
                 (= keycard-instance-uid account-instance-uid)))
      {:hardwallet/delete nil}
      (unauthorized-operation cofx))))

(fx/defn navigate-to-reset-card-screen
  [cofx]
  (navigation/navigate-to-cofx cofx :reset-card nil))

(fx/defn reset-card-next-button-pressed
  [{:keys [db]}]
  {:db       (assoc-in db [:hardwallet :reset-card :disabled?] true)
   :dispatch [:hardwallet/proceed-to-reset-card]})

(fx/defn proceed-to-reset-card
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pin-retry-counter (get-in db [:hardwallet :application-info :pin-retry-counter])
        enter-step (if (zero? pin-retry-counter) :puk :current)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/navigate-to-enter-pin-screen)
                       (assoc-in [:hardwallet :pin] {:enter-step  enter-step
                                                     :current     []
                                                     :puk         []
                                                     :status      nil
                                                     :error-label nil
                                                     :on-verified :hardwallet/unpair-and-delete}))}
              (if card-connected?
                (navigate-to-enter-pin-screen)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn error-button-pressed
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        on-card-connected (get-in db [:hardwallet :on-card-connected])]
    (if card-connected?
      {:dispatch [on-card-connected]}
      (navigation/navigate-to-cofx cofx :hardwallet-connect nil))))

(fx/defn load-pairing-screen
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :setup-step] :pairing)
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/load-pairing-screen))}
              (if card-connected?
                (dispatch-event :hardwallet/pair)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn pair [cofx]
  (let [{:keys [password]} (get-in cofx [:db :hardwallet :secrets])]
    {:hardwallet/pair {:password password}}))

(fx/defn return-back-from-nfc-settings [{:keys [db]}]
  (when (= :hardwallet-connect (:view-id db))
    {:hardwallet/check-nfc-enabled nil}))

(defn- proceed-to-pin-confirmation [fx]
  (assoc-in fx [:db :hardwallet :pin :enter-step] :confirmation))

(fx/defn load-preparing-screen
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :setup-step] :preparing)
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/load-preparing-screen))}
              (if card-connected?
                (dispatch-event :hardwallet/start-installation)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn pin-match
  [{:keys [db] :as fx}]
  (let [pairing (get-pairing db)
        new-pin (vector->string (get-in db [:hardwallet :pin :original]))
        current-pin (vector->string (get-in db [:hardwallet :pin :current]))
        setup-step (get-in db [:hardwallet :setup-step])]
    (if (= setup-step :pin)
      (load-preparing-screen fx)
      (fx/merge fx
                {:db                    (assoc-in db [:hardwallet :pin :status] :verifying)
                 :hardwallet/change-pin {:new-pin     new-pin
                                         :current-pin current-pin
                                         :pairing     pairing}}))))

(fx/defn dispatch-on-verified-event
  [{:keys [db]} event]
  {:dispatch [event]
   :db       (assoc-in db [:hardwallet :pin :on-verified] nil)})

(fx/defn on-unblock-pin-success
  [{:keys [db] :as cofx}]
  (let [pairing (get-pairing db)]
    (fx/merge cofx
              {:hardwallet/get-application-info {:pairing pairing}
               :db                              (-> db
                                                    (update-in [:hardwallet :pin] merge {:status      nil
                                                                                         :enter-step  :original
                                                                                         :current     [0 0 0 0 0 0]
                                                                                         :puk         []
                                                                                         :error-label nil}))}
              (navigation/navigate-to-cofx :enter-pin nil))))

(defn on-unblock-pin-error
  [{:keys [db]} error]
  (let [pairing (get-pairing db)]
    (log/debug "[hardwallet] unblock pin error" error)
    {:hardwallet/get-application-info {:pairing pairing}
     :db                              (update-in db [:hardwallet :pin] merge {:status      :error
                                                                              :error-label :t/puk-mismatch
                                                                              :enter-step  :puk
                                                                              :puk         []})}))
(fx/defn get-application-info
  [{:keys [db]} pairing on-card-read]
  (let [instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        pairing' (or pairing
                     (when instance-uid
                       (get-pairing db instance-uid)))]
    {:hardwallet/get-application-info {:pairing    pairing'
                                       :on-success on-card-read}}))

(fx/defn on-verify-pin-success
  [{:keys [db] :as cofx}]
  (let [on-verified (get-in db [:hardwallet :pin :on-verified])
        pairing (get-pairing db)]
    (fx/merge cofx
              {:db                              (-> db
                                                    (update-in [:hardwallet :pin] merge {:status      nil
                                                                                         :error-label nil}))}
              (when-not (contains? #{:hardwallet/unpair :hardwallet/unpair-and-delete} on-verified)
                (get-application-info pairing nil))
              (when on-verified
                (dispatch-on-verified-event on-verified)))))

(defn on-verify-pin-error
  [{:keys [db]} error]
  (let [pairing (get-pairing db)]
    (log/debug "[hardwallet] verify pin error" error)
    {:hardwallet/get-application-info {:pairing pairing}
     :db                              (update-in db [:hardwallet :pin] merge {:status       :error
                                                                              :error-label  :t/pin-mismatch
                                                                              :enter-step   :current
                                                                              :puk          []
                                                                              :current      []
                                                                              :original     []
                                                                              :confirmation []})}))

(fx/defn on-change-pin-success
  [{:keys [db] :as cofx}]
  (let [pin (vector->string (get-in db [:hardwallet :pin :original]))]
    (fx/merge cofx
              {:db               (-> db
                                     (assoc-in [:hardwallet :on-card-connected] nil)
                                     (assoc-in [:hardwallet :pin] {:status      nil
                                                                   :error-label nil}))
               :utils/show-popup {:title   ""
                                  :content (i18n/label :t/pin-changed {:pin pin})}}
              (navigation/navigate-back))))

(fx/defn on-change-pin-error
  [{:keys [db]} error]
  (log/debug "[hardwallet] change pin error" error)
  {:db (update-in db [:hardwallet :pin] merge {:status       :error
                                               :error-label  :t/pin-mismatch
                                               :enter-step   :original
                                               :puk          []
                                               :confirmation []
                                               :original     []})})

(fx/defn on-unpair-success
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db               (-> db
                                   (assoc-in [:hardwallet :secrets] nil)
                                   (assoc-in [:hardwallet :on-card-connected] nil)
                                   (assoc-in [:hardwallet :pin] {:status      nil
                                                                 :error-label nil
                                                                 :on-verified nil}))
             :utils/show-popup {:title   ""
                                :content (i18n/label :t/card-unpaired)}}
            (remove-pairing-from-account nil)
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn on-unpair-error
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] unpair error" error)
  (fx/merge cofx
            {:db                              (-> db
                                                  (assoc-in [:hardwallet :on-card-connected] nil)
                                                  (assoc-in [:hardwallet :pin] {:status      nil
                                                                                :error-label nil
                                                                                :on-verified nil}))
             :hardwallet/get-application-info nil
             :utils/show-popup                {:title   ""
                                               :content (i18n/label :t/something-went-wrong)}}
            (navigation/navigate-to-cofx :keycard-settings nil)))

(defn- verify-pin
  [{:keys [db] :as fx}]
  (let [pin (vector->string (get-in fx [:db :hardwallet :pin :current]))
        pairing (get-pairing db)]
    {:db                    (assoc-in db [:hardwallet :pin :status] :verifying)
     :hardwallet/verify-pin {:pin     pin
                             :pairing pairing}}))

(defn- unblock-pin
  [{:keys [db] :as cofx}]
  (let [puk (vector->string (get-in cofx [:db :hardwallet :pin :puk]))
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        card-connected? (get-in db [:hardwallet :card-connected?])
        pairing (get-pairing db instance-uid)]
    (if card-connected?
      {:db                     (assoc-in db [:hardwallet :pin :status] :verifying)
       :hardwallet/unblock-pin {:puk     puk
                                :new-pin default-pin
                                :pairing pairing}}
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] :hardwallet/unblock-pin)}
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(def pin-code-length 6)
(def puk-code-length 12)

(fx/defn handle-pin-input
  [{:keys [db]} enter-step]
  (let [numbers-entered (count (get-in db [:hardwallet :pin enter-step]))]
    (when (or (= numbers-entered pin-code-length)
              (= numbers-entered puk-code-length))
      {:dispatch [:hardwallet/process-pin-input]})))

(fx/defn update-pin
  [{:keys [db] :as cofx} number enter-step]
  (let [numbers-entered (count (get-in db [:hardwallet :pin enter-step]))
        need-update? (if (= enter-step :puk)
                       (< numbers-entered puk-code-length)
                       (< numbers-entered pin-code-length))]
    (fx/merge cofx
              {:db (cond-> (assoc-in db [:hardwallet :pin :status] nil)
                     need-update? (update-in [:hardwallet :pin enter-step] (fnil conj []) number))}
              (when need-update?
                (handle-pin-input enter-step)))))

(defn- pin-enter-error [fx error-label]
  (update-in fx [:db :hardwallet :pin] merge {:status       :error
                                              :error-label  error-label
                                              :enter-step   :original
                                              :original     []
                                              :confirmation []}))

(fx/defn wait-for-card-tap
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (if card-connected?
      (login-with-keycard cofx false)
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:hardwallet :on-card-read] :hardwallet/login-with-keycard))}
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn sign
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pairing (get-pairing db)
        account-keycard-instance-uid (get-in db [:account/account :keycard-instance-uid])
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        keycard-match? (= account-keycard-instance-uid instance-uid)
        hash (get-in db [:hardwallet :hash])
        pin (vector->string (get-in db [:hardwallet :pin :sign]))]
    (if (and card-connected?
             keycard-match?)
      {:db              (-> db
                            (assoc-in [:hardwallet :card-read-in-progress?] true)
                            (assoc-in [:hardwallet :pin :status] :verifying))
       :hardwallet/sign {:hash    (ethereum/naked-address hash)
                         :pairing pairing
                         :pin     pin}}
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] :hardwallet/sign)}
                (when-not keycard-match?
                  (show-wrong-keycard-alert card-connected?))
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn prepare-to-sign
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])
        pairing (get-pairing db)]
    (if card-connected?
      (get-application-info cofx pairing :hardwallet/sign)
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :on-card-connected] :hardwallet/prepare-to-sign)}
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

; PIN enter steps:
; login - PIN is used to login
; sign - PIN for transaction sign
; current - current PIN to perform actions which require PIN auth
; original - new PIN when user changes it or creates new one
; confirmation - confirmation for new PIN
(fx/defn process-pin-input
  [{:keys [db]}]
  (let [enter-step (get-in db [:hardwallet :pin :enter-step])
        pin (get-in db [:hardwallet :pin enter-step])
        numbers-entered (count pin)]
    (cond-> {:db (assoc-in db [:hardwallet :pin :status] nil)}

      (and (= enter-step :login)
           (= 6 numbers-entered))
      (wait-for-card-tap)

      (and (= enter-step :original)
           (= pin-code-length numbers-entered))
      (proceed-to-pin-confirmation)

      (and (= enter-step :original)
           (= pin-code-length numbers-entered)
           (= default-pin (vector->string pin)))
      (pin-enter-error :t/cannot-use-default-pin)

      (and (= enter-step :current)
           (= pin-code-length numbers-entered))
      (verify-pin)

      (and (= enter-step :sign)
           (= pin-code-length numbers-entered))
      (prepare-to-sign)

      (and (= enter-step :puk)
           (= puk-code-length numbers-entered))
      (unblock-pin)

      (and (= enter-step :confirmation)
           (= (get-in db [:hardwallet :pin :original])
              (get-in db [:hardwallet :pin :confirmation])))
      (pin-match)

      (and (= enter-step :confirmation)
           (= pin-code-length numbers-entered)
           (not= (get-in db [:hardwallet :pin :original])
                 (get-in db [:hardwallet :pin :confirmation])))
      (pin-enter-error :t/pin-mismatch))))

(fx/defn load-loading-keys-screen
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :setup-step] :loading-keys)
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/load-loading-keys-screen))}
              (if card-connected?
                (dispatch-event :hardwallet/generate-and-load-key)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn load-generating-mnemonic-screen
  [{:keys [db] :as cofx}]
  (let [card-connected? (get-in db [:hardwallet :card-connected?])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :setup-step] :generating-mnemonic)
                       (assoc-in [:hardwallet :on-card-connected] :hardwallet/load-generating-mnemonic-screen))}
              (if card-connected?
                (dispatch-event :hardwallet/generate-mnemonic)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn generate-mnemonic
  [cofx]
  (let [{:keys [pairing]} (get-in cofx [:db :hardwallet :secrets])]
    {:hardwallet/generate-mnemonic {:pairing pairing
                                    :words   (string/join "\n" mnemonic/dictionary)}}))

(fx/defn on-card-connected
  [{:keys [db] :as cofx} _]
  (log/debug "[hardwallet] card connected")
  (let [pin-enter-step (get-in db [:hardwallet :pin :enter-step])
        setup-running? (boolean (get-in db [:hardwallet :setup-step]))
        login? (= :login pin-enter-step)
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        accounts-screen? (= :accounts (:view-id db))
        auto-login? (and accounts-screen? instance-uid)
        should-read-instance-uid? (nil? instance-uid)
        on-card-connected (get-in db [:hardwallet :on-card-connected])
        on-card-read (cond
                       should-read-instance-uid? :hardwallet/get-application-info
                       auto-login? :hardwallet/auto-login
                       :else (get-in db [:hardwallet :on-card-read]))
        pairing (get-pairing db instance-uid)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :card-connected?] true)
                       (assoc-in [:hardwallet :card-read-in-progress?] (boolean on-card-read)))}
              (when (not= on-card-connected :hardwallet/sign)
                (get-application-info pairing on-card-read))
              (when (and on-card-connected
                         (not login?))
                (dispatch-event on-card-connected))
              (when setup-running?
                (navigation/navigate-to-cofx :hardwallet-setup nil)))))

(fx/defn on-card-disconnected
  [{:keys [db] :as cofx} _]
  (log/debug "[hardwallet] card disconnected ")
  (let [setup-running? (get-in db [:hardwallet :setup-step])
        on-card-connected (get-in db [:hardwallet :on-card-connected])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :card-connected?] false)
                       (assoc-in [:hardwallet :card-read-in-progress?] false))}
              (when (and setup-running?
                         on-card-connected)
                (navigation/navigate-to-cofx :hardwallet-connect nil)))))

(fx/defn begin-setup-button-pressed
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:hardwallet :setup-step] :pin)
           (assoc-in [:hardwallet :pin :enter-step] :original)
           (assoc-in [:hardwallet :pin :original] [])
           (assoc-in [:hardwallet :pin :confirmation] []))})

(fx/defn start-installation
  [{:keys [db] :as cofx}]
  (let [card-state (get-in db [:hardwallet :card-state])
        pin (vector->string (get-in db [:hardwallet :pin :original]))]
    (case card-state

      :pre-init
      {:hardwallet/init-card pin}

      (do
        (log/debug (str "Cannot start keycard installation from state: " card-state))
        (fx/merge cofx
                  {:utils/show-popup {:title   (i18n/label :t/error)
                                      :content (i18n/label :t/something-went-wrong)}}
                  (navigation/navigate-to-cofx :hardwallet-authentication-method nil))))))

(fx/defn on-install-applet-and-init-card-success
  [{:keys [db]} secrets]
  (let [secrets' (js->clj secrets :keywordize-keys true)]
    {:hardwallet/get-application-info nil
     :db                              (-> db
                                          (assoc-in [:hardwallet :on-card-connected] nil)
                                          (assoc-in [:hardwallet :setup-step] :secret-keys)
                                          (assoc-in [:hardwallet :secrets] secrets'))}))

(def on-init-card-success on-install-applet-and-init-card-success)

(defn- tag-lost-exception? [code]
  (= code "android.nfc.TagLostException"))

(fx/defn process-error [{:keys [db] :as cofx} code]
  (if (tag-lost-exception? code)
    (navigation/navigate-to-cofx cofx :hardwallet-connect nil)
    {:db (assoc-in db [:hardwallet :setup-step] :error)}))

(fx/defn on-install-applet-and-init-card-error
  [{:keys [db] :as cofx} {:keys [code error]}]
  (log/debug "[hardwallet] install applet and init card error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :setup-error] error)}
            (process-error code)))

(def on-init-card-error on-install-applet-and-init-card-error)

(fx/defn set-account-pairing
  [{:keys [db]} {:keys [address] :as account} pairing paired-on]
  (let [account' (assoc account :keycard-pairing pairing
                        :keycard-paired-on paired-on)]
    {:db                 (-> db
                             (assoc :accounts/account account')
                             (assoc-in [:accounts/accounts address] account'))
     :data-store/base-tx [(accounts-store/save-account-tx account')]}))

(fx/defn on-pairing-success
  [{:keys [db] :as cofx} pairing]
  (let [setup-step (get-in db [:hardwallet :setup-step])
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        account (find-account-by-keycard-instance-uid db instance-uid)
        paired-on (utils.datetime/timestamp)
        next-step (if (= setup-step :enter-pair-code)
                    :begin
                    :card-ready)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :application-info :paired?] true)
                       (assoc-in [:hardwallet :on-card-connected] nil)
                       (assoc-in [:hardwallet :setup-step] next-step)
                       (assoc-in [:hardwallet :secrets :pairing] pairing)
                       (assoc-in [:hardwallet :secrets :paired-on] paired-on))}
              (when account
                (set-account-pairing account pairing paired-on))
              (when (= next-step :begin)
                (check-card-state)))))

(fx/defn on-pairing-error
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] pairing error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :setup-error] error)}
            (process-error code)))

(fx/defn on-generate-mnemonic-success
  [{:keys [db]} mnemonic]
  {:db (-> db
           (assoc-in [:hardwallet :setup-step] :recovery-phrase)
           (assoc-in [:hardwallet :on-card-connected] nil)
           (assoc-in [:hardwallet :secrets :mnemonic] mnemonic))})

(fx/defn on-generate-mnemonic-error
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] generate mnemonic error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :setup-error] error)}
            (process-error code)))

(fx/defn recovery-phrase-start-confirmation [{:keys [db]}]
  (let [mnemonic (get-in db [:hardwallet :secrets :mnemonic])
        [word1 word2] (shuffle (map-indexed vector (clojure.string/split mnemonic #" ")))
        word1 (zipmap [:idx :word] word1)
        word2 (zipmap [:idx :word] word2)]
    {:db (-> db
             (assoc-in [:hardwallet :setup-step] :recovery-phrase-confirm-word1)
             (assoc-in [:hardwallet :recovery-phrase :step] :word1)
             (assoc-in [:hardwallet :recovery-phrase :confirm-error] nil)
             (assoc-in [:hardwallet :recovery-phrase :input-word] nil)
             (assoc-in [:hardwallet :recovery-phrase :word1] word1)
             (assoc-in [:hardwallet :recovery-phrase :word2] word2))}))

(defn- show-recover-confirmation []
  {:ui/show-confirmation {:title               (i18n/label :t/are-you-sure?)
                          :content             (i18n/label :t/are-you-sure-description)
                          :confirm-button-text (clojure.string/upper-case (i18n/label :t/yes))
                          :cancel-button-text  (i18n/label :t/see-it-again)
                          :on-accept           #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-confirm-pressed])
                          :on-cancel           #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-cancel-pressed])}})

(defn- recovery-phrase-next-word [db]
  {:db (-> db
           (assoc-in [:hardwallet :recovery-phrase :step] :word2)
           (assoc-in [:hardwallet :recovery-phrase :confirm-error] nil)
           (assoc-in [:hardwallet :recovery-phrase :input-word] nil)
           (assoc-in [:hardwallet :setup-step] :recovery-phrase-confirm-word2))})

(fx/defn recovery-phrase-confirm-word
  [{:keys [db]}]
  (let [step (get-in db [:hardwallet :recovery-phrase :step])
        input-word (get-in db [:hardwallet :recovery-phrase :input-word])
        {:keys [word]} (get-in db [:hardwallet :recovery-phrase step])]
    (if (= word input-word)
      (if (= step :word1)
        (recovery-phrase-next-word db)
        (show-recover-confirmation))
      {:db (assoc-in db [:hardwallet :recovery-phrase :confirm-error] (i18n/label :t/wrong-word))})))

(fx/defn card-ready-next-button-pressed
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:hardwallet :flow]) :create)
    (load-generating-mnemonic-screen cofx)
    {:db (assoc-in db [:hardwallet :setup-step] :recovery-phrase)}))

(fx/defn recovery-phrase-next-button-pressed
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:hardwallet :flow]) :create)
    (recovery-phrase-start-confirmation cofx)
    (let [mnemonic (get-in db [:accounts/recover :passphrase])]
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :secrets :mnemonic] mnemonic)}
                (load-loading-keys-screen)))))

(fx/defn generate-and-load-key
  [{:keys [db] :as cofx}]
  (let [{:keys [mnemonic pairing]} (get-in db [:hardwallet :secrets])
        pin (vector->string (get-in db [:hardwallet :pin :original]))]
    (fx/merge cofx
              {:hardwallet/generate-and-load-key {:mnemonic mnemonic
                                                  :pairing  pairing
                                                  :pin      pin}})))

(fx/defn create-keycard-account
  [{:keys [db] :as cofx}]
  (let [{{:keys [whisper-public-key
                 wallet-address
                 encryption-public-key
                 keycard-instance-uid
                 secrets]} :hardwallet} db
        {:keys [pairing paired-on]} secrets]
    (fx/merge (-> cofx
                  (accounts.create/get-signing-phrase)
                  (accounts.create/get-status))
              {:db (assoc-in db [:hardwallet :setup-step] nil)}
              (accounts.create/on-account-created {:pubkey               whisper-public-key
                                                   :address              wallet-address
                                                   :mnemonic             ""
                                                   :keycard-instance-uid keycard-instance-uid
                                                   :keycard-pairing      pairing
                                                   :keycard-paired-on    paired-on}
                                                  encryption-public-key
                                                  {:seed-backed-up? true
                                                   :login?          true})
              (navigation/navigate-to-cofx :hardwallet-success nil))))

(fx/defn on-generate-and-load-key-success
  [{:keys [db random-guid-generator] :as cofx} data]
  (let [{:keys [whisper-public-key
                whisper-private-key
                whisper-address
                wallet-address
                instance-uid
                encryption-public-key]} (js->clj data :keywordize-keys true)
        whisper-public-key' (str "0x" whisper-public-key)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :whisper-public-key] whisper-public-key')
                       (assoc-in [:hardwallet :whisper-private-key] whisper-private-key)
                       (assoc-in [:hardwallet :whisper-address] whisper-address)
                       (assoc-in [:hardwallet :wallet-address] wallet-address)
                       (assoc-in [:hardwallet :encryption-public-key] encryption-public-key)
                       (assoc-in [:hardwallet :keycard-instance-uid] instance-uid)
                       (assoc-in [:hardwallet :on-card-connected] nil)
                       (update :hardwallet dissoc :recovery-phrase)
                       (update-in [:hardwallet :secrets] dissoc :pin :puk :password)
                       (assoc :node/on-ready :create-keycard-account)
                       (assoc :accounts/new-installation-id (random-guid-generator))
                       (update-in [:hardwallet :secrets] dissoc :mnemonic))}
              (node/initialize nil))))

(fx/defn on-generate-and-load-key-error
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] generate and load key error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :setup-error] error)}
            (process-error code)))

(fx/defn on-get-keys-success
  [{:keys [db] :as cofx} data]
  (let [{:keys [whisper-public-key
                whisper-private-key
                wallet-address
                encryption-public-key]} (js->clj data :keywordize-keys true)
        whisper-public-key' (str "0x" whisper-public-key)
        {:keys [photo-path name]} (get-in db [:accounts/accounts wallet-address])
        password encryption-public-key
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])]
    (fx/merge cofx
              {:db                              (-> db
                                                    (assoc-in [:hardwallet :pin :status] nil)
                                                    (assoc-in [:hardwallet :pin :login] [])
                                                    (assoc-in [:hardwallet :whisper-public-key] whisper-public-key')
                                                    (assoc-in [:hardwallet :whisper-private-key] whisper-private-key)
                                                    (assoc-in [:hardwallet :wallet-address] wallet-address)
                                                    (assoc-in [:hardwallet :encryption-public-key] encryption-public-key)
                                                    (update :accounts/login assoc
                                                            :password password
                                                            :address wallet-address
                                                            :photo-path photo-path
                                                            :name name))
               :hardwallet/get-application-info {:pairing (get-pairing db instance-uid)}}
              (accounts.login/user-login true))))

(fx/defn on-get-keys-error
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] get keys error: " error)
  (let [tag-was-lost? (= "Tag was lost." (:error error))
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])]
    (if tag-was-lost?
      {:utils/show-popup {:title   (i18n/label :t/error)
                          :content (i18n/label :t/tag-was-lost)}}
      (fx/merge cofx
                {:hardwallet/get-application-info {:pairing (get-pairing db instance-uid)}
                 :db                              (update-in db [:hardwallet :pin] merge {:status      :error
                                                                                          :login       []
                                                                                          :error-label :t/pin-mismatch})}
                (navigation/navigate-to-cofx :enter-pin nil)))))

(fx/defn send-transaction-with-signature
  [_ data]
  {:send-transaction-with-signature data})

(fx/defn sign-message-completed
  [{:keys [db]} signature]
  (let [screen-params (get-in db [:navigation/screen-params :wallet-sign-message-modal])
        signature' (-> signature
                       ; add 27 to last byte
                       ; https://github.com/ethereum/go-ethereum/blob/master/internal/ethapi/api.go#L431
                       (clojure.string/replace-first #"00$", "1b")
                       (clojure.string/replace-first #"01$", "1c")
                       (ethereum/normalized-address))]
    {:dispatch
     [:status-im.ui.screens.wallet.send.events/sign-message-completed
      screen-params
      {:result signature'}]}))

(fx/defn on-sign-success
  [{:keys [db] :as cofx} signature]
  (log/debug "[hardwallet] sign success: " signature)
  (let [transaction (get-in db [:hardwallet :transaction])]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :pin :sign] [])
                       (assoc-in [:hardwallet :pin :status] nil)
                       (assoc-in [:hardwallet :on-card-connected] nil)
                       (assoc-in [:hardwallet :hash] nil)
                       (assoc-in [:hardwallet :transaction] nil))}
              (get-application-info (get-pairing db) nil)
              (if transaction
                (send-transaction-with-signature {:transaction  (types/clj->json transaction)
                                                  :signature    signature
                                                  :on-completed #(re-frame/dispatch [:wallet.callback/transaction-completed (types/json->clj %)])})
                (sign-message-completed signature)))))

(def pin-mismatch-error #"Unexpected error SW, 0x63C\d+")

(fx/defn on-sign-error
  [{:keys [db] :as cofx} error]
  (log/debug "[hardwallet] sign error: " error)
  (if (re-matches pin-mismatch-error (:error error))
    (fx/merge cofx
              {:db (update-in db [:hardwallet :pin] merge {:status      :error
                                                           :sign        []
                                                           :error-label :t/pin-mismatch})}
              (navigation/navigate-to-cofx :enter-pin nil)
              (get-application-info (get-pairing db) nil))
    (show-wrong-keycard-alert cofx true)))
