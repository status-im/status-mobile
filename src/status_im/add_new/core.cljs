(ns status-im.add-new.core
  (:require [clojure.string :as string]
            [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im.add-new.db :as db]
            [status-im.chat.models :as chat]
            [status-im.contact.core :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.native-module.core :as status]
            [status-im.i18n.i18n :as i18n]
            [status-im.router.core :as router]
            [status-im.utils.db :as utils.db]
            [status-im.utils.random :as random]
            [status-im.utils.utils :as utils]
            [status-im2.navigation.events :as navigation]))

;; (re-frame/reg-fx
;;  :multiaccount-generate-and-derive-addresses
;;  (fn []
;;    (status/multiaccount-generate-and-derive-addresses
;;     5
;;     12
;;     [constants/path-whisper
;;      constants/path-wallet-root
;;      constants/path-default-wallet]
;;     #(re-frame/dispatch [:multiaccount-generate-and-derive-addresses-success
;;                          (mapv normalize-multiaccount-data-keys
;;                                (types/json->clj %))]))))

;; TODO(esep): this seems like it should be an 'ens_' method, but maybe not... not sure
;; (defn compressed-public-key
;;   [chain-id ens-name cb]
;;   (json-rpc/call {:method     "ens_compressedPublicKeyOf"
;;                   :params     [chain-id ens-name]
;;                   :on-success cb
;;                   :on-error   #(cb "0x")}))

(re-frame/reg-fx
 :resolve-public-key
 (fn [{:keys [chain-id contact-identity cb]}]
   (let [ens-name (stateofus/ens-name-parse contact-identity)]
     (println "ens/pubkey:" chain-id ens-name cb)
     (ens/pubkey chain-id ens-name cb))))

(re-frame/reg-fx
 :resolve-compressed-public-key
 (fn [compressed-public-key]
   (println "resolve-pubkey:" compressed-public-key)
   (status/decompress-public-key compressed-public-key #()
                                 ;; #(re-frame/dispatch [:multiaccount-generate-and-derive-addresses-success
                                 ;;                      (mapv normalize-multiaccount-data-keys
                                 ;;                            (types/json->clj %))])
                                 )))

(comment
  (rf/dispatch [:resolve-public-key "0x04351fb713ee1fd8b15cc70918ccb852176f6f4068af9928051d2f78148cbd850fa6f72550142f2c8195082799d9ef90b226cd4183c67e0ad56c657c211cd83ed6"])
  (rf/dispatch [:resolve-compressed-public-key "zQ3shwiRy5TtMM7B4iZ8MKaGywkaaCgMqqbrnAUYrZJ1sgVWh"])
  )

;; (fx/defn set-new-identity
;;   {:events [:new-chat/set-new-identity]}
;;   [{:keys [db] :as cofx} id ens]
;;   (println cofx id ens)
;;   (status/decompress-public-key
;;    id #()
;;    ;; #(re-frame/dispatch [:multiaccount-generate-and-derive-addresses-success
;;    ;;                      (mapv normalize-multiaccount-data-keys
;;    ;;                            (types/json->clj %))])
;;    ))


;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(def x (atom {}))
(def db2 (atom nil))
;; WEIRD: if user types 'z', it sends z nil nil
(rf/defn new-chat-set-new-identity
  {:events [:new-chat/set-new-identity]}
  [{db :db} new-identity-raw new-ens-name id]
  (println "-----------------------")
  (println "id:" @resolve-last-id)
  (println new-identity-raw new-ens-name id)
  ;; if we're saying it's an ENS, it should not start with 0x (ENS-ERROR!)
  (let [ens-error (and (= new-identity-raw "0x")
                       (not (string/blank? new-ens-name)))]
    ;; proceed only if:
    ;; 1) no id (we haven't attempted to resolve it yet) OR
    ;; 2) id is equal resolve-last-id (we just attempted to resolve it)
    (when (or (not id) (= id @resolve-last-id))
      (println "ens-error:" ens-error)
      (if ens-error
        {:db (assoc-in db [:contacts/new-identity :state] :error)}
        ;; otherwise proceed...
        (let [new-identity   (utils/safe-trim new-identity-raw)
              ;; this checks against a public key regex ("0x04...{128}")
              is-public-key? (utils.db/valid-public-key? new-identity)
              ;; if NOT 0x AND looks like an ens
              is-ens?        (and (not is-public-key?)
                                  ;; is not blank, does not end with '.',
                                  ;;               does not include '..'
                                  (ens/valid-eth-name-prefix? new-identity))
              _              (println "is-ens?" is-ens?)
              error          (db/validate-pub-key db new-identity)
              _              (println "error:" error)
              _              (reset! resolve-last-id nil) ; init before
              a              {:public-key new-identity
                              :state      (cond is-ens?              :searching
                                                (and
                                                 (string/blank? new-identity)
                                                 (not new-ens-name)) :empty
                                                error                :error
                                                :else                :valid)
                              :error      error
                              :ens-name   (stateofus/ens-name-parse
                                           new-ens-name)}
              b              {:resolve-public-key ; -> ens_publicKeyOf
                              {:chain-id         (ethereum/chain-id db)
                               :contact-identity new-identity
                               :cb               #(rf/dispatch
                                                   ;; loop: recurse this fn
                                                   [:new-chat/set-new-identity %
                                                    ;; % is val returned
                                                    new-identity
                                                    @resolve-last-id])}}
              out            (merge {:db (assoc db
                                                ;; just store a map in db,
                                                ;;   no actions taken
                                                :contacts/new-identity a)}
                                    (when is-ens?
                                      ;; generate a new id on every call!
                                      (reset! resolve-last-id (random/id))
                                      ;; {:resolve-compressed-public-key new-identity}
                                      b))
              _              (reset! x {:a a :b b})
              _              (reset! db2 db)
              ;; _              (println out)
              ] out)))))

(rf/defn clear-new-identity
  {:events [::clear-new-identity ::new-chat-focus]}
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})

(rf/defn qr-code-handled
  {:events [::qr-code-handled]}
  [{:keys [db] :as cofx} {:keys [type public-key chat-id data ens-name]}
   {:keys [new-contact? nickname] :as opts}]
  (let [public-key?       (and (string? data)
                               (string/starts-with? data "0x"))
        chat-key          (cond
                            (= type :private-chat) chat-id
                            (= type :contact) public-key
                            (and (= type :undefined)
                                 public-key?)
                            data)
        validation-result (db/validate-pub-key db chat-key)]
    (if-not validation-result
      (if new-contact?
        (rf/merge cofx
                  (contact/add-contact chat-key nickname ens-name)
                  (navigation/navigate-to-cofx :contacts-list {}))
        (chat/start-chat cofx chat-key ens-name))
      {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                          :content    (case validation-result
                                        :invalid
                                        (i18n/label :t/use-valid-contact-code)
                                        :yourself
                                        (i18n/label :t/can-not-add-yourself))
                          :on-dismiss #(re-frame/dispatch [:pop-to-root-tab :chat-stack])}})))

(rf/defn qr-code-scanned
  {:events [:contact/qr-code-scanned]}
  [{:keys [db]} data opts]
  {::router/handle-uri {:chain (ethereum/chain-keyword db)
                        :chats (get db :chats)
                        :uri   data
                        :cb    #(re-frame/dispatch [::qr-code-handled % opts])}})

