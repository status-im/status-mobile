(ns status-im.contexts.shell.qr-reader.view
  (:require [clojure.string :as string]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [status-im.common.router :as router]
            [status-im.common.scan-qr-code.view :as scan-qr-code]
            [status-im.common.validation.general :as validators]
            [status-im.contexts.communities.events]
            [status-im.contexts.wallet.common.validation :as wallet-validation]
            [status-im.contexts.wallet.wallet-connect.utils :as wc-utils]
            [status-im.feature-flags :as ff]
            [utils.debounce :as debounce]
            [utils.ethereum.eip.eip681 :as eip681]
            [utils.i18n :as i18n]
            [utils.url :as url]))

(def invalid-qr-toast
  {:type  :negative
   :theme :dark
   :text  (i18n/label :t/invalid-qr)})

(defn- text-for-url-path?
  [text path]
  (some #(string/starts-with? text %) (router/path-urls path)))

(defn- extract-id
  [scanned-text]
  (let [index (string/index-of scanned-text "#")]
    (subs scanned-text (inc index))))

(defn eth-address?
  [scanned-text]
  (wallet-validation/eth-address? scanned-text))

(defn eip681-address?
  [scanned-text]
  (-> scanned-text
      eip681/parse-uri
      :address
      boolean))

(defn pairing-qr-code?
  [_]
  false)

(defn url?
  [scanned-text]
  (url/url? scanned-text))

(defn load-and-show-profile
  [address]
  (debounce/debounce-and-dispatch
   [:contacts/set-new-identity
    {:input            address
     :build-success-fn (fn [{:keys [public-key ens-name]}]
                         {:dispatch-n [[:chat.ui/show-profile public-key ens-name]
                                       [:contacts/clear-new-identity]]})
     :failure-fn       (fn []
                         {:dispatch [:toasts/upsert invalid-qr-toast]})}]
   300))

(defn show-invalid-qr-toast
  []
  (debounce/debounce-and-dispatch
   [:toasts/upsert invalid-qr-toast]
   300))

(defn- handle-wallet-connect
  [scanned-text]
  (debounce/debounce-and-dispatch
   [:wallet-connect/on-scan-connection scanned-text]
   300))

(defn on-qr-code-scanned
  [scanned-text]
  (cond
    (or
     (text-for-url-path? scanned-text router/community-with-data-path)
     (text-for-url-path? scanned-text router/channel-path))
    (debounce/debounce-and-dispatch [:universal-links/handle-url scanned-text] 300)

    (text-for-url-path? scanned-text router/user-with-data-path)
    (let [address (extract-id scanned-text)]
      (load-and-show-profile address))

    (or (validators/valid-public-key? scanned-text)
        (validators/valid-compressed-key? scanned-text))
    (load-and-show-profile scanned-text)

    (eth-address? scanned-text)
    (do
      (debounce/debounce-and-dispatch [:generic-scanner/scan-success scanned-text] 300)
      (debounce/debounce-and-dispatch [:navigate-change-tab :wallet-stack] 300))

    (eip681-address? scanned-text)
    (do
      (debounce/debounce-and-dispatch [:wallet-legacy/request-uri-parsed
                                       (eip681/parse-uri scanned-text)]
                                      300)
      (debounce/debounce-and-dispatch [:navigate-change-tab :wallet-stack] 300))

    (pairing-qr-code? scanned-text)
    ;; TODO: https://github.com/status-im/status-mobile/issues/18744
    nil

    (and
     (wc-utils/valid-uri? scanned-text)
     (ff/enabled? ::ff/wallet.wallet-connect))
    (handle-wallet-connect scanned-text)

    (url? scanned-text)
    (debounce/debounce-and-dispatch [:browser.ui/open-url scanned-text] 300)

    :else
    (show-invalid-qr-toast)))

(defn- f-internal-view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)]
    [:<>
     (when keyboard-shown
       (rn/dismiss-keyboard!))
     [scan-qr-code/view
      {:title           (i18n/label :t/scan-qr)
       :share-button?   true
       :on-success-scan on-qr-code-scanned}]]))

(defn view
  []
  [:f> f-internal-view])
