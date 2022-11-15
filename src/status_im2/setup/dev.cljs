(ns status-im2.setup.dev
  (:require [react-native.platform :as platform]
            [utils.re-frame :as rf]
            [status-im.ethereum.json-rpc :as json-rpc]
            ["react-native" :refer (DevSettings LogBox)]))

(.ignoreAllLogs LogBox)

(defn setup []
  (rf/set-mergeable-keys #{:filters/load-filters
                           :pairing/set-installation-metadata
                           :dispatch-n
                           :status-im.ens.core/verify-names
                           :shh/send-direct-message
                           :shh/remove-filter
                           :transport/confirm-messages-processed
                           :group-chats/extract-membership-signature
                           :utils/dispatch-later
                           ::json-rpc/call})

  (when (and js/goog.DEBUG platform/ios? DevSettings)
    ;;on Android this method doesn't work
    (when-let [nm (.-_nativeModule DevSettings)]
      ;;there is a bug in RN, so we have to enable it first and then disable
      (.setHotLoadingEnabled ^js nm true)
      (js/setTimeout #(.setHotLoadingEnabled ^js nm false) 1000))))
