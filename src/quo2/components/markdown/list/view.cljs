(ns quo2.components.markdown.list.view
  (:require [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.counter.step.view :as step]
            [quo2.components.markdown.list.style :as style]
            [quo2.components.icon :as icon]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))

(defn- themed-view
  [{:keys [theme title description index step-props]}]
  [rn/view {:style style/container}
   [rn/view {:style style/index}
    (if index
      [step/step step-props index]
      [icon/icon :i/bullet {:color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}])]
   [rn/view {:style style/text-container}
    (when title
      [text/text
       {:accessibility-label :list-item-title
        :weight              :semi-bold
        :size                :paragraph-2}
       title])
    (when description
      [rn/view (when title {:style {:margin-top 1}})
       (if (string? description)
         [text/text
          {:accessibility-label :list-item-description
           :size                :paragraph-2}
          description]
         description)])]])

(def view (theme/with-theme themed-view))
