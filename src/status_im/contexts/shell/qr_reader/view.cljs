(ns status-im.contexts.shell.qr-reader.view
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [status-im.contexts.wallet.common.validation :as wallet-validation]
    [status-im.navigation.events :as navigation]
    [utils.i18n :as i18n]))

(defn community-qr-code?
  [scanned-text]
  false)

(defn channel-qr-code?
  [scanned-text]
  false)

(defn profile-qr-code?
  [scanned-text]
  (string/starts-with? scanned-text "https://status.app/u/"))

(defn legacy-eth-address?
  [scanned-text]
  (wallet-validation/eth-address? scanned-text))

(defn pairing-qr-code?
  [scanned-text]
  false)

(defn extracted-id
  [scanned-text]
  (let [index (string/index-of scanned-text "#")]
    (subs scanned-text index)))

(defn on-qr-code-scanned [scanned-text]
  (cond
    (community-qr-code? scanned-text)
    false

    (channel-qr-code? scanned-text)
    false

    (profile-qr-code? scanned-text)
    (rf/dispatch [:chat.ui/show-profile (extracted-id scanned-text)])

    (legacy-eth-address? scanned-text)
    (navigation/change-tab :wallet-stack)

    (pairing-qr-code? scanned-text)
    false

    :else
    #(rf/dispatch [:toasts/upsert {:type  :negative
                                   :theme :dark
                                   :text  (i18n/label :t/invalid-qr)}])))

(defn- f-internal-view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)]
    [:<>
     (when keyboard-shown
       (rn/dismiss-keyboard!))
     [scan-qr-code/view
      {:title           (i18n/label :t/scan-qr)
       :on-success-scan on-qr-code-scanned}]]))

(defn view
  []
  [:f> f-internal-view])