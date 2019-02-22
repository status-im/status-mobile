(ns status-im.tribute-to-talk.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.tribute-to-talk.db :as tribute-to-talk]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.config :as config]
            [status-im.utils.money :as money]))

(re-frame/reg-sub
 :tribute-to-talk/settings
 (fn [db]
   (tribute-to-talk/get-settings db)))

(re-frame/reg-sub
 :tribute-to-talk/screen-params
 (fn [db]
   (get-in db [:navigation/screen-params :tribute-to-talk])))

(re-frame/reg-sub
 :tribute-to-talk/profile
 :<- [:tribute-to-talk/settings]
 :<- [:tribute-to-talk/screen-params]
 (fn [[{:keys [seen? snt-amount]}
       {:keys [state unavailable?]}]]
   (let [state (or state (if snt-amount :completed :disabled))
         snt-amount (tribute-to-talk/from-wei snt-amount)]
     (when config/tr-to-talk-enabled?
       (if unavailable?
         {:subtext "Change network to enable Tribute to Talk"
          :active? false
          :icon :main-icons/tribute-to-talk
          :icon-color colors/gray}
         (cond-> {:new? (not seen?)}
           (and (not (and seen?
                          snt-amount
                          (#{:signing :pending :transaction-failed :completed} state))))
           (assoc :subtext (i18n/label :t/tribute-to-talk-desc))

           (#{:signing :pending} state)
           (assoc :activity-indicator {:animating true
                                       :color colors/blue}
                  :subtext (case state
                             :pending (i18n/label :t/pending-confirmation)
                             :signing (i18n/label :t/waiting-to-sign)))

           (= state :transaction-failed)
           (assoc :icon :main-icons/warning
                  :icon-color colors/red
                  :subtext (i18n/label :t/transaction-failed))

           (not (#{:signing :pending :transaction-failed} state))
           (assoc :icon :main-icons/tribute-to-talk)

           (and (= state :completed)
                (not-empty snt-amount))
           (assoc :accessory-value (str snt-amount " SNT"))))))))

(re-frame/reg-sub
 :tribute-to-talk/enabled?
 :<- [:tribute-to-talk/settings]
 (fn [settings]
   (tribute-to-talk/enabled? settings)))

(re-frame/reg-sub
 :tribute-to-talk/settings-ui
 :<- [:tribute-to-talk/settings]
 :<- [:tribute-to-talk/screen-params]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[{:keys [seen? snt-amount message]
        :as settings}
       {:keys [step editing? state error]
        :or {step :intro}
        screen-snt-amount :snt-amount
        screen-message :message} prices currency]]
   (let [fiat-value (if snt-amount
                      (money/fiat-amount-value
                       snt-amount
                       :SNT
                       (-> currency :code keyword)
                       prices)
                      "0")]
     (cond-> {:seen? seen?
              :snt-amount (tribute-to-talk/from-wei snt-amount)
              :message message
              :enabled? (tribute-to-talk/enabled? settings)
              :error error
              :step step
              :state (or state (if snt-amount :completed :disabled))
              :editing? editing?
              :fiat-value (str "~" fiat-value " " (:code currency))}

       (= step :set-snt-amount)
       (assoc :snt-amount (str screen-snt-amount)
              :disable-button?
              (boolean (and (= step :set-snt-amount)
                            (or (string/blank? screen-snt-amount)
                                (#{"0" "0.0" "0.00"} screen-snt-amount)
                                (string/ends-with? screen-snt-amount ".")))))

       (= step :personalized-message)
       (assoc :message screen-message)))))

(re-frame/reg-sub
 :tribute-to-talk/fiat-value
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[prices currency] [_ snt-amount]]
   (if snt-amount
     (money/fiat-amount-value snt-amount
                              :SNT
                              (-> currency :code keyword)
                              prices)
     "0")))
