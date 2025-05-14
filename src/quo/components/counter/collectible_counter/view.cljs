(ns quo.components.counter.collectible-counter.view
  (:require
    [quo.components.counter.collectible-counter.style :as style]
    [quo.components.markdown.text :as text]
    [quo.context]
    [react-native.core :as rn]
    [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:container-style {:optional true} [:maybe :map]]
      [:value {:optional true} [:maybe [:or :string :int]]]
      [:status {:optional true} [:maybe :keyword]]
      [:size {:optional true} [:maybe [:enum :size-32 :size-24]]]
      [:accessibility-label {:optional true} [:maybe :keyword]]]]]
   :any])

(defn- view-internal
  [{:keys [value accessibility-label container-style]
    :as   props}]
  (let [theme         (quo.context/use-theme)
        default-props {:status :default
                       :size   :size-32}
        props         (merge default-props props)]
    [rn/view
     {:accessible          true
      :accessibility-label (or accessibility-label :collectible-counter)
      :style               (merge (style/container props theme) container-style)}
     [text/text
      {:weight :medium
       :size   (style/get-text-size props)
       :style  (style/text props theme)}
      value]]))

(def view (schema/instrument #'view-internal ?schema))
