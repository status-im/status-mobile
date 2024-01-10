(ns react-native.biometrics
  (:require
    ["react-native-biometrics" :default biometrics]
    [oops.core :as oops]
    [schema.core :as schema]
    [status-im.constants :as constants]))

(defn get-supported-type
  "Returns a JS promise that resolves with the biometrics types supported by the
  device, regardless of whether it's enabled or disabled.

  Resolved values: `:Biometrics` `:FaceID` `:TouchID`"
  []
  (-> (biometrics.)
      (.isSensorAvailable)
      (.then (fn [result]
               (let [type (-> result
                              (oops/oget "biometryType")
                              keyword)]
                 type)))))

(defn get-available
  "Returns a JS promise that resolves to a boolean, which signifies whether
  biometrics is enabled/disabled on the device."
  []
  (-> (biometrics.)
      (.isSensorAvailable)
      (.then (fn [result]
               (oops/oget result "available")))))

(defn authenticate
  "Returns a JS promise that resolves with a boolean auth success state: `true` for
  success and `false` when canceled by user."
  [{:keys [prompt-message fallback-prompt-message cancel-button-text]}]
  (-> (biometrics.)
      (.simplePrompt #js {"promptMessage"         prompt-message
                          "fallbackPromptMessage" fallback-prompt-message
                          "cancelButtonText"      cancel-button-text})
      (.then (fn [result]
               (let [result  (js->clj result)
                     success (get result "success")
                     error   (get result "error")]
                 ;; NOTE: currently `error` is only present for the user cancellation case,
                 ;; so putting this here for the future in case they add other errors
                 ;; instead of rejecting.
                 (when (and error (not= error constants/biometric-error-user-canceled))
                   (throw error))
                 success)))))

(schema/=> authenticate
  [:=>
   [:cat
    [:map {:closed true}
     [:prompt-message string?]
     [:fallback-prompt-message {:optional true} string?]
     [:cancel-button-text {:optional true} string?]]]
   :any])
