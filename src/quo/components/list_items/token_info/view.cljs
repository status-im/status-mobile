(ns quo.components.list-items.token-info.view
  (:require
    [quo.components.list-items.token-info.schema :as component-schema]
    [quo.components.list-items.token-info.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- info
  [{:keys [token label]}]
  (let [theme (quo.theme/use-theme)]
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
      [text/text
       {:weight          :medium
        :size            :paragraph-2
        :style           (style/token-description-label theme)
        :number-of-lines 1}
       token]]]))

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
      :disabled            (= state :disabled)
      :accessibility-label :token}
     [info props]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
