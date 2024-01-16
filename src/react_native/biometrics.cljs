(ns react-native.biometrics
  (:require
    ["react-native-biometrics" :default biometrics]
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.platform :as platform]
    [schema.core :as schema]))

(def ^:const biometric-error-not-available "biometric-not-available")
(def ^:const biometric-error-not-enrolled "biometric-not-enrolled")

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
                 type)))
      (.catch (constantly nil))))

(defn get-available
  "Returns a JS promise that resolves to a boolean, which signifies whether
  biometrics is enabled/disabled on the device."
  []
  (-> (biometrics.)
      (.isSensorAvailable)
      (.then (fn [result]
               (oops/oget result "available")))))

;; NOTE: the react-native-biometry package error codes/messages differ across platforms.
;; On android we get error messages while on iOS it's a stringified Obj-C error object.
(defn- convert-auth-error-message
  [code]
  (letfn [(includes? [substring string]
            (string/includes? string substring))]
    (if platform/android?
      (condp = code
        "No fingerprints enrolled."      biometric-error-not-enrolled
        "Biometric hardware unavailable" biometric-error-not-available
        code)

      (condp includes? code
        "No identities are enrolled" biometric-error-not-enrolled
        code))))

(defn authenticate
  "Returns a JS promise that resolves with a boolean auth success state: `true` for
  success and `false` when canceled by user."
  [{:keys [prompt-message fallback-prompt-message cancel-button-text]}]
  (-> (biometrics.)
      (.simplePrompt #js
                      {"promptMessage"         prompt-message
                       "fallbackPromptMessage" fallback-prompt-message
                       "cancelButtonText"      cancel-button-text})
      (.then (fn [result]
               (-> result js->clj (get "success"))))
      (.catch (fn [error]
                (-> (.-message error)
                    convert-auth-error-message
                    js/Error
                    throw)))))

(schema/=> authenticate
  [:=>
   [:cat
    [:map {:closed true}
     [:prompt-message string?]
     [:fallback-prompt-message {:optional true} string?]
     [:cancel-button-text {:optional true} string?]]]
   :any])
