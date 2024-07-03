(ns status-im.contexts.preview.quo.buttons.slide-button
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size-48}
              {:key :size-40}]}
   {:key     :type
    :type    :select
    :options [{:key :default}
              {:key :danger}]}
   {:key  :disabled?
    :type :boolean}
   {:key  :blur?
    :type :boolean}
   {:key  :keep-at-end-after-slide?
    :type :boolean}
   (preview/customization-color-option {:key :color})])

(defn f-view
  []
  (let [state                    (reagent/atom {:disabled?                false
                                                :color                    :blue
                                                :size                     :size-48
                                                :keep-at-end-after-slide? false})
        color                    (reagent/cursor state [:color])
        blur?                    (reagent/cursor state [:blur?])
        keep-at-end-after-slide? (reagent/cursor state [:keep-at-end-after-slide?])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style (when-not @blur? (:align-items :center))
        :blur?                     @blur?
        :show-blur-background?     true}
       [quo/slide-button
        {:track-text          "We gotta slide"
         :track-icon          :face-id
         :customization-color @color
         :size                (:size @state)
         :disabled?           (:disabled? @state)
         :blur?               @blur?
         :type                (:type @state)
         :on-complete         (fn [reset-fn]
                                (js/alert "Slide complete")
                                (reset-fn @keep-at-end-after-slide?))}]])))

(defn view [] [:f> f-view])
