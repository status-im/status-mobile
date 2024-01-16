(ns status-im.contexts.shell.scan.view
  (:require
   [clojure.string :as string]
   [re-frame.core :as rf]
   [react-native.core :as rn]
   [react-native.hooks :as hooks]
   [status-im.common.scan-qr-code.view :as scan-qr-code]
   [status-im.contexts.wallet.common.validation :as wallet-validation]
   [status-im.navigation.events :as navigation]
   [utils.i18n :as i18n]))


(defn legacy-wallet?
  [qr-string]
  (wallet-validation/eth-address? qr-string))


(defn profile-qr-code?
  [qr-string]
  (string/starts-with? qr-string "https://status.app/u/"))


(defn extracted-id 
  [qr-string]
  (let [index (string/index-of qr-string "#")]
    (prn (subs qr-string index))
    (subs qr-string index)))


(defn qr-type [qr-string]
  (cond
    (legacy-wallet? qr-string)
    (navigation/change-tab :wallet-stack)

    (profile-qr-code? qr-string)
    (rf/dispatch [:chat.ui/show-profile (extracted-id qr-string)])
    
    :else
    #(rf/dispatch
      [:toasts/upsert
       {:type  :negative
        :theme :dark
        :text  (i18n/label :t/oops-qr-does-not-work)}])))


(defn- f-internal-view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)]
    [:<>
     (when keyboard-shown
       (rn/dismiss-keyboard!))
     [scan-qr-code/view
      {:title           (i18n/label :t/scan-qr)
       :on-success-scan #(qr-type (string/trim %))}]]))

(defn view
  []
  [:f> f-internal-view])
