(ns status-im.add-new.core-test
  (:require [cljs.test :refer-macros [deftest testing is run-tests]]
            [utils.re-frame :as rf]
            [status-im.add-new.core :as core]))

;; As a user
;; I would like to add a contact by typing their chat key
;; I would like to add a contact by typing their ENS
;; Description

;; We should only support compressed keys, and
;; https://join.status.im/u/{key/ens-name} links.

;; If the key is valid, or the ENS name resolves, we should enable the View
;; Profile button, which if clicked, will take you to the user's profile.

;; initial db values
(def init-db {:networks/current-network "mainnet_rpc"
              :networks/networks        {"mainnet_rpc"
                                         {:id     "mainnet_rpc"
                                          :config {:NetworkId 1}}}})

;; we can't compare callback fn objects, so remove it:
(defn clean [db] (assoc-in db [:resolve-public-key :cb] nil))

;; ;; NOTE: :resolve-public-key is not executed by these tests - they are 
;; ;;   executing the fn directly, instead of through re-frame

;; the first call: a user types "e" for "ENS or chatkey"
(deftest first-call ; -> searching
  (let [inputs    {:new-identity-raw "e"
                   :new-ens-name     nil
                   :id               nil}
        text      (:new-identity-raw inputs)
        ;; the code-under-test will add these to the db
        add-to-db {:contacts/new-identity {:public-key text
                                           :state      :searching
                                           :error      :invalid
                                           :ens-name   nil}}
        ;; the expected db after code-under-test completes
        expected  {:db                 (merge init-db add-to-db)
                   :resolve-public-key {:chain-id         1
                                        :contact-identity text
                                        :cb               nil}}
        ;; the actual return value of code-under-test
        actual    (core/new-chat-set-new-identity
                   {:db init-db} text nil nil)]
    (is (= (clean actual) expected))))

;; the second call is not user-triggered; it would be a recursion from first-call.
(deftest second-call ; -> error
  (let [inputs    {:new-identity-raw "0x"
                   :new-ens-name     "e"
                   :id               "1671911236203-4ba66785-ed56-523a-83dc-4cd4cdd7e3af"}
        text      (:new-identity-raw inputs)
        ens       (:new-ens-name inputs)
        id        (:id inputs)
        add-to-db {:contacts/new-identity {:state :error}}
        expected  {:db (merge init-db add-to-db)}
        _         (reset! core/resolve-last-id id)
        actual    (core/new-chat-set-new-identity
                   {:db init-db} text ens id)]
    (is (= actual expected))))

;; the third call is when a user deletes the previously-entered text
(deftest third-call ; -> invalid
  (let [inputs    {:new-identity-raw ""
                   :new-ens-name     nil
                   :id               nil}
        text      (:new-identity-raw inputs)
        ens       (:new-ens-name inputs)
        id        (:id inputs)
        last-id   "1671911236203-4ba66785-ed56-523a-83dc-4cd4cdd7e3af"
        add-to-db {:contacts/new-identity {:public-key text
                                           :state      :empty
                                           :error      :invalid
                                           :ens-name   nil}}
        expected  {:db (merge init-db add-to-db)}
        _         (reset! core/resolve-last-id last-id) ; previous id exists
        actual    (core/new-chat-set-new-identity
                   {:db init-db} text ens id)]
    (is (= actual expected))))

(deftest fourth-call ; -> valid
  (let [inputs    {:new-identity-raw "0x048a6773339d11ccf5fd81677b7e54daeec544a1287bd92b725047ad6faa9a9b9f8ea86ed5a226d2a994f5f46d0b43321fd8de7b7997a166e67905c8c73cd37cea"
                   :new-ens-name     "esep"
                   :id               "1671917792586-38d2c414-8a94-501d-83f5-11267655bdb3"}
        text      (:new-identity-raw inputs)
        ens       (:new-ens-name inputs)
        id        (:id inputs)
        add-to-db {:contacts/new-identity {:public-key text
                                           :state      :valid
                                           :error      nil
                                           :ens-name   "esep.stateofus.eth"}}
        expected  {:db (merge init-db add-to-db)}
        _         (reset! core/resolve-last-id id) ; same id
        actual    (core/new-chat-set-new-identity
                   {:db init-db} text ens id)]
    ;; (println "actual:" actual)
    ;; (println "expected:" expected)
    (is (= actual expected))))

(deftest fifth-call ; -> valid
  (let [inputs    {:new-identity-raw "0x048a6773339d11ccf5fd81677b7e54daeec544a1287bd92b725047ad6faa9a9b9f8ea86ed5a226d2a994f5f46d0b43321fd8de7b7997a166e67905c8c73cd37cea"
                   :new-ens-name     "esep"
                   :id               "1671917792586-38d2c414-8a94-501d-83f5-11267655bdb3"}
        text      (:new-identity-raw inputs)
        ens       (:new-ens-name inputs)
        id        (:id inputs)
        add-to-db {:contacts/new-identity {:public-key text
                                           :state      :valid
                                           :error      nil
                                           :ens-name   "esep.stateofus.eth"}}
        expected  {:db (merge init-db add-to-db)}
        _         (reset! core/resolve-last-id id) ; same id
        actual    (core/new-chat-set-new-identity
                   {:db init-db} text ens id)]
    ;; (println "actual:" actual)
    ;; (println "expected:" expected)
    (is (= actual expected))))

(comment
  (run-tests))
