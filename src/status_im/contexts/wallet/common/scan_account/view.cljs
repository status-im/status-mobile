(ns status-im.contexts.wallet.common.scan-account.view
  (:require
   [clojure.string :as string]
   [status-im.common.scan-qr-code.view :as scan-qr-code]
   [status-im.constants :as constants]
   [status-im.contexts.wallet.wallet-connect.utils :as wc-utils]
   [utils.debounce :as debounce]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(def ^:private supported-networks #{:eth :arb1 :oeth})

(defn- contains-supported-address?
  [s]
  (let [address?   (boolean (re-find constants/regx-address-contains s))
        networks   (when address?
                     (as-> s $
                       (string/split $ ":")
                       (butlast $)))
        supported? (every? supported-networks (map keyword networks))]
    (and address? supported?)))

(defn- valid-wc-uri?
  [s]
  (-> s
      wc-utils/parse-uri
      wc-utils/valid-wc-connection?))

(defn- extract-address
  [scanned-text]
  (first (re-seq constants/regx-multichain-address scanned-text)))

(defn view
  []
  (let [{:keys [on-result]} (rf/sub [:get-screen-params])]
    [scan-qr-code/view
     {:title           (i18n/label :t/scan-qr)
      :subtitle        (i18n/label :t/scan-an-account-qr-code)
      :error-message   (i18n/label :t/oops-this-qr-does-not-contain-an-address)
      :validate-fn     (fn [scanned-text]
                         (or (contains-supported-address? scanned-text)
                             (valid-wc-uri? scanned-text)))
      :on-success-scan (fn [result]
                         (let [address              (extract-address result)
                               wc-parsed-uri        (wc-utils/parse-uri result)
                               valid-wc-connection? (wc-utils/valid-wc-connection? wc-parsed-uri)]
                           (prn :---here---> result)
                           ;; (when on-result (on-result address))
                           ;; (when valid-wc-connection?
                           ;;   (js/alert wc-parsed-uri)
                           ;;   (debounce/debounce-and-dispatch
                           ;;    [:wallet-connect/session-proposal wc-parsed-uri]
                           ;;    300))
                           ;; (when address
                           ;;   (debounce/debounce-and-dispatch
                           ;;    [:wallet/scan-address-success address]
                           ;;    300))
                           ))}]))
