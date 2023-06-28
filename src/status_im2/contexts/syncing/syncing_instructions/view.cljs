(ns status-im2.contexts.syncing.syncing-instructions.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.syncing.syncing-instructions.style :as style]
            [utils.i18n :as i18n]))

(defn- render-element
  [[type value]]
  (case type
    :text
    [quo/text
     {:size   :paragraph-2
      :weight :regular
      :style  style/list-text} (i18n/label value)]

    :button-primary
    [quo/button
     {:type                :primary
      :customization-color :blue
      :size                24
      :style               style/button-primary} (i18n/label value)]

    :button-grey
    [quo/button
     {:type           :grey
      :override-theme :dark
      :size           24
      :style          style/button-grey}
     (i18n/label value)]

    :button-grey-placeholder
    [quo/button
     {:type           :grey
      :override-theme :dark
      :size           24
      :before         :i/placeholder
      :style          style/button-grey-placeholder}
     (i18n/label value)]

    :context-tag
    [quo/context-tag
     {:override-theme :dark
      :text-style     {:color colors/white}}
     (resources/get-mock-image (:source value))
     (i18n/label (:label value))]))

(defn- render-item
  [i list-item]
  [rn/view
   {:style style/numbered-list-item}
   [rn/view
    {:style style/list-icon}
    [quo/text
     {:size   :label
      :weight :medium
      :style  style/list-icon-text} i]]
   (map-indexed (fn [idx item]
                  ^{:key idx}
                  [render-element item])
                list-item)])

(defn- render-instruction
  [{:keys [title image list]}]
  [rn/view
   (when title
     [quo/text
      {:size   :paragraph-1
       :weight :semi-bold
       :style  style/paragraph} (i18n/label title)])
   (case (:type image)
     :container-image [rn/view {:style style/container-image}
                       [rn/image
                        {:source (resources/get-image
                                  (:source image))}]]
     :image           [rn/image
                       {:source (resources/get-image (:source image))
                        :style  style/image}]
     nil)
   [rn/view {:style style/numbered-list}
    (map-indexed (fn [i item]
                   ^{:key i}
                   [render-item (inc i) item])
                 list)]])

(defn- map-instructions
  [idx instructions instructions-count]
  [:<>
   [render-instruction instructions]
   (when-not (= (inc idx) instructions-count)
     [rn/view {:style style/hr}])])

(defn instructions
  [{:keys [title-label-key] :as props}]
  (let [platform      (reagent/atom :mobile)
        platform-data [{:id    :mobile
                        :label (i18n/label :t/mobile)}
                       {:id    :desktop
                        :label (i18n/label :t/desktop)}]]
    (fn []
      (let [instructions       (get props @platform)
            instructions-count (count instructions)]
        [rn/view {:style style/container-outer}
         [quo/text
          {:size   :heading-1
           :weight :semi-bold
           :style  style/heading} (i18n/label title-label-key)]
         [rn/view {:style style/tabs-container}
          [quo/segmented-control
           {:size           28
            :override-theme :dark
            :blur?          true
            :default-active :mobile
            :data           platform-data
            :on-change      #(reset! platform %)}]]
         [gesture/scroll-view
          (doall
           (map-indexed (fn [idx item]
                          ^{:key idx}
                          [map-instructions idx item instructions-count])
                        instructions))]]))))
