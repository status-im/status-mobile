(ns quo.components.list-items.network-list.view
  (:require
    [clojure.string :as string]
    [quo.components.list-items.network-list.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
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
  [{:keys [token-value fiat-value]}]
  (let [theme (quo.theme/use-theme)]
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
      fiat-value]]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:network-image :int]
      [:label :string]
      [:fiat-value :string]
      [:token-value :string]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:state {:optional true} [:enum :pressed :active :default]]
      [:on-press {:optional true} [:maybe fn?]]]]]
   :any])

(defn- view-internal
  [{:keys [on-press state customization-color]
    :as   props
    :or   {customization-color :blue}}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))
        internal-state         (if pressed? :pressed state)]
    [rn/pressable
     {:style               (style/container internal-state customization-color theme)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :accessibility-label :network-list}
     [info props]
     [values props]]))

(def view (schema/instrument #'view-internal ?schema))
