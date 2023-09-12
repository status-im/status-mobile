(ns quo2.components.selectors.react-selector.view
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.selectors.reaction-resource :as reaction.resource]
            [quo2.components.selectors.react-selector.style :as style]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(defn- view-internal
  "Add your emoji as a param here"
  [{:keys [emoji clicks neutral? pinned? on-press accessibility-label on-long-press container-style
           theme]}]
  (let [numeric-value (int clicks)]
    [rn/view {:style container-style}
     [rn/touchable-opacity
      {:on-press            on-press
       :on-long-press       on-long-press
       :accessibility-label accessibility-label
       :style               (style/reaction neutral? pinned? theme)}
      [rn/image
       {:style               {:width 15 :height 15}
        :accessibility-label :emoji
        :source              (reaction.resource/get-reaction emoji)}]
      [text/text
       {:size   :paragraph-2
        :weight :semi-bold
        :style  style/reaction-count}
       (str (if (pos? numeric-value) numeric-value 1))]]]))

(def view (theme/with-theme view-internal))
