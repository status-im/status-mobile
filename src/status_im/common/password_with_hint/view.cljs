(ns status-im.common.password-with-hint.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.password-with-hint.style :as style]))

(defn view
  [{{:keys [text status shown?]} :hint :as input-props}]
  [:<>
   [quo/input
    (-> input-props
        (dissoc :hint)
        (assoc :type  :password
               :blur? true))]
   [rn/view {:style style/info-message}
    (when shown?
      [quo/info-message
       (cond-> {:status status
                :size   :default}
         (not= :success status) (assoc :icon :i/info)
         (= :success status)    (assoc :icon :i/check-circle)
         (= :default status)    (assoc :color colors/white-70-blur))
       text])]])
