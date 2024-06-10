(ns status-im.contexts.preview.quo.gradient.gradient-cover
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [utils.re-frame :as rf]))

(defn render-action-sheet
  [customization-color]
  [:<>
   [quo/drawer-top
    {:type                 :account
     :blur?                false
     :title                "Collectibles vault"
     :networks             [{:network-name :ethereum :short-name "eth"}
                            {:network-name :optimism :short-name "oeth"}]
     :description          "0x0ah...78b"
     :account-avatar-emoji "🍿"
     :customization-color  (or customization-color :blue)}]
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
      {:icon         :i/delete
       :label        "Remove account"
       :danger?      true
       :on-press     #(js/alert "Remove account")
       :add-divider? true}]]]])

(def descriptor
  [(preview/customization-color-option)
   {:label "Blur (dark only)?"
    :key   :blur?
    :type  :boolean}])

(defn view
  []
  (let [state               (reagent/atom {:customization-color :blue :blur? false})
        blur?               (reagent/cursor state [:blur?])
        customization-color (reagent/cursor state [:customization-color])]
    [:f>
     (fn []
       (rn/use-effect (fn []
                        (when @blur?
                          (rf/dispatch [:theme/switch :dark])))
                      [@blur?])
       [preview/preview-container {:state state :descriptor descriptor}
        [rn/view
         {:style {:height        332
                  :margin-top    24
                  :overflow      :hidden
                  :border-radius 12}}
         (when @blur?
           [rn/image
            {:style  {:height 332}
             :source (resources/get-mock-image :dark-blur-bg)}])
         [(if @blur? quo/blur rn/view)
          {:style     {:height           332
                       :padding-vertical 40}
           :blur-type :dark}
          [quo/gradient-cover @state]]]
        [quo/button
         {:container-style {:margin-horizontal 40}
          :on-press        #(rf/dispatch [:show-bottom-sheet
                                          {:content             (fn []
                                                                  [render-action-sheet
                                                                   @customization-color])
                                           :gradient-cover?     true
                                           :customization-color @customization-color}])}
         "See in bottom sheet"]])]))
