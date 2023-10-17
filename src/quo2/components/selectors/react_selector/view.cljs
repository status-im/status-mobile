(ns quo2.components.selectors.react-selector.view
  (:require
    [quo2.components.icon :as icons]
    [quo2.components.markdown.text :as text]
    [quo2.components.selectors.react-selector.style :as style]
    [quo2.components.selectors.reaction-resource :as reaction.resource]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [emoji clicks state use-case on-press accessibility-label on-long-press container-style
           theme]}]
  (let [numeric-value (int clicks)
        icon-color    (if (= :pinned use-case)
                        (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
                        (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))]
    (if (= :add-reaction state)
      [rn/touchable-opacity
       {:on-press            on-press
        :accessibility-label :emoji-reaction-add
        :style               (style/add-reaction
                              (= :pinned use-case)
                              theme)}
       [icons/icon :i/add-reaction
        {:size  20
         :color icon-color}]]

      [rn/touchable-opacity
       {:on-press            on-press
        :on-long-press       on-long-press
        :accessibility-label accessibility-label
        :style               (merge (style/reaction (= :pressed state)
                                                    (= :pinned use-case)
                                                    theme)
                                    container-style)}
       [rn/image
        {:style               {:width 15 :height 15}
         :accessibility-label :emoji
         :source              (reaction.resource/get-reaction emoji)}]
       [text/text
        {:size   :paragraph-2
         :weight :semi-bold
         :style  style/reaction-count}
        (str (if (pos? numeric-value) numeric-value 1))]])))

(def view (quo.theme/with-theme view-internal))
