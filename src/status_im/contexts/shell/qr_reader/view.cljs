(ns status-im.contexts.shell.qr-reader.view
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [status-im.common.router :as router]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [status-im.contexts.communities.events]
    [status-im.contexts.wallet.common.validation :as wallet-validation]
    [status-im.navigation.events :as navigation]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]))

(defn- text-for-path? [text path]
  (some #(string/starts-with? text %) (router/path-urls path)))

(defn- extract-id
  [scanned-text]
  (let [index (string/index-of scanned-text "#")]
    (subs scanned-text (inc index))))

(defn legacy-eth-address?
  [scanned-text]
  (wallet-validation/eth-address? scanned-text))

(defn pairing-qr-code?
  [scanned-text]
  false)

(defn on-qr-code-scanned [scanned-text]
  (let [address (extract-id scanned-text)]
    (cond
      (text-for-path? scanned-text router/community-with-data-path)
      nil
      ;;(debounce/debounce-and-dispatch [:communities/navigate-to-community-overview address] 300)

      (text-for-path? scanned-text router/channel-path)
      nil

      (text-for-path? scanned-text router/user-with-data-path)
      (debounce/debounce-and-dispatch [:contacts/set-new-identity {:input address
                                                                   :build-success-fn (fn [{:keys [public-key ens-name] :as contact}]
                                                                                       {:dispatch-n [[:chat.ui/show-profile public-key ens-name]]})}] 300)

      (legacy-eth-address? scanned-text)
      ;; :wallet/scan-address-success
      (debounce/debounce-and-dispatch [:navigate-to :wallet-accounts address] 300)

      (pairing-qr-code? scanned-text)
      ;; :syncing/input-connection-string-for-bootstrapping
      nil

      :else
      (debounce/debounce-and-dispatch [:toasts/upsert {:type  :negative
                                                       :theme :dark
                                                       :text  (i18n/label :t/invalid-qr)}]
                                      300))))

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