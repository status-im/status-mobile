(ns status-im.common.device-permissions
  (:require
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn camera
  [on-allowed]
  (rf/dispatch
   [:request-permissions
    {:permissions [:camera]
     :on-allowed  on-allowed
     :on-denied   #(rf/dispatch
                    [:toasts/upsert
                     {:type  :negative
                      :theme :dark
                      :text  (i18n/label :t/camera-permission-denied)}])}]))
