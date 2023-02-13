(ns status-im2.contexts.quo-preview.drawers.drawer-buttons
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Top Heading"
    :key   :top-heading
    :type  :text}
   {:label "Top Sub heading"
    :key   :top-sub-heading
    :type  :text}
   {:label "Bottom heading"
    :key   :bottom-heading
    :type  :text}])

(defn text-with-link
  []
  [quo/text
   {:style {:flex      1
            :flex-wrap :wrap}}
   [quo/text
    {:size   :paragraph-2
     :style  {:flex  1
              :color (colors/alpha colors/white 0.7)}
     :weight :semi-bold}
    "By continuing you accept our "]
   [quo/text
    {:on-press #(js/alert "Terms of use clicked")
     :size     :paragraph-2
     :style    {:flex  1
                :color colors/white}
     :weight   :semi-bold}
    "Terms of Use"]])

(defn render-drawer-buttons
  [state]
  [rn/view
   {:height           300
    :background-color (colors/theme-colors colors/white colors/neutral-95)}
   [quo/drawer-buttons
    {:container-style {:margin-left  40
                       :margin-right 24}
     :top-card        {:on-press #(js/alert "top card clicked")
                       :heading  (:top-heading @state)}
     :bottom-card     {:on-press #(js/alert "bottom card clicked")
                       :heading  (:bottom-heading @state)}}
    (:top-sub-heading @state) [text-with-link]]])

(defn cool-preview
  []
  (let [state (reagent/atom {:top-heading     "Sign in "
                             :top-sub-heading "You already use Status"
                             :bottom-heading  "I'm new to status"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 400}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60}
         [render-drawer-buttons state]]]])))

(defn preview-drawer-buttons
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :nestedScrollEnabled       true
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    :id}]])
