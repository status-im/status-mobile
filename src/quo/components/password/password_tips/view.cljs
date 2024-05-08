(ns quo.components.password.password-tips.view
  (:require [quo.components.password.password-tips.style :as style]
            [quo.components.password.tips.view :as tips]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:lower-case? :boolean]
     [:upper-case? :boolean]
     [:numbers? :boolean]
     [:symbols? :boolean]]
    [:any]]])

(defn- view-internal
  [{:keys [lower-case? upper-case? numbers? symbols?]}]
  [rn/view {:style style/password-tips}
   [tips/view {:completed? lower-case?}
    (i18n/label :t/password-creation-tips-1)]
   [tips/view {:completed? upper-case?}
    (i18n/label :t/password-creation-tips-2)]
   [tips/view {:completed? numbers?}
    (i18n/label :t/password-creation-tips-3)]
   [tips/view {:completed? symbols?}
    (i18n/label :t/password-creation-tips-4)]])

(def view (schema/instrument #'view-internal ?schema))
