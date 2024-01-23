(ns quo.components.list-items.network-list.view
  (:require
    [clojure.string :as string]
    [quo.components.list-items.network-list.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [schema.core :as schema]))

(defn- info
  [{:keys [network-image label]}]
  [rn/view {:style style/info}
   [rn/image
    {:source network-image
     :style  style/network-image}]
   [rn/view
    [text/text
     {:weight          :semi-bold
      :style           {:text-transform :capitalize}
      :number-of-lines 1}
     (if (string/blank? label) "-" label)]]])

(defn- values
  [{:keys [token-value fiat-value theme]}]
  [rn/view {:style style/values-container}
   [text/text
    {:weight          :medium
     :size            :paragraph-2
     :number-of-lines 1}
    token-value]
   [text/text
    {:style           (style/fiat-value theme)
     :size            :paragraph-2
     :number-of-lines 1}
    fiat-value]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:network-name :keyword]
      [:fiat-value :string]
      [:label :string]
      [:token-value :string]
      [:customization-color :schema.common/customization-color]
      [:state {:default :transparent} :keyword]
      [:on-press {:optional true} [:maybe fn?]]
      [:theme :schema.common/theme]]]]
   :any])

(defn- view-internal
  []
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [on-press state customization-color theme]
          :as   props
          :or   {customization-color :blue}}]
      (let [internal-state (if @pressed?
                             :pressed
                             state)]
        [rn/pressable
         {:style               (style/container internal-state customization-color theme)
          :on-press-in         on-press-in
          :on-press-out        on-press-out
          :on-press            on-press
          :accessibility-label :network-list}
         [info props]
         [values props]]))))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))
