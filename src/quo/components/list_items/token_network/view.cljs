(ns quo.components.list-items.token-network.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.list-items.token-network.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- info
  [{:keys [token label networks]}]
  [rn/view {:style style/info}
   [rn/image
    {:source (or (:source token) token)
     :style  style/token-image}]
   [rn/view {:style style/token-info}
    [text/text {:weight :semi-bold} label]
    [preview-list/view
     {:type   :network
      :size   :size-14
      :number 3}
     networks]]])

(defn- values
  [{:keys [state token-value fiat-value customization-color theme]}]
  (if (= state :selected)
    [icon/icon :i/check
     {:color               (style/check-color customization-color theme)
      :accessibility-label :check-icon}]
    [rn/view {:style style/values-container}
     [text/text
      {:weight :medium
       :size   :paragraph-2}
      token-value]
     [text/text
      {:style (style/fiat-value theme)
       :size  :paragraph-2}
      fiat-value]]))

(defn- view-internal
  []
  (let [pressed?     (reagent/atom false)
        on-press-in  #(reset! pressed? true)
        on-press-out #(reset! pressed? false)]
    (fn [{:keys [on-press state customization-color
                 _token _networks _token-value _fiat-value theme]
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
          :accessibility-label :token-network}
         [info props]
         [values props]]))))

(def view (quo.theme/with-theme view-internal))
