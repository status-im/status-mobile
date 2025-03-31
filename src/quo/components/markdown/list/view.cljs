(ns quo.components.markdown.list.view
  (:require
    [quo.components.counter.step.view :as step]
    [quo.components.icon :as icon]
    [quo.components.markdown.list.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.tags.context-tag.view :as context-tag]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn get-colors
  [theme blur?]
  (cond (and blur? (= theme :dark)) colors/white-opa-40
        (= theme :dark)             colors/neutral-50
        :else                       colors/neutral-40))

(defn description-text
  [{:keys [description tag-name tag-picture description-after-tag blur?]}]
  (if-not tag-name
    [text/text
     {:accessibility-label :list-item-description
      :size                :paragraph-2}
     description]
    [rn/view {:style {:flex-direction :row :align-items :center}}
     [text/text
      {:accessibility-label :list-item-description
       :size                :paragraph-2}
      description]
     [rn/view {:style {:margin-left 4}}
      [context-tag/view
       {:blur?           blur?
        :size            24
        :profile-picture tag-picture
        :full-name       tag-name}]]
     [text/text
      {:style               {:margin-left 4}
       :accessibility-label :list-item-description-after-tag
       :size                :paragraph-2}
      description-after-tag]]))

(defn view
  [{:keys [title description tag-picture tag-name description-after-tag step-number
           customization-color type blur? container-style icon icon-props]
    :or   {type :bullet}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style (style/container container-style)}
     [rn/view
      (case type
        :step
        [step/view
         {:in-blur-view?       blur?
          :customization-color customization-color
          :type                (if customization-color :complete :neutral)} step-number]
        :lock
        [icon/icon :i/locked {:color (get-colors theme blur?)}]
        :custom-icon
        [icon/icon icon icon-props]
        [icon/icon :i/bullet {:color (get-colors theme blur?)}])]
     [rn/view {:style style/text-container}
      (when title
        [text/text
         {:accessibility-label :list-item-title
          :weight              :semi-bold
          :size                :paragraph-2}
         title])
      (when description
        [rn/view (when title {:style {:margin-top 0}})
         [description-text
          {:description           description
           :tag-name              tag-name
           :tag-picture           tag-picture
           :description-after-tag description-after-tag
           :blur?                 blur?}]])]]))
