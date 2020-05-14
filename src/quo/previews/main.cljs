(ns quo.previews.main
  (:require [quo.previews.header :as header]
            [quo.previews.text :as text]
            [quo.previews.text-input :as text-input]
            [quo.previews.tooltip :as tooltip]
            [quo.previews.button :as button]
            [quo.previews.lists :as lists]
            [quo.previews.bottom-sheet :as bottom-sheet]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [quo.design-system.colors :as colors]
            [quo.theme :as theme]
            [status-im.ui.screens.routing.core :as navigation]))

(def screens [{:name      :texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :tooltip
               :insets    {:top false}
               :component tooltip/preview-tooltip}
              {:name      :text-input
               :insets    {:top false}
               :component text-input/preview-text}
              {:name      :headers
               :insets    {:top false}
               :component header/preview-header}
              {:name      :button
               :insets    {:top false}
               :component button/preview-button}
              {:name      :lists
               :instes    {:top false}
               :component lists/preview}
              {:name      :bottom-sheet
               :insets    {:top false}
               :component bottom-sheet/preview}])

(defn theme-switcher []
  [rn/view {:style {:flex-direction   :row
                    :margin-vertical  8
                    :border-radius    4
                    :background-color (:ui-01 @colors/theme)
                    :border-width     1
                    :border-color     (:ui-02 @colors/theme)}}
   [rn/touchable-opacity {:style    {:padding         8
                                     :flex            1
                                     :justify-content :center
                                     :align-items     :center}
                          :on-press #(theme/set-theme :light)}
    [quo/text "Set light theme"]]
   [rn/view {:width            1
             :margin-vertical  4
             :background-color (:ui-02 @colors/theme)}]
   [rn/touchable-opacity {:style    {:padding         8
                                     :flex            1
                                     :justify-content :center
                                     :align-items     :center}
                          :on-press #(theme/set-theme :dark)}
    [quo/text "Set dark theme"]]])

(defn main-screen []
  [rn/scroll-view {:flex               1
                   :padding-vertical   8
                   :padding-horizontal 16
                   :background-color   (:ui-background @colors/theme)}
   [theme-switcher]
   [rn/view
    (for [{:keys [name]} screens]
      [rn/touchable-opacity {:on-press #(navigation/navigate-to name nil)}
       [rn/view {:style {:padding-vertical 8}}
        [quo/text (str "Preview " name)]]])]])

(defonce navigation-state (atom nil))

(defn- persist-state! [state-obj]
  (js/Promise.
   (fn [resolve _]
     (reset! navigation-state state-obj)
     (resolve true))))

(defn preview-screens []
  (let [stack (navigation/create-stack)]
    [navigation/navigation-container
     {:ref             navigation/set-navigator-ref
      :initial-state   @navigation-state
      :on-state-change persist-state!}
     [stack {}
      (into [{:name      :main
              :insets    {:top false}
              :component main-screen}]
            screens)]]))



;; TODO(Ferossgp): Add separate build when shadow-cljs will be integrated
;; NOTE(Ferossgp): Separate app can be used to preview all available
;; and possible state for components, and for UI testing based on screenshots


(defn init []
  (.registerComponent ^js rn/app-registry "StatusIm" #(reagent/reactify-component preview-screens)))
