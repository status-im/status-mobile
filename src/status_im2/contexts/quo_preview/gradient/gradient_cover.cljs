(ns status-im2.contexts.quo-preview.gradient.gradient-cover
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]))

(defn render-action-sheet
  []
  [:<>
   [rn/view {:style {:align-items :center}}
    [quo/summary-info
     {:type          :status-account
      :networks?     false
      :account-props {:customization-color :purple
                      :size                32
                      :emoji               "🍑"
                      :type                :default
                      :name                "Collectibles vault"
                      :address             "0x0ah...78b"}}]]
   [quo/action-drawer
    [[{:icon     :i/edit
       :label    "Edit account"
       :on-press #(js/alert "Edit account")}
      {:icon     :i/copy
       :label    "Copy address"
       :on-press #(js/alert "Copy address")}
      {:icon     :i/share
       :label    "Share account"
       :on-press #(js/alert "Share account")}
      {:icon     :i/delete
       :label    "Remove account"
       :danger?  true
       :on-press #(js/alert "Remove account")}]]]])

(def descriptor
  [(preview/customization-color-option)
   {:label "Blur (dark only)?"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state               (reagent/atom {:customization-color :blue :blur? false})
        blur?               (reagent/cursor state [:blur?])
        customization-color (reagent/cursor state [:customization-color])]
    [:f>
     (fn []
       (rn/use-effect (fn []
                        (when @blur?
                          (quo.theme/set-theme :dark)))
                      [@blur?])
       [:<>
        [preview/customizer state descriptor]
        [rn/view
         {:style {:height        332
                  :margin-top    24
                  :overflow      :hidden
                  :border-radius 12}}
         (when @blur?
           [rn/image
            {:style  {:height 332}
             :source (resources/get-mock-image :dark-blur-bg)}])
         [(if @blur? blur/view rn/view)
          {:style     {:height           332
                       :position         :absolute
                       :top              0
                       :left             0
                       :right            0
                       :bottom           0
                       :padding-vertical 40}
           :blur-type :dark}
          [quo/gradient-cover @state]]]
        [rn/view
         {:style {:padding-vertical   20
                  :padding-horizontal 16}}
         [quo/color-picker
          {:blur?     @blur?
           :selected  @customization-color
           :on-change #(reset! customization-color %)}]]
        [quo/button
         {:container-style {:margin-horizontal 40}
          :on-press        #(rf/dispatch [:show-bottom-sheet
                                          {:content             (fn [] [render-action-sheet])
                                           :gradient-cover?     true
                                           :customization-color @customization-color}])}
         "See in bottom sheet"]])]))

(defn preview-gradient-cover
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
