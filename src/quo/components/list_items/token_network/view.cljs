(ns quo.components.list-items.token-network.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.list-items.token-network.schema :as component-schema]
    [quo.components.list-items.token-network.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.context :as quo.context]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- info
  [{:keys [token label networks]}]
  [rn/view {:style style/info}
   (when token
     [token/view
      {:style style/token-image
       :size  :size-32
       :token token}])
   [rn/view {:style style/token-info}
    [text/text
     {:weight          :semi-bold
      :number-of-lines 1}
     (if-not (empty? label) label "-")]
    [preview-list/view
     {:type   :network
      :size   :size-14
      :number (count networks)}
     networks]]])

(defn- values
  [{:keys [state token-value fiat-value customization-color]}]
  (let [theme (quo.context/use-theme)]
    (if (= state :selected)
      [icon/icon :i/check
       {:color               (style/check-color customization-color theme)
        :accessibility-label :check-icon}]
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
        fiat-value]])))

(defn- view-internal
  [{:keys [on-press state customization-color _token _networks _token-value _fiat-value]
    :as   props
    :or   {customization-color :blue}}]
  (let [theme                  (quo.context/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))
        internal-state         (if pressed? :pressed state)]
    [rn/pressable
     {:style               (style/container internal-state customization-color theme)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :disabled            (= state :disabled)
      :accessibility-label :token-network}
     [info props]
     [values props]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
