(ns quo.components.selectors.react-selector.view
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.react-selector.style :as style]
    [quo.components.selectors.reaction-resource :as reaction.resource]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [emoji clicks state use-case on-press accessibility-label on-long-press container-style]}]
  (let [theme         (quo.theme/use-theme)
        numeric-value (int clicks)
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
       [rn/text
        {:accessibility-label :emoji}
        (reaction.resource/system-emojis emoji)]
       [text/text
        {:size   :paragraph-2
         :weight :semi-bold
         :style  style/reaction-count}
        (str (if (pos? numeric-value) numeric-value 1))]])))
