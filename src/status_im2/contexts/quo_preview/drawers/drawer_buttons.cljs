(ns status-im2.contexts.quo-preview.drawers.drawer-buttons
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :top-heading
    :type :text}
   {:key  :top-sub-heading
    :type :text}
   {:key  :bottom-heading
    :type :text}])

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

(defn view
  []
  (let [state (reagent/atom {:top-heading     "Sign in"
                             :top-sub-heading "You already use Status"
                             :bottom-heading  "I'm new to Status"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:margin-top 40}}
       [quo/drawer-buttons
        {:top-card    {:on-press #(js/alert "top card clicked")
                       :heading  (:top-heading @state)}
         :bottom-card {:on-press #(js/alert "bottom card clicked")
                       :heading  (:bottom-heading @state)}}
        (:top-sub-heading @state)
        [text-with-link]]])))
