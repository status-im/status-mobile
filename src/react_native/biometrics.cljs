(ns react-native.biometrics
  (:require
    ["react-native-biometrics" :default Biometrics]
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.platform :as platform]
    [schema.core :as schema]))

(def ^:private ^:const android-not-available-error-message "Biometric hardware unavailable")
(def ^:private ^:const android-not-enrolled-error-message "No fingerprints enrolled.")
(def ^:private ^:const android-too-many-attempts-error-message
  "Too many attempts. Use screen lock instead.")
(def ^:private ^:const ios-not-enrolled-error-message "No identities are enrolled")

(defn get-supported-type
  "Returns a JS promise that resolves with the biometrics types supported by the
  device, regardless of whether it's enabled or disabled.

  Resolved values: `:Biometrics` `:FaceID` `:TouchID`"
  []
  (-> (Biometrics.)
      (.isSensorAvailable)
      (.then (fn [result]
               (-> result
                   (oops/oget :biometryType)
                   keyword)))
      (.catch (constantly nil))))

(defn get-available
  "Returns a JS promise that resolves to a boolean, which signifies whether
  biometrics is enabled/disabled on the device."
  []
  (-> (Biometrics.)
      (.isSensorAvailable)
      (.then (fn [result]
               (oops/oget result :available)))))

;; NOTE: the react-native-biometrics package error codes/messages differ across platforms.
;; On android we get error messages while on iOS it's a stringified Obj-C error object.
(defn- convert-auth-error-message
  [message]
  (let [cause (if platform/android?
                (condp = message
                  android-not-enrolled-error-message      :biometrics/fingerprints-not-enrolled-error
                  android-not-available-error-message     :biometrics/not-available-error
                  android-too-many-attempts-error-message :biometric/too-many-attempts
                  :biometrics/unknown-error)

                (condp #(string/includes? %2 %1) message
                  ios-not-enrolled-error-message :biometrics/ios-not-enrolled-error
                  :biometrics/unknown-error))]
    (ex-info "Failed to authenticate with biometrics"
             {:orig-error-message message}
             cause)))

(defn authenticate
  "Returns a JS promise that resolves with a boolean auth success state: `true` for
  success and `false` when canceled by user."
  [{:keys [prompt-message fallback-prompt-message cancel-button-text]}]
  (-> (Biometrics.)
      (.simplePrompt #js
                      {"promptMessage"         prompt-message
                       "fallbackPromptMessage" fallback-prompt-message
                       "cancelButtonText"      cancel-button-text})
      (.then (fn [result]
               (oops/oget result :success)))
      (.catch (fn [error]
                (-> (.-message error)
                    convert-auth-error-message
                    throw)))))

(schema/=> authenticate
  [:=>
   [:cat
    [:map {:closed true}
     [:prompt-message string?]
     [:fallback-prompt-message {:optional true} string?]
     [:cancel-button-text {:optional true} string?]]]
   :any])
