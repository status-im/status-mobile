(ns quo.components.list-items.network-list.view
  (:require
    [quo.components.list-items.network-list.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- info
  [{:keys [network-name label]}]
  [rn/view {:style style/info}
   [rn/image
    {:source (quo.resources/get-network network-name)
     :style  style/network-image}]
   [rn/view
    [text/text
     {:weight          :semi-bold
      :number-of-lines 1}
     (if-not (empty? label) label "-")]]])

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

(defn- view-internal
  []
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [on-press state customization-color
                 _network-name _networks _token-value _fiat-value theme]
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

(def view (quo.theme/with-theme view-internal))
