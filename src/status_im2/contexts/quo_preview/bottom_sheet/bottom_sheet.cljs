(ns status-im2.contexts.quo-preview.bottom-sheet.bottom-sheet
  (:require [quo2.components.buttons.button.view :as button]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show handle:"
    :key   :show-handle?
    :type  :boolean}
   {:label "Backdrop dismiss:"
    :key   :backdrop-dismiss?
    :type  :boolean}
   {:label "Expendable:"
    :key   :expandable?
    :type  :boolean}
   {:label "Disable drag:"
    :key   :disable-drag?
    :type  :boolean}])

(defn bottom-sheet-content
  []
  [rn/view
   {:style {:justify-content :center
            :align-items     :center}}
   [button/button {:on-press #(do (re-frame/dispatch [:hide-bottom-sheet]))} "Close bottom sheet"]

   [text/text {:style {:padding-top 20}} "Hello world!"]])

(defn cool-preview
  []
  (let [state                (reagent/atom {:show-handle?      true
                                            :backdrop-dismiss? true
                                            :expandable?       true
                                            :disable-drag?     false})
        on-bottom-sheet-open (fn []
                               (re-frame/dispatch [:show-bottom-sheet
                                                   (merge
                                                    {:content bottom-sheet-content}
                                                    @state)]))]
    (fn []
      [rn/view
       {:style {:margin-bottom 50
                :padding       16}}
       [preview/customizer state descriptor]
       [:<>
        [rn/view
         {:style {:align-items :center
                  :padding     16}}

         [button/button {:on-press on-bottom-sheet-open} "Open bottom sheet"]]]])))

(defn preview-bottom-sheet
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
