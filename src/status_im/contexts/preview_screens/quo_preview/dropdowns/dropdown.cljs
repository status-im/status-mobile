(ns status-im.contexts.preview-screens.quo-preview.dropdowns.dropdown
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :outline}
              {:key :grey}
              {:key :ghost}
              {:key :customization}]}
   {:key     :state
    :type    :select
    :options [{:key :default}
              {:key :active}
              {:key :disabled}]}
   {:key     :size
    :type    :select
    :options [{:key   :size-40
               :value "40"}
              {:key   :size-32
               :value "32"}
              {:key   :size-24
               :value "24"}]}
   {:key     :background
    :type    :select
    :options [{:key :photo}
              {:key :blur}]}
   {:key  :icon?
    :type :boolean}
   {:key     :icon-name
    :type    :select
    :options [{:key :i/wallet}
              {:key :i/group}
              {:key :i/locked}]}
   {:key  :emoji?
    :type :boolean}
   {:key  :label
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state      (reagent/atom {:type                :outline
                                  :state               :default
                                  :size                :size-40
                                  :label               "Dropdown"
                                  :icon?               false
                                  :emoji?              false
                                  :customization-color :purple})
        label      (reagent/cursor state [:label])
        emoji?     (reagent/cursor state [:emoji?])
        background (reagent/cursor state [:background])]
    [:f>
     (fn []
       (rn/use-effect (fn []
                        (swap! state assoc :label (if @emoji? "🍑" "Dropdown")))
                      [@emoji?])
       [preview/preview-container
        {:state                     state
         :descriptor                descriptor
         :component-container-style (when-not (= @background :blur) {:align-items :center})
         :blur-container-style      {:align-items :center}
         :blur?                     (= @background :blur)
         :show-blur-background?     true}
        (when (= :photo (:background @state))
          [rn/image
           {:source (resources/get-mock-image :dark-blur-bg)
            :style  {:position      :absolute
                     :top           12
                     :left          20
                     :right         0
                     :bottom        0
                     :border-radius 12}
            :height 250
            :width  "100%"}])
        [quo/dropdown
         (assoc @state :on-press #(js/alert "Pressed dropdown"))
         @label]])]))
