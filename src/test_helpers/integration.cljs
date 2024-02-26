(ns test-helpers.integration
  (:require-macros [test-helpers.integration])
  (:require
    [cljs.test :refer [is]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [native-module.core :as native-module]
    [re-frame.core :as rf]
    status-im.contexts.onboarding.events
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [taoensso.timbre :as log]
    [tests.integration-test.constants :as constants]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(defn validate-mnemonic
  [mnemonic on-success on-error]
  (native-module/validate-mnemonic
   (security/safe-unmask-data mnemonic)
   (fn [result]
     (let [{:keys [error keyUID]} (transforms/json->clj result)]
       (if (seq error)
         (when on-error (on-error error))
         (on-success mnemonic keyUID))))))

(defn initialize-app!
  []
  (rf/dispatch [:app-started]))

(defn create-multiaccount!
  []
  (rf/dispatch [:profile.create/create-and-login
                {:display-name constants/account-name :password constants/password :color "blue"}]))

(defn recover-and-login
  [seed-phrase]
  (rf/dispatch [:profile.recover/recover-and-login
                {:display-name (:name constants/recovery-account)
                 :seed-phrase  seed-phrase
                 :password     constants/password
                 :color        "blue"}]))

(defn recover-multiaccount!
  []
  (let [masked-seed-phrase (security/mask-data (:seed-phrase constants/recovery-account))]
    (validate-mnemonic
     masked-seed-phrase
     (fn [mnemonic key-uid]
       (rf/dispatch [:onboarding/seed-phrase-validated
                     (security/mask-data mnemonic) key-uid])
       (rf/dispatch [:pop-to-root :profiles])
       (rf/dispatch [:profile/profile-selected key-uid])
       (recover-and-login mnemonic))
     #())))

(defn enable-testnet!
  []
  (rf/dispatch [:profile.settings/profile-update :test-networks-enabled?
                true {}])
  (rf/dispatch [:wallet/initialize]))

(defn app-initialized
  []
  (let [app-state @(rf/subscribe [:app-state])]
    (= "active" app-state)))

(defn assert-app-initialized
  []
  (is (app-initialized)))

(defn messenger-started
  []
  @(rf/subscribe [:messenger/started?]))

(defn assert-messenger-started
  []
  (is (messenger-started)))

(defn assert-community-created
  []
  (is (= @(rf/subscribe [:communities/create]) constants/community)))

(defn logout
  []
  (rf/dispatch [:logout]))

(defn log-headline
  [test-name]
  (log/info (str "========= " (name test-name) " ==================")))
