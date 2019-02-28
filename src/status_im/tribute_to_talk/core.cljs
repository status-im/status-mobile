(ns status-im.tribute-to-talk.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]))

(fx/defn mark-ttt-as-seen
  [{:keys [db] :as cofx}]
  (let [settings (get-in db [:account/account :settings])
        {:keys [seen?]} (:tribute-to-talk settings)]
    (when-not seen?
      (fx/merge cofx
                {:db (assoc db :tribute-to-talk/seen? true)}
                (accounts.update/update-settings
                 (assoc-in settings [:tribute-to-talk :seen?] true) {})))))

(fx/defn open-settings
  [{:keys [db] :as cofx}]
  (let [snt-amount (get-in db [:account/account :settings :tribute-to-talk :snt-amount])]
    (fx/merge cofx
              mark-ttt-as-seen
              (navigation/navigate-to-cofx :tribute-to-talk
                                           (if snt-amount
                                             {:step :edit
                                              :editing? true}
                                             {:step :intro})))))

(fx/defn set-step
  [{:keys [db]} step]
  {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :step] step)})

(fx/defn open-learn-more
  [cofx]
  (set-step cofx :learn-more))

(fx/defn step-back
  [cofx]
  (let [{:keys [step editing?]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])]
    (case step
      (:intro :edit)
      (navigation/navigate-back cofx)

      (:learn-more :set-snt-amount)
      (set-step cofx (if editing?
                       :edit
                       :intro))

      :personalized-message
      (set-step cofx :set-snt-amount)

      :finish
      (set-step cofx :personalized-message))))

(fx/defn step-forward
  [cofx]
  (let [{:keys [step editing?]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])]
    (case step
      :intro
      (set-step cofx :set-snt-amount)

      :set-snt-amount
      (set-step cofx :personalized-message)

      :personalized-message
      (let [account-settings (get-in cofx [:db :account/account :settings])]
        (fx/merge cofx
                  (set-step (if editing?
                              :edit
                              :finish))
                  (accounts.update/update-settings
                   account-settings
                   {})))

      :finish
      (navigation/navigate-back cofx))))

(defn get-new-snt-amount
  [snt-amount numpad-symbol]
  ;; TODO: Put some logic in place so that incorrect numbers can not
  ;; be entered
  (let [snt-amount  (or snt-amount "0")]
    (if (= numpad-symbol :remove)
      (let [len (count snt-amount)
            s (subs snt-amount 0 (dec len))]
        (cond-> s
          ;; Remove both the digit after the dot and the dot itself
          (string/ends-with? s ".") (subs 0 (- len 2))
          ;; Set default value if last digit is removed
          (string/blank? s) (do "0")))
      (cond
        ;; Disallow two consecutive dots
        (and (string/includes? snt-amount ".") (= numpad-symbol "."))
        snt-amount
        ;; Disallow more than 2 digits after the dot
        (and (string/includes? snt-amount ".")
             (> (count (second (string/split snt-amount #"\."))) 1))
        snt-amount
        ;; Replace initial "0" by the first digit
        (and (= snt-amount "0") (not= numpad-symbol "."))
        (str numpad-symbol)
        :else (str snt-amount numpad-symbol)))))

(fx/defn update-snt-amount
  [{:keys [db]} numpad-symbol]
  {:db (update-in db [:account/account :settings :tribute-to-talk :snt-amount]
                  #(get-new-snt-amount % numpad-symbol))})

(fx/defn update-message
  [{:keys [db]} message]
  {:db (assoc-in db [:account/account :settings :tribute-to-talk :message]
                 message)})

(fx/defn start-editing
  [{:keys [db]}]
  {:db (assoc-in db [:navigation/screen-params :tribute-to-talk]
                 {:step :set-snt-amount
                  :editing? true})})

(defn remove
  [{:keys [db] :as cofx}]
  (let [account-settings (get-in db [:account/account :settings])]
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params :tribute-to-talk]
                             {:step :finish})}
              (accounts.update/update-settings
               (assoc account-settings :tribute-to-talk {:seen? true}) {}))))
