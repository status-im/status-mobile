(ns status-im.contexts.shell.qr-reader.view
  (:require
    [clojure.string :as string]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [status-im.common.router :as router]
    [status-im.common.scan-qr-code.view :as scan-qr-code]
    [status-im.common.validators :as validators]
    [status-im.contexts.communities.events]
    [status-im.contexts.wallet.common.validation :as wallet-validation]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]))

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

(defn pairing-qr-code?
  [scanned-text]
  false)

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

(defn on-qr-code-scanned
  [scanned-text]
  (let [address (extract-id scanned-text)]
    (cond
      (text-for-url-path? scanned-text router/community-with-data-path)
      ;; TODO(alwx): community link, to be implemented
      nil

      (text-for-url-path? scanned-text router/channel-path)
      ;; TODO(alwx): channel link, to be implemented
      nil

      (text-for-url-path? scanned-text router/user-with-data-path)
      (load-and-show-profile address)

      (or (validators/valid-public-key? scanned-text)
          (validators/valid-compressed-key? scanned-text))
      (load-and-show-profile scanned-text)

      (eth-address? scanned-text)
      (debounce/debounce-and-dispatch [:navigate-to :wallet-accounts address] 300)

      (pairing-qr-code? scanned-text)
      ;; TODO(alwx): pairing link, to be implemented
      nil

      :else
      (show-invalid-qr-toast))))

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
