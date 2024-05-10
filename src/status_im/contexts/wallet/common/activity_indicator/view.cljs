(ns status-im.contexts.wallet.common.activity-indicator.view
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.activity-indicator.style :as style]
            [utils.i18n :as i18n]))

(defn view
  [activity-state]
  (let [{:keys [message]
         :as   props} (case activity-state
                        :has-activity               {:accessibility-label :account-has-activity
                                                     :icon                :i/done
                                                     :type                :success
                                                     :message             :t/address-activity}
                        :no-activity                {:accessibility-label :account-has-no-activity
                                                     :icon :i/info
                                                     :type :warning
                                                     :message :t/this-address-has-no-activity}
                        :invalid-ens                {:accessibility-label :error-message
                                                     :icon                :i/info
                                                     :type                :error
                                                     :message             :t/invalid-address}
                        :address-already-registered {:accessibility-label :error-message
                                                     :icon                :i/info
                                                     :type                :error
                                                     :message             :t/address-already-in-use}
                        {:accessibility-label :searching-for-activity
                         :icon                :i/pending-state
                         :type                :default
                         :message             :t/searching-for-activity})]
    (when activity-state
      [quo/info-message
       (assoc props
              :style style/info-message
              :size  :default)
       (i18n/label message)])))
