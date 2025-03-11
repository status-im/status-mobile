(ns status-im.subs.biometrics
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :biometrics/supported-type
 :<- [:biometrics]
 (fn [biometrics]
   (get biometrics :supported-type)))
