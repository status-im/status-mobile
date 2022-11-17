(ns status-im2.setup.dev
  (:require [react-native.platform :as platform]
            [utils.re-frame :as rf]
            [status-im.ethereum.json-rpc :as json-rpc]
            ["react-native" :refer (DevSettings LogBox)]))

;; Only ignore warnings/errors that cannot be fixed for the time being.
;; When you add a warning to be ignored explain below why it is ignored and how it can be fixed.
;; When a warning is fixed make sure to remove it from here.
(.ignoreLogs ^js LogBox (clj->js ["undefined is not an object (evaluating 'e.message')"
                                  "group12"]))

;; List of ignored warnings/errors:
;; 1. "evaluating 'e.message'": Not sure why this error is happening, but it is coming from here
;; @walletconnect/jsonrpc-utils/dist/esm/error.js parseConnectionError() method
;; 2. "group12": referring to the group-icon size 12x12. Currently, it is not available. When the design team adds it to the
;; icon set it will be added to project.


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
