(ns legacy.status-im.notifications.wallet
  (:require
    [clojure.string :as string]
    [legacy.status-im.ethereum.decode :as decode]
    [legacy.status-im.ethereum.tokens :as tokens]
    [legacy.status-im.utils.utils :as utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(def default-erc20-token
  {:symbol   :ERC20
   :decimals 18
   :name     "ERC20"})

(defn preference=
  [x y]
  (and (= (:service x) (:service y))
       (= (:event x) (:event y))
       (= (:identifier x) (:identifier y))))

(defn- update-preference
  [all new-preference]
  (conj (filter (comp not (partial preference= new-preference))
                all)
        new-preference))

(rf/defn switch-transaction-notifications
  {:events [:push-notifications.wallet/switch-transactions]}
  [{:keys [db]} enabled?]
  {:db            (update db
                          :push-notifications/preferences
                          update-preference
                          {:enabled?   enabled?
                           :service    "wallet"
                           :event      "transaction"
                           :identifier "all"})
   :json-rpc/call [{:method     "localnotifications_switchWalletNotifications"
                    :params     [enabled?]
                    :on-success #(log/info "[push-notifications] switch-transaction successful" %)
                    :on-error   #(log/error "[push-notifications] switch-transaction error" %)}]})

(defn create-transfer-notification
  [{db :db}
   {{:keys [state from to fromAccount toAccount value erc20 contract network]}
    :body
    :as notification}]
  (let [token       (if erc20
                      (get-in db
                              [:wallet-legacy/all-tokens (string/lower-case contract)]
                              default-erc20-token)
                      (tokens/native-currency network))
        amount      (money/wei->ether (decode/uint value))
        to          (or (:name toAccount) (utils/get-shortened-address to))
        from        (or (:name fromAccount) (utils/get-shortened-address from))
        title       (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      "outbound" (i18n/label :t/push-outbound-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      "failed"   (i18n/label :t/push-failed-transaction
                                             {:value    amount
                                              :currency (:symbol token)})
                      nil)
        description (case state
                      "inbound"  (i18n/label :t/push-inbound-transaction-body
                                             {:from from
                                              :to   to})
                      "outbound" (i18n/label :t/push-outbound-transaction-body
                                             {:from from
                                              :to   to})
                      "failed"   (i18n/label :t/push-failed-transaction-body
                                             {:value    amount
                                              :currency (:symbol token)
                                              :to       to})
                      nil)]
    {:title     title
     :icon      (get-in token [:icon :source])
     :deepLink  (:deepLink notification)
     :user-info notification
     :message   description}))
