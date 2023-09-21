(ns quo2.components.reactions.reaction
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.reactions.resource :as resource]
            [quo2.components.reactions.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn- add-reaction-internal
  [{:keys [on-press theme]}]
  [rn/touchable-opacity
   {:on-press            on-press
    :accessibility-label :emoji-reaction-add
    :style               (style/add-reaction theme)}
   [icons/icon :i/add-reaction
    {:size  20
     :color (colors/theme-colors colors/neutral-50
                                 colors/neutral-40
                                 theme)}]])

(def add-reaction (theme/with-theme add-reaction-internal))

(defn reaction-internal
  "Add your emoji as a param here"
  [{:keys [emoji clicks neutral? on-press accessibility-label on-long-press theme]}]
  (let [numeric-value (int clicks)]
    [rn/touchable-opacity
     {:on-press            on-press
      :on-long-press       on-long-press
      :accessibility-label accessibility-label
      :style               (style/reaction neutral? theme)}
     [rn/image
      {:style               {:width 16 :height 16}
       :accessibility-label :emoji
       :source              (resource/get-reaction emoji)}]
     [text/text
      {:size   :paragraph-2
       :weight :semi-bold
       :style  style/reaction-count}
      (str (if (pos? numeric-value) numeric-value 1))]]))

(def reaction (theme/with-theme reaction-internal))
