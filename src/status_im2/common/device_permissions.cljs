(ns status-im2.common.device-permissions
  (:require
    [quo2.foundations.colors :as colors]
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
                     {:icon       :i/info
                      :icon-color colors/danger-50
                      :theme      :dark
                      :text       (i18n/label :t/camera-permission-denied)}])}]))
