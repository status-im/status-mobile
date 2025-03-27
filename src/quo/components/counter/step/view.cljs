(ns quo.components.counter.step.view
  (:require
    [quo.components.counter.step.style :as style]
    [quo.components.markdown.text :as text]
    quo.context
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.number]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:accessibility-label {:optional true} [:maybe :keyword]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:in-blur-view? {:optional true} [:maybe :boolean]]
      [:type {:optional true} [:enum :active :complete :neutral]]]]
    [:value [:maybe [:or :string :int]]]]
   :any])

(defn- view-internal
  [{:keys [type accessibility-label in-blur-view? customization-color]} value]
  (let [theme (quo.context/use-theme)
        type  (or type :neutral)
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

(def view (schema/instrument #'view-internal ?schema))
