(ns quo.components.cards.wallet-card.view
  (:require [quo.components.cards.wallet-card.schema :as component-schema]
            [quo.components.cards.wallet-card.style :as style]
            [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [schema.core :as schema]))

(defn- view-internal
  [{:keys [image title subtitle dismissible? on-press on-press-close]}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:on-press            on-press
      :accessibility-label :wallet-card}
     [rn/view {:style (style/root-container theme)}
      [rn/view {:style style/top-container}
       [fast-image/fast-image
        {:style               style/image
         :source              image
         :accessibility-label :image}]
       (when dismissible?
         [rn/pressable
          {:on-press            on-press-close
           :accessibility-label :icon-container
           :hit-slop            {:top 5 :bottom 5 :left 5 :right 5}}
          [icon/icon :i/close
           {:size                12
            :accessibility-label :close-icon}]])]
      [text/text
       {:style           (style/title theme)
        :size            :paragraph-1
        :weight          :semi-bold
        :number-of-lines 1}
       title]
      [text/text
       {:style           (style/subtitle theme)
        :size            :paragraph-2
        :weight          :regular
        :number-of-lines 1}
       subtitle]]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
