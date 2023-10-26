(ns status-im2.contexts.status-im-preview.common.floating-button-page.view
  (:require [quo.core :as quo]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.common.floating-button-page.view :as floating-button-page]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.status-im-preview.common.floating-button-page.style :as style]))

(defn view
  []
  (let [content-height (reagent/atom 300)
        slide?         (reagent/atom false)]
    (fn []
      [rn/view
       {:flex       1
        :margin-top (safe-area/get-top)}
       (when-not @slide?
         [rn/image
          {:style  style/background-image
           :source (resources/get-mock-image :dark-blur-bg)}])
       [floating-button-page/view
        {:header [quo/page-nav
                  {:type        :title-description
                   :title       "floating button page"
                   :description "press right icon to swap button type"
                   :text-align  :left
                   :right-side  [{:icon-name :i/swap
                                  :on-press  #(swap! slide? not)}]
                   :background  :blur
                   :icon-name   :i/close
                   :on-press    #(rf/dispatch [:navigate-back])}]
         :footer (if @slide?
                   [quo/slide-button
                    {:track-text          "We gotta slide"
                     :track-icon          :face-id
                     :container-style     {:z-index 2}
                     :customization-color :blue
                     :on-complete         #(js/alert "button slid")}
                    "Save address"]
                   [quo/button
                    {:container-style {:z-index 2}
                     :on-press        #(js/alert "button pressed")}
                    "Save address"])}
        [rn/view
         {:style (style/page-content @content-height)}
         [quo/text {:size :heading-1} "Page Content"]
         [quo/input
          {:auto-focus true
           :value      ""}]
         [quo/button
          {:type     :outline
           :on-press #(swap! content-height (fn [v] (+ v 10)))}
          "increase height"]
         [quo/button
          {:type     :outline
           :on-press #(swap! content-height (fn [v] (- v 10)))}
          "decrease height"]]]])))
