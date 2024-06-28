(ns status-im.setup.dev
  (:require
    ["react-native" :refer (DevSettings LogBox NativeModules)]
    [react-native.platform :as platform]
    [status-im.setup.oops :as setup.oops]
    [status-im.setup.schema :as schema]
    [utils.re-frame :as rf]))

;; Ignore all logs, because there are lots of temporary warnings when developing and hot reloading
(.ignoreAllLogs LogBox)

;; Only ignore warnings/errors that cannot be fixed for the time being.
;; When you add a warning to be ignored explain below why it is ignored and how it can be fixed.
;; When a warning is fixed make sure to remove it from here.
#_(.ignoreLogs ^js LogBox
               (clj->js ["undefined is not an object (evaluating 'e.message')"
                         "Cannot read property 'message' of undefined"
                         "InternalError Metro has encountered an error"
                         "undefined Unable to resolve module `parse-svg-path`"
                         "group12"
                         "Setting a timer for a long period of time"]))

;; List of ignored warnings/errors:
;; 1. "evaluating 'e.message'": Not sure why this error is happening, but it is coming from here
;; @walletconnect/jsonrpc-utils/dist/esm/error.js parseConnectionError() method
;; 2. "Cannot read property 'message' of undefined": same as 1, but for Android
;; 3. "InternalError Metro has encountered an error": an error that happens when losing connection to
;; metro, can be safely ignored
;; 4. "undefined Unable to resolve module `parse-svg-path`": an error that happens when losing
;; connection
;; to metro, can be safely ignored
;; 5. "group12": referring to the group-icon size 12x12. Currently, it is not available. When the design
;; team adds it to the
;; icon set it will be added to project.
;; 6. Setting a timer for a long period of time. Not sure why this error is happening

(defn setup
  []
  (rf/set-mergeable-keys #{:filters/load-filters
                           :pairing/set-installation-metadata
                           :fx
                           :dispatch-n
                           :legacy.status-im.ens.core/verify-names
                           :shh/send-direct-message
                           :shh/remove-filter
                           :transport/confirm-messages-processed
                           :group-chats/extract-membership-signature
                           :utils/dispatch-later
                           :json-rpc/call})
  (when ^:boolean js/goog.DEBUG
    (setup.oops/setup!)
    (schema/setup!)
    (when platform/ios?
      ;; on Android this method doesn't work
      (when-let [setHotLoadingEnabled (or
                                       (and NativeModules
                                            (.-DevSettings NativeModules)
                                            (.-setHotLoadingEnabled (.-DevSettings NativeModules)))
                                       (and DevSettings
                                            (.-_nativeModule DevSettings)
                                            (.-setHotLoadingEnabled (.-_nativeModule DevSettings))))]
        ;;there is a bug in RN, so we have to enable it first and then disable
        (setHotLoadingEnabled true)
        (js/setTimeout #(setHotLoadingEnabled false) 1000)))))
