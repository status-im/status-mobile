(ns quo.components.wallet.progress-bar.view
  (:require
    [quo.components.wallet.progress-bar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed false}
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:theme :schema.common/theme]
      [:progressed-value {:optional true} [:maybe [:or :string :int]]]
      [:full-width? {:optional true} [:maybe :boolean]]]]]
   :any])

(defn- view-internal
  [{:keys [full-width?] :as props}]
  [rn/view
   {:accessibility-label :progress-bar
    :style               (style/root-container props)}
   (when full-width?
     [rn/view {:style (style/progressed-bar props)}])])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))
