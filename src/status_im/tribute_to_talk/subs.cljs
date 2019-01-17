(ns status-im.tribute-to-talk.subs
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.money :as money]))

(re-frame/reg-sub
 :tribute-to-talk/settings
 (fn [db]
   (get-in db [:account/account :settings :tribute-to-talk])))

(re-frame/reg-sub
 :tribute-to-talk/screen-params
 (fn [db]
   (get-in db [:navigation/screen-params :tribute-to-talk])))

(re-frame/reg-sub
 :tribute-to-talk/ui
 :<- [:tribute-to-talk/settings]
 :<- [:tribute-to-talk/screen-params]
 :<- [:prices]
 :<- [:wallet/currency]
 (fn [[{:keys [snt-amount message]}
       {:keys [step editing?] :or {step :intro}}
       prices currency]]
   (let [fiat-value (if snt-amount
                      (money/fiat-amount-value snt-amount
                                               :SNT
                                               (-> currency :code keyword)
                                               prices)
                      "0")
         disabled? (and (= step :set-snt-amount)
                        (or (string/blank? snt-amount)
                            (= "0" snt-amount)
                            (string/ends-with? snt-amount ".")))]
     {:snt-amount snt-amount
      :disabled? disabled?
      :message message
      :step step
      :editing? editing?
      :fiat-value (str "~" fiat-value " " (:code currency))})))
