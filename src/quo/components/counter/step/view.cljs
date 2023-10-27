(ns quo.components.counter.step.view
  (:require
    [quo.components.counter.step.style :as style]
    [quo.components.markdown.text :as text]
    quo.theme
    [react-native.core :as rn]
    [utils.number]
    utils.schema))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:accessibility-label {:optional true} [:maybe :keyword]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:in-blur-view? {:optional true} [:maybe :boolean]]
      [:theme :schema.common/theme]
      [:type {:optional true} [:enum :active :complete :neutral]]]]
    [:value [:maybe [:or :string :int]]]]
   :any])

(defn- view-internal
  [{:keys [type accessibility-label theme in-blur-view? customization-color]} value]
  (let [type  (or type :neutral)
        value (utils.number/parse-int value)
        label (str value)
        size  (count label)]
    [rn/view
     {:accessible          true
      :accessibility-label (or accessibility-label :step-counter)
      :style               (style/container {:size                size
                                             :type                type
                                             :in-blur-view?       in-blur-view?
                                             :theme               theme
                                             :customization-color customization-color})}
     [text/text
      {:weight :medium
       :size   :label
       :style  {:color (style/text-color type theme)}}
      label]]))

(def ^:private view-internal-instrumented
  (utils.schema/instrument ::step ?schema #'view-internal))

(def view (quo.theme/with-theme #'view-internal-instrumented))
