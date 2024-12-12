(ns quo.components.list-items.network-list.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
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
      [:fiat-value {:optional true} [:maybe :string]]
      [:token-value {:optional true} [:maybe :string]]
      [:container-style {:optional true} [:maybe :map]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:state {:optional true} [:enum :pressed :active :disabled :default]]
      [:on-press {:optional true} [:maybe fn?]]]]]
   :any])

(defn- view-internal
  [{:keys [fiat-value token-value on-press container-style state customization-color]
    :as   props
    :or   {customization-color :blue}}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))
        internal-state         (if pressed? :pressed state)]
    [rn/pressable
     {:style               (merge (style/container internal-state customization-color theme)
                                  container-style)
      :on-press-in         (when-not (= state :disabled) on-press-in)
      :on-press-out        (when-not (= state :disabled) on-press-out)
      :on-press            (when-not (= state :disabled) on-press)
      :accessibility-label :network-list}
     [info props]
     (if (or token-value fiat-value)
       [values props]
       [icon/icon :i/chevron-right])]))

(def view (schema/instrument #'view-internal ?schema))
