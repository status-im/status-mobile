(ns react-native.vision-camera
  (:require
    [oops.core :as oops]
    [react-native-vision-camera :refer (Camera useCodeScanner useCameraDevice)]
    [reagent.core :as reagent]
    [schema.core :as schema]
    [taoensso.timbre :as log]))

(defn capture
  [^js camera-ref on-success]
  (-> (.takePhoto camera-ref)
      (.then #(on-success (oops/oget % :path)))
      (.catch #(log/warn "couldn't capture photo" {:error %}))))

(def ^:private camera-comp (reagent/adapt-react-class Camera))

(defn camera
  [{:keys [code-scanner active? torch] :as props}]
  [camera-comp
   (assoc props
          :codeScanner code-scanner
          :isActive    active?
          :torch?      torch)])

(defn use-camera-device [type] (useCameraDevice (name type)))
(schema/=> use-camera-device [:=> [:cat [:enum :back :front :external]] :any])

(def ^:private scan-code-types ["qr"])

(defn use-code-scanner
  [on-scan]
  (-> {:codeTypes     scan-code-types
       :onCodeScanned on-scan}
      clj->js
      useCodeScanner))
(schema/=> use-code-scanner [:=> [:cat fn?] :any])
