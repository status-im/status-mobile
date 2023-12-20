(ns quo.components.utilities.social.view
  (:require [quo.components.utilities.social.loader :as social-loader]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.number]))

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:accessibility-label {:optional true} [:or keyword? string?]]
     [:size {:optional true :default :default} [:or keyword? string?]]
     [:type {:optional true :default :default} [:or keyword? string?]]
     [:social [:or keyword? string?]]
     [:style {:optional true} map?]]]
   :any])

(defn- social-style
  [style]
  (assoc style
         :width  20
         :height 20))

(defn view-internal
  "Render a social image.
   Props:
   - accessibility-label accessibility-label to the rn/image
   - style:              extra styles to apply to the `rn/image` component.
   - size:               `:default` or `:bigger`
   - type:               `:default` or `:solid`
   - social:             string or keyword, it can contain upper case letters or not.
                         E.g. all of these are valid and resolve to the same:
                         :social/github | :github | :GITHUB | \"GITHUB\" | \"github\".
  "
  [{:keys [social size style accessibility-label type]
    :or   {size :default
           type :default}}]
  (let [source (social-loader/get-social-image (str (name social) (name size) (name type)))]
    [rn/image
     {:accessibility-label accessibility-label
      :style               (social-style style)
      :source              source}]))

(def view (schema/instrument #'view-internal ?schema))
